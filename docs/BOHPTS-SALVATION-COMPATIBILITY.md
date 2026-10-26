# Salvation client compatibility — High Five server

This document concerns ONLY the new High Five server project. The previously shared Interlude `system.7z` is unrelated and must not be used for this work.

## Project scope
- Server base: L2J Mobius CT 2.6 HighFive.
- Intended game content: High Five.
- Target client family: Salvation.
- Compatibility work is being developed directly on `main`, as requested. Keep changes incremental and verify builds before claiming support.

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

## First implementation step on main
- `ProtocolVersion.java` now consumes the optional `0x104`-byte payload observed in a public Salvation-related protocol handler when the announced revision is 140. The block consists of `0x100` bytes plus a 4-byte check field. This prevents those bytes from being left in the packet buffer and misread as another packet.
- This is only handshake parsing groundwork. Protocol 140 remains unenabled by default because the High Five packet tables and payload layouts have not yet been verified against the target client.

## Detailed dispatch findings
- `gameserver/network/GamePacketHandler.java` reads the first byte and resolves it through the single static `ClientPackets.PACKET_ARRAY`; extended opcode `0xD0` is resolved through the static `ExClientPackets.PACKET_ARRAY`.
- `ClientPackets.java` and `ExClientPackets.java` are enum-based lookup tables. The dispatcher does not select a table based on `GameClient.getProtocolVersion()`.
- This means the current server has no demonstrated per-revision packet dispatch layer. A safe implementation must first introduce a protocol-specific packet mapping/adapter or prove that Salvation 140 uses compatible opcode mappings for the target packet set. We must not infer that compatibility from the revision number alone.
- The imported L2J Mobius High Five baseline has been merged into `main`. Continue all project changes on `main`.

## Validation gates before enabling Salvation 140
1. Unit tests for revision selection: High Five revisions remain accepted; Salvation 140 is accepted only when the Salvation adapter is explicitly enabled; unsupported revisions are rejected.
2. Packet dispatch tests for known client opcodes and extended opcodes, including invalid/out-of-range values and null/unimplemented handlers.
3. Protocol handshake tests covering protocol version, encryption key exchange, login, server list, game-server authentication, character list, character selection, and Enter World.
4. A clean Ant build on the JDK version declared by upstream `build.xml`.
5. Manual client smoke test with the exact Salvation distribution targeted for release. Until this succeeds, label support experimental.

## Important technical note
Adding 140 or 152 to `AllowedProtocolRevisions` alone is NOT sufficient. The server needs compatible protocol handling and packet implementations, and must pass login, server-list, game-server handshake, character selection, Enter World, and gameplay smoke tests.

## Work plan
1. Inspect the imported source's protocol-version validation and packet implementations.
2. Determine how this Mobius branch structures client protocol handling and whether multiple revisions are supported cleanly.
3. Compare existing High Five packet handlers with Salvation 140; identify concrete gaps before editing.
4. Implement support directly on `main`, keeping existing High Five behavior intact.
5. Build with the JDK declared in upstream `build.xml` (currently JDK 27) and run smoke tests.
6. Test Salvation 140; consider 152 only as a separate compatibility target.

## Sources reviewed
- Client system archive catalog listing Salvation RU/NA/EU protocol variants 140 and 152: https://sites.google.com/site/l2clientdev/system-m0nster-io
- Client resource listings for “Salvation 140 Protocol”: https://mmo-dev.info/tags/salvation/
- Server project references describing High Five protocol 273 and Salvation protocol 140: https://forum.lucera2.com/threads/high-five-h5-fafurion-i-pererabotka-seti.9157/page-2
- Compatibility catalog listing High Five revisions and Salvation 140/152: https://www.strix-guard.info/en/compatibility

## Packet-shuffling investigation (2026-10-26)
Further research found a public L2J Mobius Salvation/Arena packet-shuffler discussion describing a protocol mode in which a non-zero obfuscation seed in the version-check response causes the client to shuffle packet opcodes. This is a separate mechanism from Blowfish packet encryption.

Concrete findings in this repository:
- `gameserver/network/serverpackets/KeyPacket.java` currently writes an obfuscation key of `0` unconditionally.
- `gameserver/network/GamePacketHandler.java` resolves incoming base and extended opcodes directly through static packet arrays.
- Therefore, a non-zero seed cannot safely be sent until per-client opcode maps are generated and the dispatcher translates shuffled opcodes back to the original IDs. The extended opcode path must be handled consistently as well.
- The published example is useful as a research lead, not a drop-in patch. It references packet identifiers and assumptions that may not match this CT 2.6 branch, and it does not establish full client compatibility.

Implementation order:
1. Implement an isolated, deterministic shuffler with explicit seed and range validation.
2. Add per-connection inbound opcode translation and tests for normal, extended, and maintained handshake opcodes.
3. Advertise a non-zero seed only for the explicitly enabled Salvation protocol, leaving High Five unchanged.
4. Verify the complete login-to-world sequence against the exact client build before enabling it by default.

Research references:
- Packet-shuffling discussion for L2J Mobius Salvation/Arena (2026-01): https://mmo-dev.info/threads/packet-shuffler-for-l2jmobius-salvation-arena.35169/
- General explanation of Lineage 2 opcode obfuscation and protocol handshake: https://npetrovski.github.io/l2js-client/guide/gameProtocol.html

