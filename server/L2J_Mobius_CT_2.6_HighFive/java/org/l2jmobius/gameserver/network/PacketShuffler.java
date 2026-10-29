/*
 * Copyright (c) 2013 L2jMobius
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.l2jmobius.gameserver.network;

/**
 * Deterministic opcode shuffler for Salvation-family clients (protocol 140).<br>
 * When the KeyPacket carries a non-zero obfuscation seed, the client shuffles
 * the opcodes of the packets it sends. This class reproduces the client's
 * permutation so incoming shuffled opcodes can be translated back to the real
 * ids before dispatch. Server-to-client opcodes are not shuffled.<br>
 * The implementation follows the publicly documented L2J Mobius
 * Salvation/Arena shuffler (MrKirill1232) and operates over the Salvation
 * opcode space sizes, since the permutation ranges must match the client's
 * own packet tables.
 * @author MrKirill1232, Mobius
 */
public class PacketShuffler
{
        /** First-opcode (client packet) space size of the Salvation client (max 0xD0). */
        private static final int CM_SIZE = 0xD1;
        
        /** Extended-opcode (0xD0 xx) space size of the Salvation client (max 0x15F). */
        private static final int EX_CM_SIZE = 0x160;
        
        /**
         * Client packet ids that must keep their real id on the wire so the
         * handshake-critical flow stays dispatchable while shuffling initializes.
         */
        private static final int[] MAINTAIN_IDS =
        {
                ClientPackets.ENTER_WORLD.getPacketId(), // 0x11
                ClientPackets.CHARACTER_SELECT.getPacketId(), // 0x12
                ClientPackets.NET_PING.getPacketId(), // 0xB1
                ClientPackets.EX_PACKET.getPacketId() // 0xD0
        };
        
        /**
         * Extended ids kept unshuffled by the client (Salvation space ids of the
         * raid-server connect/return packets).
         */
        private static final int[] MAINTAIN_EX_IDS =
        {
                0x70, // Salvation id of ExClientPackets.EX_CONNECT_TO_RAID_SERVER
                0x71 // Salvation id of ExClientPackets.EX_RETURN_FROM_RAID_SERVER
        };
        
        private final int[] _state = new int[16];
        private int _stateIndex;
        private final int[] _idsMap = new int[CM_SIZE];
        private final int[] _exIdsMap = new int[EX_CM_SIZE];
        private boolean _enabled;
        
        /**
         * Builds the shuffled id maps for the given non-zero seed.<br>
         * The identical seed must be written to the KeyPacket obfuscation key field.
         * @param seed the non-zero obfuscation seed announced to the client
         */
        public void makeShuffledPacket(int seed)
        {
                for (int i = 0; i < _idsMap.length; i++)
                {
                        _idsMap[i] = i;
                }
                
                for (int i = 0; i < _exIdsMap.length; i++)
                {
                        _exIdsMap[i] = i;
                }
                
                _stateIndex = 0;
                int currentSeed = seed;
                for (int i = 0; i < 16; i++)
                {
                        _state[i] = currentSeed;
                        currentSeed = (2 * currentSeed) + 51;
                }
                
                doShuffle(_idsMap);
                doShuffle(_exIdsMap);
                
                for (int id : MAINTAIN_IDS)
                {
                        maintainPacket(_idsMap, id);
                }
                
                for (int id : MAINTAIN_EX_IDS)
                {
                        maintainPacket(_exIdsMap, id);
                }
                
                _enabled = true;
        }
        
        /**
         * Indicates whether shuffled opcodes are expected from the client.
         * @return true when the maps have been initialized with a non-zero seed
         */
        public boolean isEnabled()
        {
                return _enabled;
        }
        
        /**
         * Translates a shuffled first opcode received from the client back to the
         * real Salvation opcode id (which is then remapped to the local table by
         * {@link SalvationProtocol}).
         * @param shuffledId the unsigned opcode byte read from the wire
         * @return the real opcode id, or the input value when out of range
         */
        public int getOriginalId(int shuffledId)
        {
                if ((shuffledId < 0) || (shuffledId >= _idsMap.length))
                {
                        return shuffledId;
                }
                
                return _idsMap[shuffledId];
        }
        
        /**
         * Translates a shuffled extended opcode (second byte after 0xD0) received
         * from the client back to the real Salvation extended opcode id.
         * @param shuffledExId the unsigned extended opcode read from the wire
         * @return the real extended opcode id, or the input value when out of range
         */
        public int getOriginalExId(int shuffledExId)
        {
                if ((shuffledExId < 0) || (shuffledExId >= _exIdsMap.length))
                {
                        return shuffledExId;
                }
                
                return _exIdsMap[shuffledExId];
        }
        
        private void doShuffle(int[] list)
        {
                final int nLength = list.length;
                for (int i = 2; i <= nLength; i++)
                {
                        final int mIdx = _stateIndex;
                        final int s0 = _state[mIdx];
                        final int s3 = _state[(mIdx - 3) & 0xF];
                        final int s7 = _state[(mIdx - 7) & 0xF];
                        final int v8 = s0 ^ s3 ^ ((s3 ^ (s0 << 1)) << 15);
                        final int v9 = (s7 >>> 11) ^ s7;
                        _state[mIdx] = v8 ^ v9;
                        _stateIndex = (_stateIndex - 1) & 0xF;
                        final int nextIdx = _stateIndex;
                        final int magic = (v8 ^ v9) & 0xFED22169;
                        _state[nextIdx] ^= v9 ^ (magic << 5) ^ (4 * (_state[nextIdx] ^ ((v8 ^ (v9 << 10)) << 16)));
                        final long unsignedStateValue = Integer.toUnsignedLong(_state[_stateIndex]);
                        final int swapIdx = (int) (unsignedStateValue % i);
                        final int currIdx = i - 1;
                        final int temp = list[currIdx];
                        list[currIdx] = list[swapIdx];
                        list[swapIdx] = temp;
                }
        }
        
        private void maintainPacket(int[] map, int targetId)
        {
                if ((targetId < 0) || (targetId >= map.length))
                {
                        return;
                }
                
                for (int i = 0; i < map.length; i++)
                {
                        if (map[i] == targetId)
                        {
                                final int temp = map[i];
                                map[i] = map[targetId];
                                map[targetId] = temp;
                                break;
                        }
                }
        }
}
