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

import org.l2jmobius.gameserver.network.enums.IUpdateTypeComponent;

/**
 * @author UnAfraid
 * @param <T>
 */
public abstract class AbstractMaskPacket<T extends IUpdateTypeComponent> extends ServerPacket
{
	protected static final byte[] DEFAULT_FLAG_ARRAY =
	{
		(byte) 0x80,
		0x40,
		0x20,
		0x10,
		0x08,
		0x04,
		0x02,
		0x01
	};
	
	protected abstract byte[] getMasks();
	
	protected void onNewMaskAdded(T component)
	{
	}
	
	@SafeVarargs
	public final void addComponentType(T... updateComponents)
	{
		for (T component : updateComponents)
		{
			if (!containsMask(component))
			{
				addMask(component.getMask());
				onNewMaskAdded(component);
			}
		}
	}
	
	protected void addMask(int mask)
	{
		getMasks()[mask >> 3] |= DEFAULT_FLAG_ARRAY[mask & 7];
	}
	
	public boolean containsMask(T component)
	{
		return containsMask(component.getMask());
	}
	
	public boolean containsMask(int mask)
	{
		return (getMasks()[mask >> 3] & DEFAULT_FLAG_ARRAY[mask & 7]) != 0;
	}
	
	/**
	 * @param masks
	 * @param type
	 * @return {@code true} if the mask contains the current update component type
	 */
	public boolean containsMask(int masks, T type)
	{
		return (masks & type.getMask()) == type.getMask();
	}
}

