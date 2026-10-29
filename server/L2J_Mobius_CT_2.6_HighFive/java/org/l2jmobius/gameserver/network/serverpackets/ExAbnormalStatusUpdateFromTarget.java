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
import org.l2jmobius.gameserver.entity.actor.Creature;
import org.l2jmobius.gameserver.mechanics.skill.BuffInfo;
import org.l2jmobius.gameserver.mechanics.skill.Skill;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * Salvation (protocol 140) buff bar of the currently selected target.
 * Ported from the Mobius 5.0 Salvation reference (EX 0xE6). The Salvation
 * client keeps the buff icons with countdown timers next to the target frame
 * fed by this packet; without it the bar stays empty or shows 0 seconds.
 * @author Mobius
 */
public class ExAbnormalStatusUpdateFromTarget extends ServerPacket
{
	private final Creature _creature;
	private final List<BuffInfo> _effects = new ArrayList<>();
	
	public ExAbnormalStatusUpdateFromTarget(Creature creature)
	{
		_creature = creature;
		for (BuffInfo info : creature.getEffectList().getEffects())
		{
			if ((info != null) && info.isInUse() && !info.getSkill().isToggle())
			{
				_effects.add(info);
			}
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WriteBuffer buffer)
	{
		ServerPackets.EX_ABNORMAL_STATUS_UPDATE_FROM_TARGET.writeId(this, buffer);
		buffer.writeInt(_creature.getObjectId());
		buffer.writeShort(_effects.size());
		for (BuffInfo info : _effects)
		{
			final Skill skill = info.getSkill();
			buffer.writeInt(skill.getDisplayId());
			buffer.writeShort(skill.getDisplayLevel());
			buffer.writeShort(0); // skill sub level
			buffer.writeShort(skill.getAbnormalType() != null ? skill.getAbnormalType().getClientId() : 0);
			final int time = info.getTime();
			// Salvation (protocol 140) optional int: plain short, or short(32767) + int.
			if (time >= Short.MAX_VALUE)
			{
				buffer.writeShort(Short.MAX_VALUE);
				buffer.writeInt(time);
			}
			else
			{
				buffer.writeShort(time);
			}
			buffer.writeInt(info.getEffector() != null ? info.getEffector().getObjectId() : 0);
		}
	}
}
