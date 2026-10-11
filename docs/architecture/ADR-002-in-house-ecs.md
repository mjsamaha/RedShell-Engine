# ADR-002: In-house ECS instead of Ashley

Status: accepted (Phase 2)

## Context
Phase 2 needs entities, components, queries and systems that compile under GWT. Ashley is the usual LibGDX choice.

## Decision
Build a small in-house ECS in `entity` (data) and `ecs` (world, queries, systems).

## Rationale
- Full control over GWT compatibility (no reflection, no `Class.cast`/`isInstance`).
- Small API surface that matches the layering rules (`entity` must not depend on `ecs`).
- Structural changes during a system update are rejected at runtime and must use the command buffer.
- Cached queries are rebuilt only when entity structure changes.

## Consequences
- We maintain the ECS ourselves. No spike against Ashley was prototyped; this was a desk decision.
- If performance or features become a problem, `World`/`Query` are the only seams to replace.