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
import org.l2jmobius.gameserver.entity.item.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;
	
public class ItemList extends AbstractItemPacket
{
	private final int _sendType;
	private final Player _player;
	private final boolean _showWindow;
	private final List<Item> _items = new ArrayList<>();
	
	public ItemList(int sendType, Player player)
	{
		_sendType = sendType;
		_player = player;
		_showWindow = false;
		for (Item item : player.getInventory().getItems())
		{
			if (!item.isQuestItem())
			{
				_items.add(item);
			}
		}
	}
	
	public ItemList(Player player, boolean showWindow)
	{
		_sendType = showWindow ? 1 : 2;
		_player = player;
		_showWindow = showWindow;
		for (Item item : player.getInventory().getItems())
		{
			if (!item.isQuestItem())
			{
				_items.add(item);
			}
		}
	}
	
	@Override
	public void writeImpl(GameClient client, WriteBuffer buffer)
	{
		if ((client != null) && client.isSalvationMode())
		{
			// Salvation (protocol 140) ItemList layout:
			// sendType 1 = display inventory window (client uses its cached items).
			// sendType 2 = full item data sync (mask-based per item).
			ServerPackets.ITEM_LIST.writeId(this, buffer);
			buffer.writeByte(_sendType);
			if (_sendType == 2)
			{
				buffer.writeInt(_items.size());
				buffer.writeInt(_items.size());
				for (Item item : _items)
				{
					writeItem140(item, buffer);
				}
			}
			else
			{
				buffer.writeInt(0);
				buffer.writeInt(_items.size());
			}
			writeInventoryBlock(_player.getInventory(), buffer);
			return;
		}
	
		ServerPackets.ITEM_LIST.writeId(this, buffer);
		buffer.writeShort(_showWindow);
		buffer.writeShort(_items.size());
		for (Item item : _items)
		{
			writeItem(item, buffer);
		}
	
		writeInventoryBlock(_player.getInventory(), buffer);
	}
	
	@Override
	public void runImpl(Player player)
	{
		// In salvation mode the ExQuestItemList pair is sent explicitly with the ItemList pair.
		if (player != null)
		{
			final GameClient client = player.getClient();
			if ((client == null) || !client.isSalvationMode())
			{
				player.sendPacket(new ExQuestItemList(_player));
			}
		}
	}
}
