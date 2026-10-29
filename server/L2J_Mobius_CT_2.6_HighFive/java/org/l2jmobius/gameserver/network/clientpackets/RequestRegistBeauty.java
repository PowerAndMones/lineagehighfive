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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.gameserver.data.xml.BeautyShopData;
import org.l2jmobius.gameserver.entity.actor.Player;
import org.l2jmobius.gameserver.entity.actor.enums.player.Sex;
import org.l2jmobius.gameserver.entity.beautyshop.BeautyData;
import org.l2jmobius.gameserver.entity.beautyshop.BeautyItem;
import org.l2jmobius.gameserver.entity.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.network.serverpackets.ExResponseBeautyList;
import org.l2jmobius.gameserver.network.serverpackets.ExResponseBeautyRegistReset;

/**
 * Buys a new visual style (hair/color/face).
 * @author Sdw
 */
public class RequestRegistBeauty extends ClientPacket
{
	private int _hairId;
	private int _faceId;
	private int _colorId;
	
	@Override
	protected void readImpl()
	{
		_hairId = readInt();
		_faceId = readInt();
		_colorId = readInt();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Sex sex = player.getAppearance().isFemale() ? Sex.FEMALE : Sex.MALE;
		final BeautyData beautyData = BeautyShopData.getInstance().getBeautyData(player.getRace(), sex);
		if (beautyData == null)
		{
			return;
		}
		
		int requiredAdena = 0;
		int requiredBeautyShopTicket = 0;
		if (_hairId > 0)
		{
			final BeautyItem hair = beautyData.getHairList().get(_hairId);
			if (hair == null)
			{
				player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.FAILURE));
				player.sendPacket(new ExResponseBeautyList(player, ExResponseBeautyList.SHOW_FACESHAPE));
				return;
			}
			
			if (hair.getId() != player.getVisualHair())
			{
				requiredAdena += hair.getAdena();
				requiredBeautyShopTicket += hair.getBeautyShopTicket();
			}
			
			if (_colorId > 0)
			{
				final BeautyItem color = hair.getColors().get(_colorId);
				if (color == null)
				{
					player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.FAILURE));
					player.sendPacket(new ExResponseBeautyList(player, ExResponseBeautyList.SHOW_FACESHAPE));
					return;
				}
				
				requiredAdena += color.getAdena();
				requiredBeautyShopTicket += color.getBeautyShopTicket();
			}
		}
		
		if ((_faceId > 0) && (_faceId != player.getVisualFace()))
		{
			final BeautyItem face = beautyData.getFaceList().get(_faceId);
			if (face == null)
			{
				player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.FAILURE));
				player.sendPacket(new ExResponseBeautyList(player, ExResponseBeautyList.SHOW_FACESHAPE));
				return;
			}
			
			requiredAdena += face.getAdena();
			requiredBeautyShopTicket += face.getBeautyShopTicket();
		}
		
		if ((player.getAdena() < requiredAdena) || (player.getBeautyTickets() < requiredBeautyShopTicket))
		{
			player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.FAILURE));
			player.sendPacket(new ExResponseBeautyList(player, ExResponseBeautyList.SHOW_FACESHAPE));
			return;
		}
		
		if ((requiredAdena > 0) && !player.reduceAdena(ItemProcessType.FEE, requiredAdena, null, true))
		{
			player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.FAILURE));
			player.sendPacket(new ExResponseBeautyList(player, ExResponseBeautyList.SHOW_FACESHAPE));
			return;
		}
		
		if ((requiredBeautyShopTicket > 0) && !player.reduceBeautyTickets(ItemProcessType.FEE, requiredBeautyShopTicket, null, true))
		{
			player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.FAILURE));
			player.sendPacket(new ExResponseBeautyList(player, ExResponseBeautyList.SHOW_FACESHAPE));
			return;
		}
		
		if (_hairId > 0)
		{
			player.setVisualHair(_hairId);
		}
		
		if (_colorId > 0)
		{
			player.setVisualHairColor(_colorId);
		}
		
		if (_faceId > 0)
		{
			player.setVisualFace(_faceId);
		}
		
		player.sendPacket(new ExResponseBeautyRegistReset(player, ExResponseBeautyRegistReset.CHANGE, ExResponseBeautyRegistReset.SUCCESS));
	}
}
