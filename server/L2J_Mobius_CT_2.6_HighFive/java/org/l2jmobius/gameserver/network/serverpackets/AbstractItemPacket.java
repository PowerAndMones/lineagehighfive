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

import org.l2jmobius.commons.network.buffer.WriteBuffer;
import org.l2jmobius.gameserver.entity.item.holders.ItemInfo;
import org.l2jmobius.gameserver.entity.item.instance.Item;
import org.l2jmobius.gameserver.entity.itemcontainer.PlayerInventory;
import org.l2jmobius.gameserver.network.holders.TradeItem;
import org.l2jmobius.gameserver.network.enums.ItemListType;

/**
 * @author UnAfraid
 */
public abstract class AbstractItemPacket extends ServerPacket
{
	protected void writeItem(TradeItem item, WriteBuffer buffer)
	{
		writeItem(new ItemInfo(item), buffer);
	}
	
	protected void writeItem(Item item, WriteBuffer buffer)
	{
		writeItem(new ItemInfo(item), buffer);
	}
	
	protected void writeTradeItem(TradeItem item, WriteBuffer buffer)
	{
		buffer.writeShort(item.getItem().getType1());
		buffer.writeInt(item.getObjectId()); // ObjectId
		buffer.writeInt(item.getItem().getDisplayId()); // ItemId
		buffer.writeLong(item.getCount()); // Quantity
		buffer.writeShort(item.getItem().getType2()); // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item
		buffer.writeShort(item.getCustomType1()); // Filler (always 0)
		buffer.writeInt(item.getItem().getBodyPart().getMask()); // Slot : 0006-lr.ear, 0008-neck, 0030-lr.finger, 0040-head, 0100-l.hand, 0200-gloves, 0400-chest, 0800-pants, 1000-feet, 4000-r.hand, 8000-r.hand
		buffer.writeShort(item.getEnchant()); // Enchant level (pet level shown in control item).
		buffer.writeShort(0); // Equipped : 00-No, 01-yes
		buffer.writeShort(item.getCustomType2());
		writeItemElementalAndEnchant(new ItemInfo(item), buffer);
	}
	
	protected void writeItem(ItemInfo item, WriteBuffer buffer)
	{
		buffer.writeInt(item.getObjectId()); // ObjectId
		buffer.writeInt(item.getItem().getDisplayId()); // ItemId
		buffer.writeInt(item.getLocation()); // T1
		buffer.writeLong(item.getCount()); // Quantity
		buffer.writeShort(item.getItem().getType2()); // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item
		buffer.writeShort(item.getCustomType1()); // Filler (always 0)
		buffer.writeShort(item.getEquipped()); // Equipped : 00-No, 01-yes
		buffer.writeInt(item.getItem().getBodyPart().getMask()); // Slot : 0006-lr.ear, 0008-neck, 0030-lr.finger, 0040-head, 0100-l.hand, 0200-gloves, 0400-chest, 0800-pants, 1000-feet, 4000-r.hand, 8000-r.hand
		buffer.writeShort(item.getEnchant()); // Enchant level (pet level shown in control item).
		buffer.writeShort(item.getCustomType2()); // Pet name exists or not shown in control item.
		buffer.writeInt(item.getAugmentationBonus());
		buffer.writeInt(item.getMana());
		buffer.writeInt(item.getTime());
		writeItemElementalAndEnchant(item, buffer);
	}
	
	protected void writeItemElementalAndEnchant(ItemInfo item, WriteBuffer buffer)
	{
		buffer.writeShort(item.getAttackElementType());
		buffer.writeShort(item.getAttackElementPower());
		for (byte i = 0; i < 6; i++)
		{
			buffer.writeShort(item.getElementDefAttr(i));
		}
		
		// Enchant Effects
		for (int op : item.getEnchantOptions())
		{
			buffer.writeShort(op);
		}
	}
	
    /**
	 * Salvation (protocol 140) mask-based item layout: cddcQcchQccddc + optional blocks.
	 * Ported byte-exact from the Mobius 5.0 Salvation reference.
	 */
	protected void writeItem140(Item item, WriteBuffer buffer)
	{
		writeItem140(new ItemInfo(item), buffer);
	}
	
