#!/usr/bin/env python3
"""Build the TrailCharter Eryri Pass 4 offline-map package on a desktop PC.

This is spike tooling, not a final production package format. It deliberately keeps
heavy cartographic preparation off the Android device and outside the APK.

Requirements:
  * Python 3.11+
  * go-pmtiles / pmtiles CLI v1.31.2 (or a compatible version) on PATH, or --pmtiles
  * Internet access while building the package

The resulting ZIP contains a small manifest plus independently replaceable local
basemap, terrain, contour, hiking-route relation and glyph files. Runtime
TrailCharter remains offline.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import os
from pathlib import Path
import shutil
import sqlite3
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request
import zipfile

PMTILES_VERSION = "1.31.2"
# Pass 4 extends the eastern edge into Capel Curig / Moel Siabod while keeping the
# other bounds stable so package growth can be measured incrementally.
SPIKE_BBOX = (-4.22, 52.97, -3.88, 53.18)
TERRAIN_HIGH_BBOX = (-4.15, 53.035, -3.88, 53.12)
TERRAIN_Z16_WYDDFA_BBOX = (-4.105, 53.055, -4.045, 53.09)
TERRAIN_Z16_SIABOD_BBOX = (-3.97, 53.045, -3.90, 53.105)

PROTOMAPS_URL = "https://data.source.coop/protomaps/openstreetmap/v4.pmtiles"
MAPTERHORN_LOW_URL = "https://download.mapterhorn.com/planet.pmtiles"
MAPTERHORN_HIGH_URL = "https://download.mapterhorn.com/6-31-20.pmtiles"
OS_TERRAIN50_DOWNLOADS_URL = "https://api.os.uk/downloads/v1/products/Terrain50/downloads"
OVERPASS_ENDPOINTS = (
    "https://overpass-api.de/api/interpreter",
    "https://overpass.kumi.systems/api/interpreter",
)
GLYPH_URL = (
    "https://raw.githubusercontent.com/openmaptiles/fonts/"
    "025ff2b2f84cc0fdf11f7b1d74b3a784595fe7a4/Open%20Sans%20Regular/0-255.pbf"
)

PACKAGE_ID = "uk-wales-eryri-east-pass4-spike"
PACKAGE_NAME = "Eryri East Pass 4 offline topo"
HIKING_ROUTES_PATH = "eryri-hiking-routes.geojson"


def bbox_text(bounds: tuple[float, float, float, float]) -> str:
    return ",".join(str(value) for value in bounds)


def run(*args: str) -> None:
    print("+", " ".join(args), flush=True)
    subprocess.run(args, check=True)


def download(url: str, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    print(f"Downloading {url}", flush=True)
    request = urllib.request.Request(
        url,
        headers={"User-Agent": "TrailCharter-map-package-spike/1"},
    )
    with urllib.request.urlopen(request, timeout=240) as response, destination.open("wb") as output:
        shutil.copyfileobj(response, output)
    if destination.stat().st_size <= 0:
        raise RuntimeError(f"Download produced an empty file: {destination}")


def fetch_overpass(query: str) -> dict[str, object]:
    encoded = urllib.parse.urlencode({"data": query}).encode("utf-8")
    errors: list[str] = []
    for endpoint in OVERPASS_ENDPOINTS:
        print(f"Querying OSM hiking-route relations via {endpoint}", flush=True)
        request = urllib.request.Request(
            endpoint,
            data=encoded,
            headers={
                "User-Agent": "TrailCharter-map-package-spike/1",
                "Content-Type": "application/x-www-form-urlencoded",
            },
        )
        try:
            with urllib.request.urlopen(request, timeout=240) as response:
                payload = json.loads(response.read().decode("utf-8"))
            if not isinstance(payload, dict) or not isinstance(payload.get("elements"), list):
                raise RuntimeError("Overpass response did not contain an elements array")
            return payload
        except (OSError, RuntimeError, json.JSONDecodeError, urllib.error.URLError) as error:
            errors.append(f"{endpoint}: {error}")
            print(f"WARNING: {errors[-1]}", file=sys.stderr, flush=True)
    raise RuntimeError("All Overpass endpoints failed: " + " | ".join(errors))


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def safe_extract_zip(archive: Path, destination: Path) -> None:
    destination.mkdir(parents=True, exist_ok=True)
    root = destination.resolve()
    with zipfile.ZipFile(archive) as zipped:
        for info in zipped.infolist():
            resolved = (destination / info.filename).resolve()
            if os.path.commonpath([root, resolved]) != str(root):
                raise RuntimeError(f"Unsafe ZIP member: {info.filename}")
        zipped.extractall(destination)


def find_os_vector_url(payload: object) -> str:
    records: list[dict[str, object]] = []

    def visit(value: object) -> None:
        if isinstance(value, dict):
            if value.get("url"):
                records.append(value)
            for child in value.values():
                visit(child)
        elif isinstance(value, list):
            for child in value:
                visit(child)

    visit(payload)

    def text(record: dict[str, object], key: str) -> str:
        return str(record.get(key, "")).strip().lower()

    for record in records:
        combined = " ".join(
            str(record.get(key, ""))
            for key in ("area", "format", "fileName", "name", "url")
        ).lower()
        if text(record, "area") in {"gb", "great britain", "national"} and (
            "vector" in combined and "tile" in combined
        ):
            return str(record["url"])

    for record in records:
        combined = " ".join(
            str(record.get(key, ""))
            for key in ("format", "fileName", "name", "url")
        ).lower()
        if "mbtiles" in combined or ("vector" in combined and "tile" in combined):
            return str(record["url"])

    raise RuntimeError("Could not locate the OS Terrain 50 vector-tile download")


def x_for_lon(lon: float, zoom: int) -> int:
    n = 1 << zoom
    return max(0, min(n - 1, int(math.floor((lon + 180.0) / 360.0 * n))))


def y_for_lat(lat: float, zoom: int) -> int:
    n = 1 << zoom
    lat_rad = math.radians(max(-85.05112878, min(85.05112878, lat)))
    y = (1.0 - math.asinh(math.tan(lat_rad)) / math.pi) / 2.0 * n
    return max(0, min(n - 1, int(math.floor(y))))


def clip_os_contours(source: Path, destination: Path) -> int:
    west, south, east, north = SPIKE_BBOX
    if destination.exists():
        destination.unlink()

    database = sqlite3.connect(destination)
    try:
        database.execute("CREATE TABLE metadata (name TEXT, value TEXT)")
        database.execute(
            "CREATE TABLE tiles (zoom_level INTEGER, tile_column INTEGER, tile_row INTEGER, tile_data BLOB)"
        )
        database.execute(
            "CREATE UNIQUE INDEX tile_index ON tiles (zoom_level, tile_column, tile_row)"
        )
        database.execute("ATTACH DATABASE ? AS src", (str(source),))

        metadata = dict(database.execute("SELECT name, value FROM src.metadata").fetchall())
        metadata["bounds"] = bbox_text(SPIKE_BBOX)
        metadata["minzoom"] = "9"
        metadata["maxzoom"] = "14"
        database.executemany(
            "INSERT INTO metadata(name, value) VALUES (?, ?)",
            sorted(metadata.items()),
        )

        copied = 0
        for zoom in range(9, 15):
            n = 1 << zoom
            x_min = x_for_lon(west, zoom)
            x_max = x_for_lon(east, zoom)
            y_xyz_min = y_for_lat(north, zoom)
            y_xyz_max = y_for_lat(south, zoom)
            tms_min = n - 1 - y_xyz_max
            tms_max = n - 1 - y_xyz_min
            before = database.total_changes
            database.execute(
                """
                INSERT OR REPLACE INTO tiles(zoom_level, tile_column, tile_row, tile_data)
                SELECT zoom_level, tile_column, tile_row, tile_data
                FROM src.tiles
                WHERE zoom_level = ?
                  AND tile_column BETWEEN ? AND ?
                  AND tile_row BETWEEN ? AND ?
                """,
                (zoom, x_min, x_max, tms_min, tms_max),
            )
            added = database.total_changes - before
            copied += added
            print(f"OS contours z{zoom}: copied {added} tiles", flush=True)

        database.commit()
        database.execute("DETACH DATABASE src")
        if copied <= 0:
            raise RuntimeError("OS Terrain 50 Eryri clip contains no tiles")
        return copied
    finally:
        database.close()


def route_member_near_package(coordinates: list[list[float]]) -> bool:
    west, south, east, north = SPIKE_BBOX
    margin = 0.015
    return any(
        west - margin <= lon <= east + margin and south - margin <= lat <= north + margin
        for lon, lat in coordinates
    )


def build_hiking_routes_geojson(destination: Path) -> int:
    west, south, east, north = SPIKE_BBOX
    query = f"""[out:json][timeout:180];
