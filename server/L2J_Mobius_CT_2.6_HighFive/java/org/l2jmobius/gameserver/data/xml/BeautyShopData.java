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
package org.l2jmobius.gameserver.data.xml;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.entity.actor.enums.creature.Race;
import org.l2jmobius.gameserver.entity.actor.enums.player.Sex;
import org.l2jmobius.gameserver.entity.beautyshop.BeautyData;
import org.l2jmobius.gameserver.entity.beautyshop.BeautyItem;
import org.l2jmobius.gameserver.util.StatSet;

/**
 * Beauty Shop data (BeautyShop.xml), displayed by the Salvation 140 client.
 * @author Sdw
 */
public class BeautyShopData implements IXmlReader
{
	private final Map<Race, Map<Sex, BeautyData>> _beautyList = new EnumMap<>(Race.class);
	private final Map<Sex, BeautyData> _beautyData = new EnumMap<>(Sex.class);
	
	protected BeautyShopData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_beautyList.clear();
		_beautyData.clear();
		parseDatapackFile("data/BeautyShop.xml");
	}
	
	@Override
	public void parseDocument(Document document, File file)
	{
		NamedNodeMap attrs;
		StatSet set;
		Node att;
		Race race = null;
		Sex sex = null;
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("race".equalsIgnoreCase(d.getNodeName()))
					{
						att = d.getAttributes().getNamedItem("type");
						if (att != null)
						{
							race = parseEnum(att, Race.class);
						}
						
						for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
						{
							if ("sex".equalsIgnoreCase(b.getNodeName()))
							{
								att = b.getAttributes().getNamedItem("type");
								if (att != null)
								{
									sex = parseEnum(att, Sex.class);
								}
								
								final BeautyData beautyData = new BeautyData();
								for (Node a = b.getFirstChild(); a != null; a = a.getNextSibling())
								{
									if ("hair".equalsIgnoreCase(a.getNodeName()))
									{
										attrs = a.getAttributes();
										set = new StatSet();
										for (int i = 0; i < attrs.getLength(); i++)
										{
											att = attrs.item(i);
											set.set(att.getNodeName(), att.getNodeValue());
										}
										
										final BeautyItem hair = new BeautyItem(set);
										for (Node g = a.getFirstChild(); g != null; g = g.getNextSibling())
										{
											if ("color".equalsIgnoreCase(g.getNodeName()))
											{
												attrs = g.getAttributes();
												set = new StatSet();
												for (int i = 0; i < attrs.getLength(); i++)
												{
													att = attrs.item(i);
													set.set(att.getNodeName(), att.getNodeValue());
												}
												
												hair.addColor(set);
											}
										}
										
										beautyData.addHair(hair);
									}
									else if ("face".equalsIgnoreCase(a.getNodeName()))
									{
										attrs = a.getAttributes();
										set = new StatSet();
										for (int i = 0; i < attrs.getLength(); i++)
										{
											att = attrs.item(i);
											set.set(att.getNodeName(), att.getNodeValue());
										}
										
										final BeautyItem face = new BeautyItem(set);
										beautyData.addFace(face);
									}
								}
								
								_beautyData.put(sex, beautyData);
							}
						}
						
						_beautyList.put(race, _beautyData);
					}
				}
			}
		}
	}
	
	public boolean hasBeautyData(Race race, Sex sex)
	{
		return _beautyList.containsKey(race) && _beautyList.get(race).containsKey(sex);
	}
	
	public BeautyData getBeautyData(Race race, Sex sex)
	{
		if (_beautyList.containsKey(race))
		{
			return _beautyList.get(race).get(sex);
		}
		
		return null;
	}
	
	public static BeautyShopData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BeautyShopData INSTANCE = new BeautyShopData();
	}
}
