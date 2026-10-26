# Lineage High Five — BOHPTS Salvation Compatibility

This repository is the separate workspace for adapting an L2J Mobius High Five server toward compatibility with the BOHPTS Salvation client.

## Current status

- [x] Dedicated repository created
- [x] Project scope documented
- [ ] Identify the exact L2J Mobius High Five revision
- [ ] Obtain and inspect the Salvation client patch/files and available technical documentation
- [ ] Determine client protocol revision and login/game-server handshake requirements
- [ ] Implement compatibility changes in small, reviewable commits
- [ ] Build and test each milestone

## Important

Protocol compatibility is not established yet. A High Five server foundation does not automatically support a Salvation client. Do not treat the project as playable until login, character selection, world entry, movement, combat, and core gameplay packets have been verified.

## Planned approach

1. Preserve a clean baseline of the chosen L2J Mobius High Five source.
2. Document the client/server protocol differences before changing packet handlers.
3. Isolate compatibility code where practical instead of scattering client-specific checks across the codebase.
4. Record build and test results for each change.

## Client files and licensing

Only add client assets or third-party code when redistribution is permitted. Prefer technical notes, patch metadata, and compatibility code over committing a full proprietary game client.

## Imported server source

The upstream L2J Mobius CT 2.6 High Five source is imported by the manual GitHub Actions workflow into `server/L2J_Mobius_CT_2.6_HighFive`. Review upstream licensing and notices before redistribution. Client compatibility with BOHPTS Salvation has not yet been implemented or verified.
