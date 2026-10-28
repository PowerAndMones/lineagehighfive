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


## Implemented: Salvation 140 adapter (2026-10-26)

The first working Salvation 140 adapter is implemented on `main`. Design notes:

### Opcode research results (Mobius CT 2.6 HighFive vs Mobius 5.0 Salvation)
- Client-to-server regular opcodes: identical (170/170 packet names share the same ids).
- Server-to-client regular opcodes: identical among all common packet names.
- Client-to-server Ex opcodes (0xD0 xx): 83 ids shifted; Salvation space is larger (max 0x15F vs local 0x97).
- Server-to-client Ex opcodes (0xFE xx): 146 ids shifted; Salvation space is larger (max 0x1FB vs local 0xE8).
- The Mobius 5.0 Salvation baseline ships seed 0 (no shuffling) and works with Salvation clients; shuffling is therefore optional and off by default.
- Login protocol (Init/AuthLogin/ServerList/ServerLogin opcodes) is identical; only the ServerList tail layout differs (Salvation writes feature mask 0xA4 and a plain per-server character-count list without deletion data).

### Changes on main
- `gameserver/network/SalvationProtocol.java` (new): generated Ex-opcode remap tables (Salvation <-> High Five, name-matched from both baselines) plus the outgoing 0xFE xx translation hook.
- `gameserver/network/PacketShuffler.java` (new): deterministic client-to-server opcode shuffler (public MrKirill1232 algorithm) operating over the Salvation opcode space, with the maintain list (EnterWorld, CharacterSelect, NetPing, ExPacket, raid connect/return).
- `gameserver/config/ServerConfig.java` + `dist/game/config/Server.ini`: `EnableSalvationProtocol` (adapter gate; 140 is rejected while off), `SalvationShuffle` (optional non-zero obfuscation seed) and `SalvationIsClassic` (see below).
- `gameserver/network/clientpackets/ProtocolVersion.java`: accepts 140 only through the adapter gate, consumes the optional 0x104-byte handshake block, registers the per-client shuffler when shuffling is enabled, and sends the obfuscation seed inside KeyPacket.
- `gameserver/network/GamePacketHandler.java`: per-client inbound opcode pipeline - unshuffle (when enabled), then Salvation-to-local Ex remap; Salvation-only Ex packets without a local counterpart are dropped and logged (`SALVATION:` prefix).
- `gameserver/network/GameClient.java`: outbound Ex opcode remap applied in `encrypt()` before encryption, so all server packets reach the 140 client with Salvation-space ids.
- `gameserver/network/serverpackets/KeyPacket.java`: obfuscation key field now carries the shuffler seed (0 keeps shuffling off) and Salvation clients receive the trailing `isClassic` byte the Salvation protocol appends.
- `SalvationIsClassic` (default `False`, shipped as `True` in `Server.ini`): controls the KeyPacket `isClassic` byte. With `True` the client loads its lighter classic data set instead of the full live data (item upgrade system etc.). This avoids the 32-bit `Insufficient Memory` crash inside `FL2UpgradeSystemData::Serialize <- LoadAlternateGameData <- OnChangeServerType <- VersionCheckPacket` that happens when a Salvation client enters the character selection screen on a memory-limited host. Packet layouts are identical in both modes.
- Handshake tracing: the whole Salvation login chain is logged with a `SALVATION:` prefix in `GameServer.log` - protocol acceptance (`ProtocolVersion`), `AuthLogin` (account + session key check), `PlayerAuthRequest`/auth result and the `CharSelectionInfo` character count - so a stuck client can be located precisely.
- `gameserver/network/serverpackets/CharSelectionInfo.java`: Salvation character-select layout (header flags, SP as long, reputation, 33-slot paperdoll, visual ids, per-slot enchant shorts, augmentation option pair, vitality/hero/noble tail).
- `gameserver/network/serverpackets/CharSelected.java`: Salvation EnterWorld layout (SP as long, no base-stat block, adjusted padding).
- `loginserver/config/LoginConfig.java` + `dist/login/config/Server.ini`: `SalvationClientMode` switch (the login protocol carries no client revision, so the mode is server-side).
- `loginserver/network/serverpackets/ServerList.java`: Salvation ServerList tail (0xA4 mask + simplified per-server character counts) while keeping the High Five layout for regular clients.

