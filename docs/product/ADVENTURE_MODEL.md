# TrailCharter Adventure Information Model

Status: **AGREE / EXPLORE DETAIL**

This document records the agreed product backbone for Adventures while keeping unresolved implementation and UX detail explicitly open. It is deliberately product-led rather than database-led.

## Agreed backbone

- **Adventure** is the top-level container.
- Only an Adventure **title** is required to create one. Dates, summary, places, routes, stages and all other detail are optional.
- **Stage** is an optional ordered subdivision of an Adventure. A Stage may represent a day, leg, travel section, camp-to-camp segment or other meaningful phase. Simple Adventures must not be forced to create artificial stages.
- **Route**, **Place** and **Itinerary item** are shared concepts rather than duplicated feature-specific records.
- A Route may belong to the Adventure as a whole and/or be associated with a Stage.
- A Place is a reusable physical location that may be referenced from the itinerary, map, route, accommodation/arrangements, safety information or other modules.
- An Itinerary item is an ordered plan/event that may reference shared Places, Routes and Stages rather than copying their data.
- Planning/support modules such as Pack, Food & Water, Safety, Notes, Journal and Attachments belong to the Adventure and may optionally be associated with a Stage where useful.
- Accommodation, transport and similar booking/visit information should be modelled separately from the physical Place so booking details, dates, costs and notes do not become attributes of the location itself.

## Core product principles

### Enter once, use everywhere
Information should be entered once and shown wherever it is relevant. A campsite added to an itinerary should be available to the map and relevant Stage without requiring separate duplicate records.

### Complexity is optional
The same model must support both a simple Adventure such as `Walk Cadair Idris` and a multi-day expedition with ordered stages, routes, accommodation, safety planning, pack, food/water and journal data.

## Progress and completion

Status: **AGREE**

TrailCharter will support progress/completion against relevant Adventure items, including location-linked milestones such as reaching a Place or waypoint.

- **Manual completion is always available.** The user can tick an applicable milestone/item as achieved and can undo that completion.
- **Automatic location-based completion is optional.** When enabled, TrailCharter may mark a location-linked milestone as reached when active device-location information shows the user has entered the accepted arrival area around that Place.
- The user can choose their preferred behaviour in Settings. Initial modes are:
  - **Manual** — user explicitly marks milestones complete.
  - **Automatic when location is active** — eligible location-linked milestones may be completed from active TrailCharter location information.
- **Manual is the default.** Automatic completion requires explicit user choice and the relevant location permission/state.
- Automatic completion does **not** by itself authorise background location tracking. Existing least-privilege permission rules and the separate background-location decision remain authoritative.
- A user must be able to correct/undo an automatic completion.
- Completion should be recorded as user-owned Adventure state rather than inferred afresh every time the screen opens.

## First playable vertical slice

Status: **AGREE / IMPLEMENTED / PHYSICAL TEST PENDING**

The first physical-device Adventure alpha intentionally implements only the minimum coherent subset needed to exercise the model:

- Adventure list/home.
- Create Adventure with title required and summary/dates optional.
- Edit and delete an Adventure.
- Optional ordered Stages.
- Basic ordered Itinerary items/milestones, optionally associated with a Stage.
- Manual completion and undo.
- Adventure progress summary derived from persisted completion state.
- Local Room persistence with schema version 1.
- In-place upgrade continuity through the established development signing identity.
- Android system Back handling within the first-pass Adventure flow.

This slice is not the final IA or visual design. It exists to put the agreed product model onto a physical phone early and gather evidence before larger modules are built.

Implementation was merged through PR #8 as `0.2.0-alpha1` / versionCode 8 at merge commit `f2ae96614f21e6970e164be2704e3097b7a88e5c`. Post-merge Android CI run #31 (`33194533139`) passed unit tests, lint, debug assembly, continuity-signing certificate verification, Room-schema export and artifact upload.

Room schema version 1 is committed at `app/schemas/com.masamunr.trailcharter.data.adventure.TrailCharterDatabase/1.json`. CI also exports generated Room schemas so future schema changes can be compared against the compiler-produced migration baseline.

The first schema deliberately does **not** introduce Places, Routes, accommodation/arrangements, pack, food/water, safety, notes, journal, GPX or map-package tables before those details are sufficiently agreed.

## Still EXPLORE

- Exact arrival-radius/tolerance rules for automatic completion.
- Whether automatic completion should require dwell time or other anti-false-positive logic.
- Visual treatment of completed stages, itinerary items, places and overall Adventure progress.
- Whether non-location itinerary events can support other automatic completion triggers.
- Exact Adventure status/lifecycle values.
- Exact primary navigation and final screen structure.
- Detailed Place, Route, arrangement, pack, food/water, safety, note, journal and attachment models.
- Future Room schema changes for those concepts. All changes after schema version 1 require explicit versioned migrations; destructive production migration remains prohibited.
