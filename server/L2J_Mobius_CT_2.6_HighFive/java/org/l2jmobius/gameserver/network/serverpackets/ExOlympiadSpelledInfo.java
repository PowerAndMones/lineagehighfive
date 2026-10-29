/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.buffer.WriteBuffer;
import org.l2jmobius.gameserver.entity.actor.Player;
import org.l2jmobius.gameserver.mechanics.skill.BuffInfo;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * @author godson
 */
public class ExOlympiadSpelledInfo extends ServerPacket
{
	private final int _playerId;
	private final List<BuffInfo> _effects = new ArrayList<>();
	
	public ExOlympiadSpelledInfo(Player player)
	{
		_playerId = player.getObjectId();
	}
	
	public void addSkill(BuffInfo info)
	{
		_effects.add(info);
	}
	
	@Override
	public void writeImpl(GameClient client, WriteBuffer buffer)
	{
		ServerPackets.EX_OLYMPIAD_SPELLED_INFO.writeId(this, buffer);
		if ((client != null) && client.isSalvationMode())
		{
			buffer.writeInt(_playerId);
			buffer.writeInt(_effects.size());
			for (BuffInfo info : _effects)
			{
				if ((info != null) && info.isInUse())
				{
					buffer.writeInt(info.getSkill().getDisplayId());
					buffer.writeShort(info.getSkill().getDisplayLevel());
					buffer.writeShort(0); // skill sub level
					buffer.writeInt(info.getSkill().getAbnormalType() != null ? info.getSkill().getAbnormalType().getClientId() : 0);
					final int time = info.getSkill().isToggle() ? -1 : info.getTime();
					if ((time >= 0) && (time < Short.MAX_VALUE))
					{
						buffer.writeByte(1);
						buffer.writeShort(time);
					}
					else
					{
						buffer.writeByte(0);
						buffer.writeInt(time);
					}
				}
			}
			return;
		}

		buffer.writeInt(_playerId);
		buffer.writeInt(_effects.size());
		for (BuffInfo info : _effects)
		{
			if ((info != null) && info.isInUse())
			{
				buffer.writeInt(info.getSkill().getDisplayId());
				buffer.writeShort(info.getSkill().getDisplayLevel());
				buffer.writeInt(info.getTime());
			}
		}
	}
}
