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

Progress is integrated into the planned Adventure structure rather than maintained as a separate user-facing checklist.

- **Stage is the primary progress unit** for staged Adventures. A planned Stage can be manually ticked complete and unticked again.
- The Adventure progress bar is derived directly from completed Stages divided by total Stages.
- A separate Progress/milestone-entry section is not required merely to duplicate the Stage plan.
- Itinerary items remain a distinct planning concept for future detailed itinerary information, but they do not drive the main Adventure progress bar simply by existing.
- **Manual completion is always available.**
- **Automatic location-based completion remains optional future behaviour.** When enabled, TrailCharter may mark an eligible Stage/location as reached from active device-location information.
- **Manual is the default.** Automatic completion requires explicit user choice and the relevant location permission/state.
- Automatic completion does **not** by itself authorise background location tracking. Existing least-privilege permission rules and the separate background-location decision remain authoritative.
- A user must be able to correct/undo an automatic completion.
- Completion is stored as user-owned Adventure state rather than inferred afresh every time the screen opens.

## First playable vertical slice

Status: **AGREE / IMPLEMENTED / PHYSICAL TESTING ACTIVE**

The first physical-device Adventure alpha intentionally implements only the minimum coherent subset needed to exercise the model:

- Adventure list/home.
- Create Adventure with title required and summary/dates optional.
- Edit and delete an Adventure.
- Optional ordered Stages.
- Local completion/progress state.
- Adventure progress summary derived from persisted completion state.
- Local Room persistence.
- In-place upgrade continuity through the established development signing identity.
- Android system Back handling within the first-pass Adventure flow.

This slice is not the final IA or visual design. It exists to put the agreed product model onto a physical phone early and gather evidence before larger modules are built.

Implementation began through PR #8 as `0.2.0-alpha1` / versionCode 8 at merge commit `f2ae96614f21e6970e164be2704e3097b7a88e5c`. Post-merge Android CI run #31 (`33194533139`) passed unit tests, lint, debug assembly, continuity-signing certificate verification, Room-schema export and artifact upload.

Room schema version 1 is committed at `app/schemas/com.masamunr.trailcharter.data.adventure.TrailCharterDatabase/1.json`. CI exports generated Room schemas so future schema changes can be compared against compiler-produced migration baselines.

The early schema deliberately does **not** introduce Places, Routes, accommodation/arrangements, pack, food/water, safety, notes, journal, GPX or map-package tables before those details are sufficiently agreed.

## First physical-device feedback

Status: **AGREE / IMPLEMENTED / PHYSICAL RETEST PENDING**

The first `0.2.0-alpha1` device pass produced these concrete corrections, implemented in `0.2.1-alpha1` / versionCode 9:

- Adventure dates use UK display order `dd/MM/yyyy` throughout the app.
- Date selection uses a calendar picker rather than requiring typed ISO/US-style input.
- Long Stage/milestone entry screens remain scrollable above the software keyboard using IME insets and bottom scroll clearance so input controls cannot become trapped behind the keyboard.
- Adventure planning has an explicit finish/save path. `Save adventure` returns to the Adventures list and `Save & new adventure` starts another Adventure immediately.
- Unit tests lock UK date ordering and calendar date round-trip behaviour.

These fixes were merged through PR #9 at merge commit `62dba4df7be7615be073e7538cea1643940f2367`. Post-merge Android CI run #33 (`33196188717`) passed unit tests, lint, debug assembly, continuity-signing certificate verification, Room-schema export and artifact upload.

## Stage-integrated progress refinement

Status: **AGREE / IMPLEMENTED / PHYSICAL RETEST PENDING**

Physical testing of `0.2.1-alpha1` confirmed that a separate Progress/milestone section unnecessarily duplicated the Stage plan.

- Each Stage carries its own completion state and is ticked directly when achieved.
- The progress bar uses completed Stages / total Stages both inside the Adventure and on the Adventures home card.
- The separate user-facing Progress/milestone-entry UI is removed from this first-pass flow.
- Existing alpha itinerary/milestone database rows are preserved rather than destructively deleted. They no longer drive the visible progress model and remain available for future itinerary-model work.
- Room schema version 2 adds Stage completion state through an explicit versioned migration from schema 1; destructive migration remains prohibited.

This refinement was merged through PR #10 as `0.2.2-alpha1` / versionCode 10 at merge commit `080b19a907490c1fdaf42423f4376167a192b091`. Post-merge Android CI run #36 (`33197950163`) passed unit tests, lint, debug assembly, continuity-signing certificate verification, Room-schema export and artifact upload.

## Planning-session save and date-range refinement

Status: **AGREE / IMPLEMENTATION IN PROGRESS**

Physical testing of `0.2.2-alpha1` produced two further simplifications:

- Start and end dates are presented as two compact adjacent fields rather than separate multi-row controls.
- Both dates are selected from one date-range calendar picker. UK `dd/MM/yyyy` display remains authoritative.
- Adventure planning sessions use **explicit save**, not background persistence of edits. Detail changes, Stage additions/removals and Stage completion changes remain local to the active planning session until the user saves.
- `Save adventure` commits the planning session and returns to the Adventures list. `Save & new adventure` commits it and starts another Adventure.
- If the user backs out of a planning session with unsaved changes, TrailCharter asks whether to **Save**, **Don't save**, or **Cancel** the exit.
- Saving the planning session is performed atomically at the repository/Room boundary so the Adventure and its Stage changes are committed together.
- This refinement does **not** require a Room schema change; schema version 2 remains current.

The earlier `0.2.1-alpha1` behaviour where some Stage edits were stored immediately is superseded by this explicit-save planning-session model.

## Map-planned Stages and routing

Status: **EXPLORE**

The intended product direction is for Stages to become spatially plannable rather than remaining text-only containers.

- A Stage may eventually reference one or more shared Routes and Places and be planned directly from the map.
- Route geometry should be reusable by the map, itinerary and Stage views rather than duplicated.
- Once route geometry and travel mode are known, TrailCharter should be able to derive useful planning information such as route distance and expected travel time.
- Exact routing engine, route-generation rules, transport/travel modes, offline routing architecture, elevation/speed assumptions and ETA calculations remain unresolved and must be investigated before implementation.
- Automatic route/time generation must remain editable by the user and must not turn an Adventure into a rigid schedule.

## Still EXPLORE

- Exact arrival-radius/tolerance rules for automatic completion.
- Whether automatic completion should require dwell time or other anti-false-positive logic.
- Visual treatment of completed Stages, Places and overall Adventure progress.
- Whether non-location events can support other automatic completion triggers.
- Exact Adventure status/lifecycle values.
- Exact primary navigation and final screen structure.
- Detailed Place, Route, arrangement, itinerary, pack, food/water, safety, note, journal and attachment models.
- Future Room schema changes for those concepts. All changes after schema version 2 require explicit versioned migrations; destructive production migration remains prohibited.