	private void writeItem140Internal(ItemInfo itemInfo, long countOverride, WriteBuffer buffer)
	{
		int mask = 0;
		if (itemInfo.getAugmentationBonus() != 0)
		{
			mask |= ItemListType.AUGMENT_BONUS.getMask();
		}
		if ((itemInfo.getAttackElementType() >= 0) || (itemInfo.getElementDefAttr((byte) 0) > 0) || (itemInfo.getElementDefAttr((byte) 1) > 0) || (itemInfo.getElementDefAttr((byte) 2) > 0) || (itemInfo.getElementDefAttr((byte) 3) > 0) || (itemInfo.getElementDefAttr((byte) 4) > 0) || (itemInfo.getElementDefAttr((byte) 5) > 0))
		{
			mask |= ItemListType.ELEMENTAL_ATTRIBUTE.getMask();
		}
		if (itemInfo.getEnchantOptions() != null)
		{
			for (int op : itemInfo.getEnchantOptions())
			{
				if (op > 0)
				{
					mask |= ItemListType.ENCHANT_EFFECT.getMask();
					break;
				}
			}
		}
		
		buffer.writeByte(mask);
		buffer.writeInt(itemInfo.getObjectId()); // ObjectId
		buffer.writeInt(itemInfo.getItem().getDisplayId()); // ItemId
		buffer.writeByte(itemInfo.getItem().isQuestItem() || (itemInfo.getEquipped() == 1) ? 0xFF : itemInfo.getLocation()); // T1
		buffer.writeLong(countOverride); // Quantity
		buffer.writeByte(itemInfo.getItem().getType2()); // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item
		buffer.writeByte(itemInfo.getCustomType1()); // Filler (always 0)
		buffer.writeShort(itemInfo.getEquipped()); // Equipped : 00-No, 01-yes
		buffer.writeLong(itemInfo.getItem().getBodyPart().getMask()); // Slot
		buffer.writeByte(itemInfo.getEnchant()); // Enchant level (pet level shown in control item)
		buffer.writeByte(itemInfo.getCustomType2()); // Pet name exists or not shown in control item
		buffer.writeInt(itemInfo.getMana());
		buffer.writeInt(itemInfo.getTime());
		buffer.writeByte(1); // GOD Item enabled = 1 disabled (red) = 0
		buffer.writeByte(0); // 140 protocol
		buffer.writeByte(0); // 140 protocol
		if ((mask & ItemListType.AUGMENT_BONUS.getMask()) != 0)
		{
			buffer.writeInt(itemInfo.getAugmentationBonus() & 0x0000FFFF);
			buffer.writeInt(itemInfo.getAugmentationBonus() >> 16);
		}
		
		if ((mask & ItemListType.ELEMENTAL_ATTRIBUTE.getMask()) != 0)
		{
			buffer.writeShort(itemInfo.getAttackElementType());
			buffer.writeShort(itemInfo.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				buffer.writeShort(itemInfo.getElementDefAttr(i));
			}
		}
		
		if ((mask & ItemListType.ENCHANT_EFFECT.getMask()) != 0)
		{
			for (int op : itemInfo.getEnchantOptions())
			{
				buffer.writeInt(op);
			}
		}
	}
	
	protected void writeItem140(ItemInfo itemInfo, WriteBuffer buffer)
	{
		writeItem140Internal(itemInfo, itemInfo.getCount(), buffer);
	}
	
	protected void writeItem140(ItemInfo itemInfo, long countOverride, WriteBuffer buffer)
	{
		writeItem140Internal(itemInfo, countOverride, buffer);
	}
	
	/**
	 * Salvation (protocol 140) item block for shop products (no objectId/augment/elemental data).
	 */
	protected void writeItem140Product(org.l2jmobius.gameserver.mechanics.buylist.Product product, WriteBuffer buffer)
	{
		buffer.writeByte(0); // mask: no optional blocks
		buffer.writeInt(0); // ObjectId
		buffer.writeInt(product.getItem().getDisplayId()); // ItemId
		buffer.writeByte(0); // T1
		buffer.writeLong(product.getCount()); // Quantity
		buffer.writeByte(product.getItem().getType2()); // Item Type 2
		buffer.writeByte(0); // Custom type 1
		buffer.writeShort(0); // Equipped
		buffer.writeLong(product.getItem().getBodyPart().getMask()); // Slot
		buffer.writeByte(0); // Enchant level
		buffer.writeByte(0); // Custom type 2
		buffer.writeInt(0); // Mana
		buffer.writeInt(0); // Time
		buffer.writeByte(1); // GOD item enabled
		buffer.writeByte(0);
		buffer.writeByte(0);
	}
	
	protected void writeInventoryBlock(PlayerInventory inventory, WriteBuffer buffer)
	{
		if (inventory.hasInventoryBlock())
		{
			buffer.writeShort(inventory.getBlockItems().length);
			buffer.writeByte(inventory.getBlockMode());
			for (int i : inventory.getBlockItems())
			{
				buffer.writeInt(i);
			}
		}
		else
		{
			buffer.writeShort(0);
		}
	}
}
