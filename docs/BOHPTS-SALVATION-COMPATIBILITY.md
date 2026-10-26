# Salvation client compatibility — High Five server

This document concerns ONLY the new High Five server project. The previously shared Interlude `system.7z` is unrelated and must not be used for this work.

## Project scope
- Server base: L2J Mobius CT 2.6 HighFive.
- Intended game content: High Five.
- Target client family: Salvation.
- Keep all compatibility work isolated on a feature branch until validated.

## Protocol research
Public client archives and server-project references identify multiple Salvation protocol variants:
- Salvation protocol 140 is a documented and commonly distributed client variant.
- Salvation protocol 152 is also documented for some client distributions/regions.
- High Five commonly uses protocol revisions 267, 268, 271, and 273.

The current imported Mobius High Five configuration at `dist/game/config/Server.ini` allows `267;268;271;273` only. Therefore, this source does not currently advertise Salvation 140 or 152 as supported.

## Recommended target
Start the compatibility investigation with Salvation protocol 140 as the primary target. Keep protocol 152 as a separate possible target until the exact client distribution is chosen and verified. Do not promise 152 support based only on the word “Salvation”.

## Source inspection findings
- `gameserver/network/clientpackets/ProtocolVersion.java` reads the client revision and accepts it only when `ServerConfig.PROTOCOL_LIST` contains that revision; otherwise it rejects the connection. This confirms that changing the allow-list would only pass the first gate.
- `gameserver/network/ClientPackets.java` defines the High Five client packet ID-to-handler table as a fixed enum. Extended packets are also mapped in a separate `ExClientPackets.java` table. Salvation compatibility therefore requires validating packet ID mappings and packet payload layouts, not just the revision number.
- `loginserver/network/LoginClientPackets.java` has a separate fixed packet table for the login protocol. Login compatibility must be checked independently from game-server protocol compatibility.
- `GameClient.java` stores the negotiated protocol version, but the inspected `ProtocolVersion` handler does not itself select a Salvation-specific packet table or protocol adapter.

## Important technical note
Adding 140 or 152 to `AllowedProtocolRevisions` alone is NOT sufficient. The server needs compatible protocol handling and packet implementations, and must pass login, server-list, game-server handshake, character selection, Enter World, and gameplay smoke tests.

## Work plan
1. Inspect the imported source's protocol-version validation and packet implementations.
2. Determine how this Mobius branch structures client protocol handling and whether multiple revisions are supported cleanly.
3. Compare existing High Five packet handlers with Salvation 140; identify concrete gaps before editing.
4. Implement support on an isolated feature branch, keeping High Five behavior intact.
5. Build with the JDK declared in upstream `build.xml` (currently JDK 27) and run smoke tests.
6. Test Salvation 140; consider 152 only as a separate compatibility target.

## Sources reviewed
- Client system archive catalog listing Salvation RU/NA/EU protocol variants 140 and 152: https://sites.google.com/site/l2clientdev/system-m0nster-io
- Client resource listings for “Salvation 140 Protocol”: https://mmo-dev.info/tags/salvation/
- Server project references describing High Five protocol 273 and Salvation protocol 140: https://forum.lucera2.com/threads/high-five-h5-fafurion-i-pererabotka-seti.9157/page-2
- Compatibility catalog listing High Five revisions and Salvation 140/152: https://www.strix-guard.info/en/compatibility
