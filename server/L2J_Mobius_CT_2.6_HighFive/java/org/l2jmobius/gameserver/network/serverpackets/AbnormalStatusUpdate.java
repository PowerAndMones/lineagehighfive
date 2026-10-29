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
import org.l2jmobius.gameserver.mechanics.skill.BuffInfo;
import org.l2jmobius.gameserver.mechanics.skill.Skill;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;

public class AbnormalStatusUpdate extends ServerPacket
{
	private final List<BuffInfo> _effects = new ArrayList<>();
	
	public void addSkill(BuffInfo info)
	{
		if (!info.getSkill().isHealingPotionSkill())
		{
			_effects.add(info);
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WriteBuffer buffer)
	{
		ServerPackets.ABNORMAL_STATUS_UPDATE.writeId(this, buffer);
		if ((client != null) && client.isSalvationMode())
		{
			// SALVATION_140_LAYOUT: sub level + abnormal type + optional time.
			buffer.writeShort(_effects.size());
			for (BuffInfo info : _effects)
			{
				if ((info != null) && info.isInUse())
				{
					final Skill skill = info.getSkill();
					buffer.writeInt(skill.getDisplayId());
					buffer.writeShort(skill.getDisplayLevel());
					buffer.writeShort(0); // skill sub level
					buffer.writeInt(skill.getAbnormalType() != null ? skill.getAbnormalType().getClientId() : 0);
					final int salvationTime = skill.isToggle() ? -1 : info.getTime();
					// Salvation (protocol 140) optional int: plain short, or short(32767) + int.
					if (salvationTime >= Short.MAX_VALUE)
					{
						buffer.writeShort(Short.MAX_VALUE);
						buffer.writeInt(salvationTime);
					}
					else
					{
						buffer.writeShort(salvationTime);
					}
				}
			}
			return;
		}

		buffer.writeShort(_effects.size());
		for (BuffInfo info : _effects)
		{
			if ((info != null) && info.isInUse())
			{
				final Skill skill = info.getSkill();
				buffer.writeInt(skill.getDisplayId());
				buffer.writeShort(skill.getDisplayLevel());
				buffer.writeInt(info.getTime());
			}
		}
	}
}