### Validation performed
- Full clean compile of the source tree (JDK preview build; CI uses the declared JDK).
- Self-test of the generated tables and the shuffler: key mappings (2nd password, bookmark, raid), table ranges, permutation bijectivity, maintain-id stability, seed determinism - all passed.
- Live smoke test: Salvation 140 client passes login -> server list -> gameserver KeyPacket handshake. The remaining blocker was the client-side 32-bit OOM described above, addressed by the `SalvationIsClassic` flag plus standard client-side remedies (Windows pagefile, 4GB/LAA patch on `l2.exe`).

### Next steps
1. Smoke test against the exact Salvation 140 client: login, server list, character selection, Enter World, movement.
2. Fix any packet payload mismatches discovered during the smoke test (the remaining payload differences between High Five and Salvation are handled packet-by-packet).
3. Only after the static path is verified, evaluate `SalvationShuffle` with the target client.


## Full verification against the complete Mobius 5.0 Salvation source (2026-10-28)

The complete Mobius 5.0 Salvation server source (`L2J_Mobius_05.0_Salvation`, 23104 files) was extracted and every packet in the Salvation client chain was diffed against this build. Results:

### Verified identical / correct
- Regular client packets (`ClientPackets`): all common names map to identical ids - 100% compatible.
- Regular server packets (`ServerPackets`): 0 mismatches (252 local entries checked).
- `ExClientPackets` / `ServerPackets` Ex remap tables in `SalvationProtocol.java`: regenerated from the FULL enums (not the earlier 12-file sample) - the shipped tables match the regenerated ones exactly (CM: 132 matched, 0 collisions; SM: 203 matched, 0 collisions). Key mappings confirmed: `REQUEST_EX_2ND_PASSWORD_CHECK` 0xA6 -> 0x93, `EX_2ND_PASSWORD_CHECK` 0xE5 -> 0x105.
- `KeyPacket`: structure matches the reference (result, 8 key bytes, encryption flag, server id, byte 1, obfuscation key, trailing `isClassic` byte).
- `CharSelectionInfo` Salvation branch: header and character entry layouts match the reference byte-for-byte in sizes and order (cosmetic value differences only: clan id vs 0, karma vs reputation, zeros for vitality percent / noble).
- `CharacterSelect` (client) and `CharSelected` Salvation branch: identical to the reference.
- `AuthLogin` read: identical.
- `ServerList` Salvation tail: identical (0xA4 + per-server character counts).
- `Encryption`: same blowfish + running-XOR algorithm (local copy only optimizes byte->int masking).
- `NewCharacterSuccess`: identical structure (only cap values 70/10 vs 99/1 differ).
- Reference default `ServerListType = Free` -> the real Salvation server sends `isClassic = 0` (live data set) by default. Our `SalvationIsClassic = True` is a deliberate deviation to avoid the 32-bit client OOM; the client supports both modes and the flag is configurable without rebuilding.

### Fixed: EnterWorld read order
The Salvation client sends `EnterWorld` fields in a different order than High Five: tracert block (5x4 bytes) FIRST, then 4 unknown ints, a 64-byte array and 1 int (High Five: 32-byte array, 4 ints, 32-byte array, 1 int, tracert last). Total length is the same (104 bytes), so the packet parsed without errors before, but `_tracert` captured misaligned garbage. `EnterWorld.readImpl()` now branches on protocol 140 and reads the Salvation layout, so client IP tracert logging works correctly for Salvation clients.

## Live smoke test: character selection reached (2026-10-28)

The Salvation 140 client completed the full login chain against the patched build: login server auth -> server list (Sieghardt) -> gameserver `PlayerAuthRequest`/session key check -> `auth ok` -> `CharSelectionInfo` delivered (0 characters). The client now reaches the character selection screen; the earlier `Insufficient Memory` crash is gone.

### Dropped Ex packet identified: 0x104 = EX_SEND_CLIENT_INI
Right after `CharSelectionInfo`, the client sent `0xD0 0x0104` twice. In the Mobius 5.0 Salvation reference this is `EX_SEND_CLIENT_INI(0x104, null, ConnectionState.AUTHENTICATED)` - the client uploading its INI settings. The reference server registers it with a **null handler** (the packet is deliberately ignored even by the real Salvation server), so dropping it here is correct behavior and nothing is blocked.

