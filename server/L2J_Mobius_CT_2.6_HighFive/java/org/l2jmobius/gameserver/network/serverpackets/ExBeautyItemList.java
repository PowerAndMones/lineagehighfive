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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.commons.network.buffer.WriteBuffer;
import org.l2jmobius.gameserver.data.xml.BeautyShopData;
import org.l2jmobius.gameserver.entity.actor.Player;
import org.l2jmobius.gameserver.entity.actor.enums.player.Sex;
import org.l2jmobius.gameserver.entity.beautyshop.BeautyData;
import org.l2jmobius.gameserver.entity.beautyshop.BeautyItem;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;

/**
 * Full beauty shop price list (hair/face/color blocks).
 * @author Sdw
 */
public class ExBeautyItemList extends ServerPacket
{
	private static final int HAIR_TYPE = 0;
	private static final int FACE_TYPE = 1;
	private static final int COLOR_TYPE = 2;
	
	private int _colorCount;
	private final BeautyData _beautyData;
	private final Map<Integer, List<BeautyItem>> _colorData = new HashMap<>();
	
	public ExBeautyItemList(Player player)
	{
		final Sex sex = player.getAppearance().isFemale() ? Sex.FEMALE : Sex.MALE;
		_beautyData = BeautyShopData.getInstance().getBeautyData(player.getRace(), sex);
		for (BeautyItem hair : _beautyData.getHairList().values())
		{
			final List<BeautyItem> colors = new ArrayList<>();
			for (BeautyItem color : hair.getColors().values())
			{
				colors.add(color);
				_colorCount++;
			}
			
			_colorData.put(hair.getId(), colors);
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WriteBuffer buffer)
	{
		ServerPackets.EX_BEAUTY_ITEM_LIST.writeId(this, buffer);
		buffer.writeInt(HAIR_TYPE);
		buffer.writeInt(_beautyData.getHairList().size());
		for (BeautyItem hair : _beautyData.getHairList().values())
		{
			buffer.writeInt(0); // ?
			buffer.writeInt(hair.getId());
			buffer.writeInt(hair.getAdena());
			buffer.writeInt(hair.getResetAdena());
			buffer.writeInt(hair.getBeautyShopTicket());
			buffer.writeInt(1); // Limit
		}
		
		buffer.writeInt(FACE_TYPE);
		buffer.writeInt(_beautyData.getFaceList().size());
		for (BeautyItem face : _beautyData.getFaceList().values())
		{
			buffer.writeInt(0); // ?
			buffer.writeInt(face.getId());
			buffer.writeInt(face.getAdena());
			buffer.writeInt(face.getResetAdena());
			buffer.writeInt(face.getBeautyShopTicket());
			buffer.writeInt(1); // Limit
		}
		
		buffer.writeInt(COLOR_TYPE);
		buffer.writeInt(_colorCount);
		for (Entry<Integer, List<BeautyItem>> entry : _colorData.entrySet())
		{
			for (BeautyItem color : entry.getValue())
			{
				buffer.writeInt(entry.getKey());
				buffer.writeInt(color.getId());
				buffer.writeInt(color.getAdena());
				buffer.writeInt(color.getResetAdena());
				buffer.writeInt(color.getBeautyShopTicket());
				buffer.writeInt(1);
			}
		}
	}
}
