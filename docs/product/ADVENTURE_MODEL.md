# TrailCharter Adventure Information Model

Status: **AGREE / EXPLORE DETAIL**

This document records the agreed product backbone for Adventures while keeping unresolved implementation and UX detail explicitly open. It is deliberately above the Room/database-schema level.

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

## Still EXPLORE

- Exact arrival-radius/tolerance rules for automatic completion.
- Whether automatic completion should require dwell time or other anti-false-positive logic.
- Visual treatment of completed stages, itinerary items, places and overall Adventure progress.
- Whether non-location itinerary events can support other automatic completion triggers.
- Exact Adventure status/lifecycle values.
- Exact primary navigation and screen structure.
- Room entities, relationships, indices and migrations. These remain deliberately deferred until the wider product model is sufficiently agreed.