Improvements shipped with this finding:
- `SalvationProtocol.java` now embeds `EX_CM_SALVATION_ONLY_NAMES` - the enum names of all 210 Salvation-only client Ex ids (generated from the reference `ExClientPackets` enum by `scripts/gen_salvation_only_names.py`), so drop logs name the packet instead of a bare id.
- `GamePacketHandler.java` logs known Salvation-only packets as `SALVATION: ignored <NAME> (salvationExId=...) - Salvation-only packet, no server action required`; truly unknown ids keep the generic drop message.

### CharacterCreate verified
`CharacterCreate` read layout is byte-identical between High Five and Salvation (`cSdddddddddddd`: name, race, sex, classId, 6 stat ints, hairStyle, hairColor, face), and `NewCharacter`/`NewCharacterSuccess` was verified earlier. Character creation is expected to work without adapter changes.

## Live smoke test: client stalls at character selection (v1.2, 2026-10-28)

Log signature: `auth ok` -> `CharSelectionInfo sent (1 characters)` -> ~12 seconds of silence -> 2x `ignored EX_SEND_CLIENT_INI (salvationExId=260)` -> `CM 0x0 LOGOUT` -> client disconnect. The full packet tracing shipped in v1.2 proves the client sent nothing else in between: no `CharacterSelect (0x12)`, no `NewCharacter (0x13)` - the character selection screen was rendered but never activated.

### Root cause: missing LOGIN_SUCCESS marker
The reference `LoginServerThread` (PlayerAuthResponse ok branch) sends `LoginFail.LOGIN_SUCCESS` - a `LoginFail` packet with `success = -1, reason = 0` (wire layout: SM `0x0A` + int -1 + int 0) - to the game client immediately BEFORE `CharSelectionInfo`. The High Five build never sent it. Without the marker the Salvation client does not activate the character selection screen: it waits for roughly 12 seconds, uploads its client INI (2x `EX_SEND_CLIENT_INI`), then gives up (`LOGOUT`) and disconnects.

### Fix
- `LoginFail.java`: added the `_success` field, a 2-arg constructor and the `LOGIN_SUCCESS` constant; `writeImpl` writes the success int only for salvation-mode clients, so classic High Five clients keep the legacy single-int layout.
- `LoginServerThread.java`: on successful auth, salvation clients now receive `LoginFail.LOGIN_SUCCESS` right before `CharSelectionInfo`, with a `SALVATION: sent LoginFail.LOGIN_SUCCESS marker` trace line.

Expected next test: the client activates the character selection screen; clicking a character and pressing Start sends `CM 0x12 CHARACTER_SELECT` (visible in the SALVATION trace), followed by `CharSelected` and the already-fixed `EnterWorld` (protocol-140 layout).

## First entry to the world; UserInfo struct graft (phase 3)

With the LOGIN_SUCCESS marker shipped (v1.3) the full selection chain works: the client activated the character selection screen, sent `CM 0x12 CHARACTER_SELECT`, received `CharSelected`, sent `CM 0x11 ENTER_WORLD` and the server switched the client to `IN_GAME` state. The client then crashed with `CachingUserInfoData::decodeStream <- UIPacket <- S_USER_INFO` - the first in-game packet with a drifted struct.

### Root cause
The Essence (Salvation) client expects the **modern mask-based `UserInfo`** (block header = data length + 2, blocks keyed by the `UserInfoType` mask), while the High Five build writes the legacy fixed-layout `UserInfo`. The same drift class affects `ItemList` and `ExQuestItemList`: the Essence client parses the per-item mask layout (`cddcQcchQccddc` + optional augment / elemental / enchant effect blocks).

### Grafted from the reference (byte-exact structs, local API values)
- New mask infrastructure: `AbstractMaskPacket`, `IUpdateTypeComponent`, `UserInfoType`, `ItemListType`.
- `UserInfo`: salvation branch writes all 23 mask blocks; values are mapped to the local Player API. Fields absent in High Five are written as zero: LUC/CHA stats, ability points, magic evasion/accuracy, brooch jewels and agathion charm slots, raidboss points, hair accessory, true hero. Reputation uses karma. `getMCriticalHit` and elemental blocks use the local Creature stats.
- `AbstractItemPacket.writeItem140`: protocol 140 item struct with a per-item mask; the packed High Five augmentation id is split into option1 (`id & 0xFFFF`) and option2 (`id >> 16`) ints, matching the local `AugmentationStatBonus` packing order.
- `ItemList` / `ExQuestItemList`: salvation branches use send type 2 with the new item layout.

