# Map path visual hierarchy

Status: **AGREE / SPIKE**

## Purpose

TrailCharter should distinguish ordinary mapped paths, recognised existing walking routes and TrailCharter-created routes without relying on unrelated colours or silently filtering potentially legitimate source geometry.

## AGREE: existing path colour family

- Ordinary mapped paths and tracks use a **single subdued violet/purple family**.
- Recognised existing walking routes derived from reliable OSM hiking/foot route relations use the **same colour family at substantially higher prominence** through stronger colour, weight and dash treatment.
- Relation-derived route labels use the recognised-route purple family; ordinary named-path labels remain quieter.
- Colour is not the only distinction. Weight, dash pattern and label treatment also communicate hierarchy.
- Short or isolated path fragments are **not removed merely because they look disconnected**. TrailCharter should prefer changing prominence over hiding potentially legitimate walking geometry.
- Clear semantic exclusions remain acceptable where source data says a line is not a walking path at all, but appearance/length/isolation alone is not an exclusion rule.

Initial spike colours:

- ordinary paths/tracks: `#8B79B5`;
- recognised hiking-route relations: `#6A3FD2`;
- ordinary named-path labels: `#6C608B`;
- recognised route labels: `#5730A5`.

These exact values remain subject to physical contrast/readability testing; the hierarchy and shared colour-family principle are the agreed product direction.

## AGREE: TrailCharter-created routes

- TrailCharter guided/magnetic routes remain visually separate from existing mapped routes and default to a bright red family.
- A future planner UI may offer a **curated user-selectable route-colour palette** rather than an unrestricted colour picker, with every offered colour tested against the topo style for contrast.
- Manual/direct geometry must remain distinguishable from guided/magnetic geometry by line pattern or another non-colour cue as well as colour.

## AGREE: legend

The production map should ultimately include a clear legend explaining at least:

- ordinary mapped path/track;
- recognised existing walking route;
- TrailCharter guided/magnetic route;
- TrailCharter manual/direct route.

The legend is a production UI requirement, not a reason to crowd the current technical spike.

## VersionCode 23 physical test

The APK-only physical pass applies the hierarchy after the existing Eryri East Pass 4 style has loaded. The map and BRouter packages do not change.

Acceptance focus:

1. ordinary paths remain readily traceable but no longer dominate the topo map;
2. recognised routes such as Watkin Path visibly stand out from ordinary paths while remaining part of the same colour family;
3. isolated/short ordinary path geometry remains present rather than being silently suppressed;
4. relation labels remain readable and visually associated with the recognised route line;
5. existing route-opacity, z18 inspection and routing-safety behaviour remain unchanged.