relation[\"type\"=\"route\"][\"route\"~\"^(hiking|foot|walking)$\"][\"name\"]({south},{west},{north},{east});
out body geom;
"""
    payload = fetch_overpass(query)
    features: list[dict[str, object]] = []

    for element in payload.get("elements", []):
        if not isinstance(element, dict) or element.get("type") != "relation":
            continue
        tags = element.get("tags")
        members = element.get("members")
        if not isinstance(tags, dict) or not isinstance(members, list):
            continue
        name = str(tags.get("name", "")).strip()
        route = str(tags.get("route", "")).strip()
        if not name or route not in {"hiking", "foot", "walking"}:
            continue

        lines: list[list[list[float]]] = []
        for member in members:
            if not isinstance(member, dict) or member.get("type") != "way":
                continue
            geometry = member.get("geometry")
            if not isinstance(geometry, list):
                continue
            coordinates: list[list[float]] = []
            for node in geometry:
                if not isinstance(node, dict) or "lon" not in node or "lat" not in node:
                    continue
                coordinates.append([float(node["lon"]), float(node["lat"])])
            if len(coordinates) >= 2 and route_member_near_package(coordinates):
                lines.append(coordinates)

        if not lines:
            continue

        properties: dict[str, object] = {
            "name": name,
            "route": route,
            "relation_id": int(element.get("id", 0)),
        }
        for key in ("ref", "network", "operator", "name:cy"):
            value = str(tags.get(key, "")).strip()
            if value:
                properties[key.replace(":", "_")] = value

        geometry_object: dict[str, object]
        if len(lines) == 1:
            geometry_object = {"type": "LineString", "coordinates": lines[0]}
        else:
            geometry_object = {"type": "MultiLineString", "coordinates": lines}

        features.append(
            {
                "type": "Feature",
                "properties": properties,
                "geometry": geometry_object,
            }
        )

    features.sort(
        key=lambda feature: (
            str(feature["properties"].get("name", "")).casefold(),
            int(feature["properties"].get("relation_id", 0)),
        )
    )
    if not features:
        raise RuntimeError("No named hiking/walking route relations were found in the expanded Eryri bounds")
    if not any("watkin" in str(feature["properties"].get("name", "")).casefold() for feature in features):
        raise RuntimeError(
            "Expanded OSM relation extract does not contain a Watkin-named route; "
            "do not silently claim the Watkin Path acceptance case"
        )

    destination.write_text(
        json.dumps({"type": "FeatureCollection", "features": features}, separators=(",", ":")) + "\n",
        encoding="utf-8",
    )
    print(f"Named hiking/walking route relations: {len(features)}", flush=True)
    return len(features)


def manifest_entry(
    relative_path: str,
    file_path: Path,
    **metadata: object,
) -> dict[str, object]:
    return {
        "path": relative_path,
        "bytes": file_path.stat().st_size,
        "sha256": sha256(file_path),
        **metadata,
    }


def build_package(pmtiles: str, output: Path, keep_work: bool) -> None:
    started = time.perf_counter()
    run(pmtiles, "--help")

    temporary = tempfile.TemporaryDirectory(prefix="trailcharter-map-package-")
    work = Path(temporary.name)
    if keep_work:
        persisted = output.parent / f"{output.stem}-work"
        if persisted.exists():
            shutil.rmtree(persisted)
        temporary.cleanup()
        persisted.mkdir(parents=True)
        work = persisted

    package_root = work / "package"
    package_root.mkdir(parents=True, exist_ok=True)
    glyph_dir = package_root / "glyphs" / "TrailCharterSans"
    glyph_dir.mkdir(parents=True, exist_ok=True)

    basemap = package_root / "eryri-basemap.pmtiles"
    terrain_low = work / "eryri-terrain-z0-z12.pmtiles"
    terrain_high = work / "eryri-terrain-z13-z15-core-east.pmtiles"
    terrain_z16_wyddfa = work / "eryri-terrain-z16-wyddfa.pmtiles"
    terrain_z16_siabod = work / "eryri-terrain-z16-siabod.pmtiles"
    terrain = package_root / "eryri-terrain.pmtiles"
    contours_mbtiles = work / "eryri-contours.mbtiles"
    contours = package_root / "eryri-contours.pmtiles"
    hiking_routes = package_root / HIKING_ROUTES_PATH
    glyph = glyph_dir / "0-255.pbf"

    stage = time.perf_counter()
    run(
        pmtiles,
        "extract",
        PROTOMAPS_URL,
        str(basemap),
        f"--bbox={bbox_text(SPIKE_BBOX)}",
        "--maxzoom=15",
    )
    print(f"Basemap stage: {time.perf_counter() - stage:.1f}s", flush=True)

    stage = time.perf_counter()
    run(
        pmtiles,
        "extract",
        MAPTERHORN_LOW_URL,
        str(terrain_low),
        f"--bbox={bbox_text(SPIKE_BBOX)}",
        "--maxzoom=12",
    )
    run(
        pmtiles,
        "extract",
        MAPTERHORN_HIGH_URL,
        str(terrain_high),
        f"--bbox={bbox_text(TERRAIN_HIGH_BBOX)}",
        "--minzoom=13",
        "--maxzoom=15",
    )
    run(
        pmtiles,
        "extract",
        MAPTERHORN_HIGH_URL,
        str(terrain_z16_wyddfa),
        f"--bbox={bbox_text(TERRAIN_Z16_WYDDFA_BBOX)}",
        "--minzoom=16",
        "--maxzoom=16",
    )
    run(
        pmtiles,
        "extract",
        MAPTERHORN_HIGH_URL,
        str(terrain_z16_siabod),
        f"--bbox={bbox_text(TERRAIN_Z16_SIABOD_BBOX)}",
        "--minzoom=16",
        "--maxzoom=16",
    )
    run(
        pmtiles,
        "merge",
        str(terrain_low),
        str(terrain_high),
        str(terrain_z16_wyddfa),
        str(terrain_z16_siabod),
        str(terrain),
    )
    print(f"Terrain stage: {time.perf_counter() - stage:.1f}s", flush=True)

    stage = time.perf_counter()
    os_json_path = work / "os-terrain50-downloads.json"
    download(OS_TERRAIN50_DOWNLOADS_URL, os_json_path)
    os_payload = json.loads(os_json_path.read_text(encoding="utf-8"))
    os_vector_url = find_os_vector_url(os_payload)
    os_zip = work / "os-terrain50-vector.zip"
    os_dir = work / "os-terrain50-vector"
    download(os_vector_url, os_zip)
    safe_extract_zip(os_zip, os_dir)
    mbtiles_files = sorted(os_dir.rglob("*.mbtiles"))
    if not mbtiles_files:
        raise RuntimeError("OS Terrain 50 vector archive contains no MBTiles file")
    copied = clip_os_contours(mbtiles_files[0], contours_mbtiles)
    print(f"OS contour tiles copied: {copied}", flush=True)
    run(pmtiles, "convert", str(contours_mbtiles), str(contours))
    print(f"Contour stage: {time.perf_counter() - stage:.1f}s", flush=True)

    stage = time.perf_counter()
    route_count = build_hiking_routes_geojson(hiking_routes)
    print(f"Hiking-route relation stage: {time.perf_counter() - stage:.1f}s", flush=True)

    download(GLYPH_URL, glyph)

    files = [basemap, terrain, contours, hiking_routes, glyph]
    for path in files:
        if not path.exists() or path.stat().st_size <= 0:
            raise RuntimeError(f"Expected package file is missing or empty: {path}")

    manifest = {
        "schemaVersion": 2,
        "status": "EXPLORE_SPIKE",
        "packageId": PACKAGE_ID,
        "displayName": PACKAGE_NAME,
        "geography": "Eryri including Capel Curig and Moel Siabod, Wales, United Kingdom",
        "bounds": list(SPIKE_BBOX),
        "buildTool": {
            "name": "TrailCharter desktop map package builder",
            "pmtilesVersionExpected": PMTILES_VERSION,
        },
        "layers": {
            "basemap": manifest_entry(
                "eryri-basemap.pmtiles",
                basemap,
                kind="vector",
                nativeMaxZoom=15,
                source="OpenStreetMap / Protomaps",
            ),
            "terrain": manifest_entry(
                "eryri-terrain.pmtiles",
                terrain,
                kind="raster-dem",
                nativeMaxZoom=16,
                encoding="terrarium",
                source="Mapterhorn",
            ),
            "contours": manifest_entry(
                "eryri-contours.pmtiles",
                contours,
                kind="vector",
                nativeMaxZoom=14,
                intervalMetres=10,
                source="OS Terrain 50",
            ),
            "hikingRoutes": manifest_entry(
                HIKING_ROUTES_PATH,
                hiking_routes,
                kind="geojson",
                source="OpenStreetMap named hiking/walking route relations",
                featureCount=route_count,
                relationRoutes=["hiking", "foot", "walking"],
            ),
            "glyphs": manifest_entry(
                "glyphs/TrailCharterSans/0-255.pbf",
                glyph,
                kind="glyph-pbf",
                source="OpenMapTiles fonts",
            ),
        },
        "runtimeNetworkRequired": False,
    }
    manifest_path = package_root / "manifest.json"
    manifest_path.write_text(
        json.dumps(manifest, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )

    output.parent.mkdir(parents=True, exist_ok=True)
    if output.exists():
        output.unlink()
    with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_STORED) as zipped:
        for path in sorted(package_root.rglob("*")):
            if path.is_file():
                zipped.write(path, path.relative_to(package_root).as_posix())

    print("\nPackage built successfully", flush=True)
    print(f"Path: {output}")
    print(f"Bytes: {output.stat().st_size}")
    print(f"SHA-256: {sha256(output)}")
    print(f"Total build time: {time.perf_counter() - started:.1f}s")
    print("This ZIP is a spike transport container, not a FINAL TrailCharter package format.")

    if not keep_work:
        temporary.cleanup()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--pmtiles",
        default=shutil.which("pmtiles") or "pmtiles",
        help="Path to the go-pmtiles CLI (default: pmtiles on PATH)",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("build/maps/trailcharter-eryri-east-pass4-spike.zip"),
        help="Output ZIP path",
    )
    parser.add_argument(
        "--keep-work",
        action="store_true",
        help="Keep intermediate downloads beside the output for debugging",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    try:
        build_package(args.pmtiles, args.output.resolve(), args.keep_work)
    except (
        OSError,
        RuntimeError,
        subprocess.CalledProcessError,
        sqlite3.Error,
        zipfile.BadZipFile,
    ) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())