Note: the dropped `CM 0xa6` in ENTERING state is correct behavior - in the reference `0xA6` is `REQUEST_SKILL_COOL_TIME` registered for IN_GAME only, and the reference server drops it in ENTERING as well.

## Inventory duplication fix; protocol 140 item/status pipeline (phase 4)

Test of v1.4: the player enters the world, moves and opens the inventory, but every inventory open **accumulates duplicated entries** and the status row shows an inconsistent HP value. Three protocol-140 drifts remained after the phase 3 graft.

### Root causes (all verified against the reference)
1. **ItemList orchestration.** The reference client-server pair never sends a standalone `ItemList` type 2. `Player.sendItemList()` (and `EnterWorld`) send the quartet `ItemList(1)` + `ItemList(2)` + `ExQuestItemList(1)` + `ExQuestItemList(2)`, where send type 1 is a header-only *display/reset window* command (`byte 1, int 0, int size, block`) and send type 2 carries the mask-based item data. Our build sent only the type-2 data packet, so the client kept layering entries on every `REQUEST_ITEM_LIST`.
2. **InventoryUpdate layout.** Protocol 140 prefixes the update list with `byte 0, int 0` and a `writeInt` count, and writes each entry as `short change + mask-based item` (`writeItem140`). Our build wrote the legacy `short count + fixed-struct item` list, garbling the client item cache on every add/modify/remove.
3. **StatusUpdate layout.** Protocol 140 writes `int objectId, int casterId, byte isVisible, byte count` and per attribute `byte id, int value`; legacy writes `int objectId, int count` with `int id, int value` pairs. This mismatch produced the corrupt HP row.

### Changes (grafted byte-exact, local API values)
- `ItemList`: `sendType` 1/2 support (`ItemList(int, Player)` constructor), salvation branch writes the reference layouts; H5 branch untouched. `runImpl` no longer auto-sends `ExQuestItemList` for salvation clients (the explicit pair replaces it).
- `ExQuestItemList`: `sendType` 1/2 support per the reference (both types carry the full mask-based item list).
- `Player.sendItemList(boolean)` / `EnterWorld`: salvation clients receive the reference quartet.
- `AbstractItemPacket.writeItem140(ItemInfo)`: new core overload reused by the inventory update path; the `Item` overload delegates to it.
- `AbstractInventoryUpdate.writeItems(client)`: salvation branch with the 140 header prefix and mask items; `InventoryUpdate` and `PetInventoryUpdate` pass the client through.
- `StatusUpdate`: salvation branch writes the 140 layout; `isVisible` is set when a playable gets a `CUR_HP`/`CUR_MP`/`CUR_CP` attribute (caster id falls back to the object id, matching the no-`addCaster` default of the reference).

Expected next test (v1.5): inventory opens clean on every re-open, no duplicate entries, HP/CP/MP rows consistent; item pickup/equip/drop update correctly (InventoryUpdate path).

## Mask-based NpcInfo; mobs and NPCs now render (phase 5)

Test of v1.5: inventory duplication is gone and status bars are correct, but **no mobs or NPCs are visible** in the world. The Essence client parses `NpcInfo` (0x16) with the modern 37-bit mask layout (`NpcInfoType` components, init block + data block), while the High Five build writes the legacy fixed CT2.6 layout - the client silently drops every NPC packet.

### Grafted from the reference
- New `network/enums/NpcInfoType.java` (37 components with mask ids and block lengths), matching our `IUpdateTypeComponent`.
- `AbstractNpcInfo.NpcInfo.writeImpl`: salvation branch builds the reference mask set at write time (ATTACKABLE, RELATIONS, TITLE, ID, POSITION, HEADING, ATK_CAST_SPEED, SPEED_MULTIPLIER, EQUIPPED, STOP_MODE, MOVE_MODE, SWIM_OR_FLY, TEAM, ENCHANT, FLYING, PET_EVOLUTION_ID, DISPLAY_EFFECT, HP/MP blocks, SUMMONED, NAME, CLAN, VISUAL_STATE) and writes the exact reference block order. Local specifics kept: level/aggression champion titles, castle clan crests, server-side name/title flags.
- Not ported (no local data): ABNORMALS visual effect list (H5 `AbnormalVisualEffect` is a bitmask without client ids), NPCString name/title ids, clone/transformation/follow blocks. Summons and traps still use the legacy layout (`SummonInfo`/`TrapInfo`) - they will be ported if they ever apply to salvation clients.

