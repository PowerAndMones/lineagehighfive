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
package org.l2jmobius.gameserver.entity.beautyshop;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.util.StatSet;

/**
 * A beauty shop style entry (hair, hair color or face).
 * @author Sdw
 */
public class BeautyItem
{
	private final int _id;
	private final int _adena;
	private final int _resetAdena;
	private final int _beautyShopTicket;
	private final Map<Integer, BeautyItem> _colors = new HashMap<>();
	
	public BeautyItem(StatSet set)
	{
		_id = set.getInt("id");
		_adena = set.getInt("adena", 0);
		_resetAdena = set.getInt("reset_adena", 0);
		_beautyShopTicket = set.getInt("beauty_shop_ticket", 0);
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getAdena()
	{
		return _adena;
	}
	
	public int getResetAdena()
	{
		return _resetAdena;
	}
	
	public int getBeautyShopTicket()
	{
		return _beautyShopTicket;
	}
	
	public void addColor(StatSet set)
	{
		final BeautyItem color = new BeautyItem(set);
		_colors.put(set.getInt("id"), color);
	}
	
	public Map<Integer, BeautyItem> getColors()
	{
		return _colors;
	}
}
