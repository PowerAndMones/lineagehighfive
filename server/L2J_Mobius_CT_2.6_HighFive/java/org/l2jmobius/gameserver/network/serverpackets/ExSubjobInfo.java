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
import org.l2jmobius.gameserver.entity.actor.Player;
import org.l2jmobius.gameserver.entity.actor.holders.player.SubClassHolder;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * Salvation (protocol 140): subclass overview. Sent with NO_CHANGES type.
 * @author Sdw
 */
public class ExSubjobInfo extends ServerPacket
{
	private final int _currClassId;
	private final int _currRace;
	private final int _type;
	private final List<SubInfo> _subs;
	
	public ExSubjobInfo(Player player)
	{
		_currClassId = player.getPlayerClass().getId();
		_currRace = player.getRace().ordinal();
		_type = 0; // NO_CHANGES
		_subs = new ArrayList<>();
		_subs.add(new SubInfo(player));
		for (SubClassHolder sub : player.getSubClasses().values())
		{
			_subs.add(new SubInfo(sub));
		}
	}
	
	private static class SubInfo
	{
		private final int _index;
		private final int _classId;
		private final int _level;
		private final int _type;
		
		SubInfo(SubClassHolder sub)
		{
			_index = sub.getClassIndex();
			_classId = sub.getId();
			_level = sub.getLevel();
			_type = 2; // SUBCLASS
		}
		
		SubInfo(Player player)
		{
			_index = 0;
			_classId = player.getBaseClass();
			_level = player.getLevel();
			_type = 0; // BASECLASS
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WriteBuffer buffer)
	{
		ServerPackets.EX_SUBJOB_INFO.writeId(this, buffer);
		buffer.writeByte(_type);
		buffer.writeInt(_currClassId);
		buffer.writeInt(_currRace);
		buffer.writeInt(_subs.size());
		for (SubInfo sub : _subs)
		{
			buffer.writeInt(sub._index);
			buffer.writeInt(sub._classId);
			buffer.writeInt(sub._level);
			buffer.writeByte(sub._type);
		}
	}
}