### Client crash with a third-party system folder
A test with a different `system` folder crashed the client after server select inside `FL2OneDayRewardSystem::Serialize <- LoadAlternateGameData <- OnChangeServerType` (client build dated 2018). This is a client-side DAT mismatch (old/incompatible `OneDayRewardSetting.dat` in that repack), not a server issue: our `KeyPacket` layout matches the reference (result, key, encryption, server id, obfuscation key, isClassic byte). Use the system folder that matches the working client build.

## Full opcode-table alignment; ExUserInfo set; packet format branches (phase 5.2)

Test of v1.6: mobs and NPCs render and can be attacked, but show **no names**; teleporting into a city produces a **black screen** and the character disappears; the skill window stays empty. A full diff of the reference build against ours exposed a systemic protocol drift that piecemeal fixes could never cover.

### Root causes
1. **The High Five and Salvation-140 server opcode tables differ systematically.** Of the 485 packet constants in our `ServerPackets` enum, 146 share their reference name but carry a different wire id (`SkillList 0x5F -> 0x40`, `ExQuestItemList 0xC6 -> 0xC7`, `ExBasicActionList 0x5F -> 0x60`, plus ~143 Ex-packets shifted by one). The 140 client silently dropped every mismatched packet - the skill list response never arrived (the client kept re-sending `REQUEST_SKILL_LIST`), quest items and the basic action list never loaded.
2. **NPC names.** The reference datapack does not enable `usingServerSideName` either - the stock 140 client resolves NPC names from its own DAT files. On the tested client install those names did not render, so the server now always sends the `NpcInfoType.NAME` component for salvation clients.
3. **Teleport/visibility tails.** `TeleportToLocation` was 4 bytes short (missing the trailing "unknown" int), `ValidateLocation` missed the heading-validation byte, `DeleteObject` wrote an int where 140 expects a byte, `SpawnItem` lacked the 3-byte enchant/augment/abilities tail.
4. **Equipment paperdoll.** The 140 client draws the paperdoll exclusively from `ExUserInfoEquipSlot` (`0xFE 0x156`), sent by the reference at enter-world and on every equip change. Our build never sent it - hence "equipment cells do not match".

### Changes
- **Central opcode override mechanism.** `ServerPacket` exposes `getCurrentWritingClient()` (ThreadLocal, set/cleared around `writeImpl`); the generated `ServerPacketsSalvation` map (146 entries, diffed from the reference enum) is consulted by `ServerPackets.writeId` whenever the receiving client is in salvation mode. Ten new enum constants (`EX_USER_INFO_EQUIP_SLOT`, `EX_SUBJOB_INFO`, ...) carry their 140 ids directly.
- **New packets ported from the reference**: `ExUserInfoEquipSlot` (38-slot mask layout, mapped onto our paperdoll indices; agathion/brooch slots write zeros), `ExUserInfoInvenWeight`, `ExAdenaInvenCount`, `ExUserInfoCubic`, `ExUserInfoAbnormalVisualEffect`, `ExSubjobInfo`, `ExVitalityEffectInfo`, `ExWorldChatCnt`, `InventorySlot` enum, `SalvationAveMap` (49 common H5 abnormal-visual-effect names mapped to 140 client ids).
- **EnterWorld** now sends the ExUserInfo set after `ShortcutInit`; `Inventory.setPaperdollItem` broadcasts `ExUserInfoEquipSlot` on every equip/unequip.
- **Full 140 wire branches** for `Attack` (inline first hit, int flags + grade), `MagicSkillUse` (casting type head, reuse group, action id tail), `ShortcutInit` (item augment/visual ints, skill short level + sub level), `SkillList` (short level/sub level, reuse delay group, tail), `HennaInfo` (short stats + premium block), `RelationChanged` (byte mask header, byte entry flags), `ExStorageMaxCount` (12 ints), `CreatureSay` (whisper relation tail), `Die` (7 teleport flags + hide animation + items), `CharInfo` (complete 140 branch for other players).
- `NpcInfo`: the NAME component is always included for salvation clients.

Expected next test (v1.7): NPC/mob names visible, teleport lands clean, equipment occupies correct paperdoll cells, the skill window lists skills, other players render correctly.
