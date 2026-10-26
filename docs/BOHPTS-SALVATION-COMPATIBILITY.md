# BOHPTS Salvation compatibility audit

Status: initial audit only. BOHPTS Salvation compatibility is NOT yet verified.

## Verified in the imported L2J Mobius High Five source

- Project: `L2J_Mobius_CT_2.6_HighFive`
- Build file: `build.xml`
- Build requirement declared by upstream: JDK 27 and Ant 1.9.10 or newer.
- Game server configuration: `dist/game/config/Server.ini`
- Current allowed protocol revisions: `267;268;271;273`
- Default game port: `7777`; default login port: `2106`

## Required checks before claiming Salvation support

1. Identify the exact Salvation client build/chronicle and its protocol revision.
2. Compare the client system files and protocol revision with the High Five server implementation.
3. Test login-server connection, server list, game-server handshake, character selection, and Enter World.
4. Test core gameplay packets: movement, targeting, attacks, skills, inventory, NPC interaction, chat, and community board.
5. Review custom client features and server-side packet handlers; implement only differences confirmed by client files/logs.
6. Verify server IP/ports, login/game registration, and client launcher/system configuration.
7. Build with the required JDK and run login/game server smoke tests before merging compatibility changes into main.

## Evidence needed to continue

Best evidence is the Salvation client `system` folder/archive and launcher details. If available, provide the client version/build number and any login or connection error text. Do not share account passwords or private server credentials.

## Important

Do not change `AllowedProtocolRevisions` blindly. Adding a number to this list does not implement a missing protocol or packet handler and may only allow an incompatible client to proceed further before failing.
