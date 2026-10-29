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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.buffer.WriteBuffer;
import org.l2jmobius.gameserver.entity.actor.Npc;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * Salvation (protocol 140) light-weight NPC abnormal visual effect update.
 * Ported from the Mobius 5.0 Salvation reference (opcode 0x4B). Sent instead
 * of a full NpcInfo re-send when only the visual effects of an NPC change.
 * @author Sdw, Mobius
 */
public class NpcInfoAbnormalVisualEffect extends ServerPacket
{
        private final Npc _npc;
        private final List<Short> _abnormalClientIds;
        
        public NpcInfoAbnormalVisualEffect(Npc npc)
        {
                _npc = npc;
                // Salvation (protocol 140) abnormal visual effects as short client ids.
                _abnormalClientIds = SalvationAveMap.toClientIds(npc.getAbnormalVisualEffects() | npc.getAbnormalVisualEffectSpecial(), npc.isInvisible());
        }
        
        @Override
        public void writeImpl(GameClient client, WriteBuffer buffer)
        {
                ServerPackets.NPC_INFO_ABNORMAL_VISUAL_EFFECT.writeId(this, buffer);
                buffer.writeInt(_npc.getObjectId());
                buffer.writeInt(0); // Transformation display id: not present on the High Five data model.
                buffer.writeShort(_abnormalClientIds.size());
                for (short aveClientId : _abnormalClientIds)
                {
                        buffer.writeShort(aveClientId);
                }
        }
}
