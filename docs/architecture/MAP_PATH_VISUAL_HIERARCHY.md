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

The APK-only physical pass applied the hierarchy after the existing Eryri East Pass 4 style had loaded. The map and BRouter packages did not change.

Physical result:

1. recognised walking routes stood out clearly and the stronger purple treatment was accepted;
2. recognised routes appeared too late while zooming in, so they needed to remain visible from a further-out zoom level;
3. ordinary paths/tracks were too faint, particularly beside recognised routes, and needed a modest visibility increase;
4. the hierarchy itself remained accepted: ordinary path < recognised route < TrailCharter-created route;
5. isolated/short paths remained present and were not filtered merely for looking disconnected.

## VersionCode 24 / Run #180 physical tuning

The APK-only tuning kept the agreed colour family and adjusted prominence only:

- recognised hiking-route relation lines begin at `z11.5` instead of `z13`;
- ordinary tracks use opacity `0.82`;
- ordinary paths use opacity `0.76`;
- recognised-route colour, weight, dash treatment and TrailCharter bright-red route treatment remain unchanged;
- no map-package, BRouter-package, routing-logic, privacy or source-path filtering changes were included.

**Physical result: PASS.** The user confirmed the tuned result looks good. Treat Run #180 / map-spike versionCode 24 as the accepted path-visual-hierarchy baseline for the next planner ↔ map integration work.
