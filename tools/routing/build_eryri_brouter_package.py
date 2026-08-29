#!/usr/bin/env python3
"""Build the first TrailCharter Eryri BRouter WALK routing package.

This is a technical-spike transport container, not a FINAL TrailCharter package format.
It deliberately keeps BRouter routing data and profiles outside the Android APK.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
import tempfile
import urllib.request
import zipfile
from pathlib import Path

BRT_VERSION = "1.7.10"
PACKAGE_ID = "uk-wales-eryri-brouter-walk-spike"
SEGMENT_NAME = "W5_N50.rd5"
SEGMENT_URL = f"https://brouter.de/brouter/segments4/{SEGMENT_NAME}"
PROFILE_URL = (
    "https://raw.githubusercontent.com/abrensch/brouter/"
    f"v{BRT_VERSION}/misc/profiles2/hiking-mountain.brf"
)
LOOKUPS_URL = (
    "https://raw.githubusercontent.com/abrensch/brouter/"
    f"v{BRT_VERSION}/misc/profiles2/lookups.dat"
)
MAX_SEGMENT_BYTES = 180 * 1024 * 1024
MAX_PACKAGE_BYTES = 190 * 1024 * 1024


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for block in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def download(url: str, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    print(f"Downloading {url}")
    request = urllib.request.Request(url, headers={"User-Agent": "TrailCharter-routing-spike/1"})
    with urllib.request.urlopen(request, timeout=120) as response, destination.open("wb") as output:
        shutil.copyfileobj(response, output)
    if destination.stat().st_size <= 0:
        raise RuntimeError(f"Downloaded empty payload: {url}")


def payload(path: Path, archive_path: str, source: str, payload_type: str) -> dict:
    return {
        "path": archive_path,
        "type": payload_type,
        "bytes": path.stat().st_size,
        "sha256": sha256(path),
        "source": source,
    }


def enable_misplaced_via_correction(profile: Path) -> None:
    """Apply the one TrailCharter spike override to BRouter's pinned hiking profile."""
    profile_text = profile.read_text(encoding="utf-8")
    if re.search(r"(?m)^\s*assign\s+correctMisplacedViaPoints\b", profile_text):
        raise RuntimeError(
            "Pinned hiking profile now defines correctMisplacedViaPoints itself; review the upstream change"
        )

    marker = "---context:way"
    if marker not in profile_text:
        raise RuntimeError("Pinned BRouter profile no longer contains the expected way-context marker")

    override = (
        "# TrailCharter EXPLORE spike override: remove short detours caused by shaping via points.\n"
        "assign correctMisplacedViaPoints = true\n"
        "assign correctMisplacedViaPointsDistance = 400\n\n"
    )
    profile.write_text(profile_text.replace(marker, override + marker, 1), encoding="utf-8")


def build(output: Path) -> None:
    output.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="trailcharter-brouter-") as tmp_name:
        root = Path(tmp_name)
        package = root / "package"
        segment = package / "segments4" / SEGMENT_NAME
        profile = package / "profiles2" / "hiking-mountain.brf"
        lookups = package / "profiles2" / "lookups.dat"

        download(SEGMENT_URL, segment)
        download(PROFILE_URL, profile)
        download(LOOKUPS_URL, lookups)

        if segment.stat().st_size > MAX_SEGMENT_BYTES:
            raise RuntimeError(
                f"BRouter segment exceeded spike safety ceiling: {segment.stat().st_size} bytes"
            )

        profile_text = profile.read_text(encoding="utf-8")
        if not re.search(r"(?m)^\s*assign\s+validForFoot\s*(?:=\s*)?1(?:\s*(?:#.*)?)?$", profile_text):
            raise RuntimeError("Pinned BRouter profile no longer declares validForFoot = 1")

        enable_misplaced_via_correction(profile)
        corrected_profile_text = profile.read_text(encoding="utf-8")
        if not re.search(
            r"(?m)^\s*assign\s+correctMisplacedViaPoints\s*=\s*true\s*$",
            corrected_profile_text,
        ):
            raise RuntimeError("TrailCharter misplaced-via correction was not applied")

        manifest = {
            "schemaVersion": 1,
            "packageId": PACKAGE_ID,
            "displayName": "Eryri BRouter walking spike",
            "bounds": [-5.0, 50.0, 0.0, 55.0],
            "routingEngine": {"name": "BRouter", "version": BRT_VERSION},
            "travelMode": "WALK",
            "runtimeNetworkRequired": False,
            "profileOverrides": {
                "correctMisplacedViaPoints": True,
                "correctMisplacedViaPointsDistance": 400,
                "status": "EXPLORE",
            },
            "dataSnapshot": {
                "source": "BRouter published segments4 weekly dataset",
                "segment": SEGMENT_NAME,
                "reproducibleSnapshot": False,
                "note": "EXPLORE spike only; production should build/pin routing data from an attributable OSM extract.",
            },
            "payloads": {
                "segment": payload(segment, f"segments4/{SEGMENT_NAME}", SEGMENT_URL, "brouter-rd5"),
                "profile": payload(profile, "profiles2/hiking-mountain.brf", PROFILE_URL, "brouter-profile"),
                "lookups": payload(lookups, "profiles2/lookups.dat", LOOKUPS_URL, "brouter-lookups"),
            },
        }
        manifest_path = package / "manifest.json"
        manifest_path.write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")

        with zipfile.ZipFile(output, "w", allowZip64=True) as archive:
            for file_path in sorted(path for path in package.rglob("*") if path.is_file()):
                relative = file_path.relative_to(package).as_posix()
                compression = zipfile.ZIP_STORED if file_path.suffix == ".rd5" else zipfile.ZIP_DEFLATED
                archive.write(file_path, relative, compress_type=compression)

    package_bytes = output.stat().st_size
    if package_bytes > MAX_PACKAGE_BYTES:
        output.unlink(missing_ok=True)
        raise RuntimeError(f"Routing package exceeded spike safety ceiling: {package_bytes} bytes")

    print("Routing package built successfully")
    print(f"Path: {output}")
    print(f"Bytes: {package_bytes}")
    print(f"SHA-256: {sha256(output)}")
    print("This ZIP is an EXPLORE spike transport container, not a FINAL TrailCharter package format.")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("build/routing/trailcharter-eryri-brouter-walk-spike.zip"),
    )
    args = parser.parse_args()
    build(args.output.resolve())


if __name__ == "__main__":
    main()
