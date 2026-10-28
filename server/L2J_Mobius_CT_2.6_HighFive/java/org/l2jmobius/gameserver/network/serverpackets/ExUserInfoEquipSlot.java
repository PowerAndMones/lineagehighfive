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

import org.l2jmobius.commons.network.buffer.WriteBuffer;
import org.l2jmobius.gameserver.entity.actor.Player;
import org.l2jmobius.gameserver.entity.itemcontainer.PlayerInventory;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;
import org.l2jmobius.gameserver.network.enums.InventorySlot;

/**
 * Salvation (protocol 140): per-slot equipment state for the receiving player.
 * Required for the 140 client to render the paperdoll correctly.
 * @author Mobius
 */
public class ExUserInfoEquipSlot extends ServerPacket
{
	private final Player _player;
	private final byte[] _masks =
	{
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00
	};
	
	public ExUserInfoEquipSlot(Player player)
	{
		_player = player;
		for (InventorySlot slot : InventorySlot.values())
		{
			_masks[slot.getMask() >> 3] |= DEFAULT_FLAG_ARRAY[slot.getMask() & 7];
		}
	}
	
	protected static final byte[] DEFAULT_FLAG_ARRAY =
	{
		(byte) 0x01,
		(byte) 0x02,
		(byte) 0x04,
		(byte) 0x08,
		(byte) 0x10,
		(byte) 0x20,
		(byte) 0x40,
		(byte) 0x80
	};
	
	@Override
	public void writeImpl(GameClient client, WriteBuffer buffer)
	{
		ServerPackets.EX_USER_INFO_EQUIP_SLOT.writeId(this, buffer);
		buffer.writeInt(_player.getObjectId());
		buffer.writeShort(InventorySlot.values().length);
		buffer.writeBytes(_masks);
		final PlayerInventory inventory = _player.getInventory();
		for (InventorySlot slot : InventorySlot.values())
		{
			if ((slot.getMask() >> 3) < _masks.length)
			{
				buffer.writeShort(22); // 10 + 4 * 3
				if (slot.getSlot() >= 0)
				{
					final int augmentationId = inventory.getPaperdollAugmentationId(slot.getSlot());
					buffer.writeInt(inventory.getPaperdollObjectId(slot.getSlot()));
					buffer.writeInt(inventory.getPaperdollItemId(slot.getSlot()));
					buffer.writeInt(augmentationId); // Option 1
					buffer.writeInt(0); // Option 2
					buffer.writeInt(0); // Visual id
				}
				else
				{
					buffer.writeInt(0);
					buffer.writeInt(0);
					buffer.writeInt(0);
					buffer.writeInt(0);
					buffer.writeInt(0);
				}
			}
		}
	}
}
