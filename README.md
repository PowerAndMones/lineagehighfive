# Lineage High Five — Salvation compatibility

This repository contains the L2J Mobius CT 2.6 High Five server source. The intended compatibility target is the BOHPTS Salvation client, starting with protocol 140.

## Get a ready-to-copy server package

A GitHub Actions workflow compiles the server using JDK 27 and publishes the resulting ZIP as an artifact.

1. Open [latest build runs](https://github.com/PowerAndMones/lineagehighfive/actions/workflows/highfive-build.yml).
2. Open the newest run with a green check mark.
3. At the bottom, under **Artifacts**, download **L2J-Mobius-HighFive-build**.
4. Extract the downloaded artifact ZIP, then extract the server package ZIP inside it.
5. Copy the extracted server files to your server machine. Configure database credentials, network addresses, and server settings before starting it.

If a run is red, do not use its package. Open the run and inspect the failed build step.

## Build status

- [x] High Five source imported into `main`.
- [x] Automated build on JDK 27.
- [x] Successful baseline compilation confirmed: [build run](https://github.com/PowerAndMones/lineagehighfive/actions/runs/40306655739).
- [x] Successful build publishes a downloadable artifact.
- [x] Salvation protocol 140 compatibility implemented (opcode remapping, Salvation packet layouts, optional opcode shuffling).
- [ ] Salvation protocol 140 verified with the target client (smoke test pending).
- [ ] Login, character selection, Enter World, and movement to be verified with the exact target client.

## Compatibility warning

**Salvation (protocol 140) support is implemented behind the `EnableSalvationProtocol` setting but is still experimental until the smoke test with the target client passes.** High Five clients (267;268;271;273) continue to work unchanged. Set `EnableSalvationProtocol = True` in `dist/game/config/Server.ini` and `SalvationClientMode = True` in `dist/login/config/Server.ini` (both enabled by default in this repository). See `docs/BOHPTS-SALVATION-COMPATIBILITY.md` for the implementation details.

## Source and licensing

Server source: `server/L2J_Mobius_CT_2.6_HighFive`. Review upstream license notices before redistribution. Do not commit proprietary client files unless redistribution is permitted.
