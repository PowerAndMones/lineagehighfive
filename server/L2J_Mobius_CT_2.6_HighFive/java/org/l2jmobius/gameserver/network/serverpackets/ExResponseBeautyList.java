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

import java.util.Map;

import org.l2jmobius.commons.network.buffer.WriteBuffer;
import org.l2jmobius.gameserver.data.xml.BeautyShopData;
import org.l2jmobius.gameserver.entity.actor.Player;
import org.l2jmobius.gameserver.entity.actor.enums.player.Sex;
import org.l2jmobius.gameserver.entity.beautyshop.BeautyItem;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * List of available styles of one type (hair or face).
 * @author Sdw
 */
public class ExResponseBeautyList extends ServerPacket
{
	public static final int SHOW_FACESHAPE = 1;
	public static final int SHOW_HAIRSTYLE = 0;
	
	private final Player _player;
	private final int _type;
	private final Map<Integer, BeautyItem> _beautyItem;
	
	public ExResponseBeautyList(Player player, int type)
	{
		_player = player;
		_type = type;
		
		final Sex sex = player.getAppearance().isFemale() ? Sex.FEMALE : Sex.MALE;
		if (type == SHOW_HAIRSTYLE)
		{
			_beautyItem = BeautyShopData.getInstance().getBeautyData(player.getRace(), sex).getHairList();
		}
		else
		{
			_beautyItem = BeautyShopData.getInstance().getBeautyData(player.getRace(), sex).getFaceList();
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WriteBuffer buffer)
	{
		ServerPackets.EX_RESPONSE_BEAUTY_LIST.writeId(this, buffer);
		buffer.writeLong(_player.getAdena());
		buffer.writeLong(_player.getBeautyTickets());
		buffer.writeInt(_type);
		buffer.writeInt(_beautyItem.size());
		for (BeautyItem item : _beautyItem.values())
		{
			buffer.writeInt(item.getId());
			buffer.writeInt(1); // Limit
		}
		
		buffer.writeInt(0);
	}
}
