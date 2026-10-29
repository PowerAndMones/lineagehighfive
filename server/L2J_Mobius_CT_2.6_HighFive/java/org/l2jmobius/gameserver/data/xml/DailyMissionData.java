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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.entity.actor.Player;
import org.l2jmobius.gameserver.entity.actor.enums.player.PlayerClass;
import org.l2jmobius.gameserver.entity.actor.holders.player.DailyMissionDataHolder;
import org.l2jmobius.gameserver.entity.item.holders.ItemHolder;
import org.l2jmobius.gameserver.util.StatSet;

/**
 * Daily missions data (DailyMission.xml), displayed by the Salvation 140 client in the clan interface.
 * @author Sdw, Mobius
 */
public class DailyMissionData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(DailyMissionData.class.getName());
	
	private final Map<Integer, List<DailyMissionDataHolder>> _dailyMissionRewards = new LinkedHashMap<>();
	private final List<DailyMissionDataHolder> _dailyMissionData = new ArrayList<>();
	private boolean _isAvailable;
	
	protected DailyMissionData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_dailyMissionRewards.clear();
		parseDatapackFile("data/DailyMission.xml");
		
		_dailyMissionData.clear();
		for (List<DailyMissionDataHolder> missionList : _dailyMissionRewards.values())
		{
			_dailyMissionData.addAll(missionList);
		}
		
		_isAvailable = !_dailyMissionRewards.isEmpty();
		
		if (_isAvailable)
		{
			ensureTable();
		}
		
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _dailyMissionRewards.size() + " one day rewards.");
	}
	
	/**
	 * Creates the progress table when missing, so a fresh install does not depend on the DB installer.
	 */
	private synchronized void ensureTable()
	{
		try (Connection con = DatabaseFactory.getConnection();
			Statement st = con.createStatement())
		{
			st.execute("CREATE TABLE IF NOT EXISTS character_daily_rewards (" //
				+ "charId int(10) UNSIGNED NOT NULL," //
				+ "rewardId int(3) UNSIGNED NOT NULL," //
				+ "status tinyint(1) UNSIGNED NOT NULL DEFAULT 1," //
				+ "progress int UNSIGNED NOT NULL DEFAULT 0," //
				+ "lastCompleted bigint UNSIGNED NOT NULL," //
				+ "PRIMARY KEY (charId, rewardId))");
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Could not ensure character_daily_rewards table: " + e.getMessage());
		}
	}
	
	@Override
	public void parseDocument(Document document, File file)
	{
		forEach(document, "list", listNode -> forEach(listNode, "reward", rewardNode ->
		{
			final StatSet set = new StatSet(parseAttributes(rewardNode));
			final List<ItemHolder> items = new ArrayList<>(1);
			forEach(rewardNode, "items", itemsNode -> forEach(itemsNode, "item", itemNode ->
			{
				final int itemId = parseInteger(itemNode.getAttributes(), "id");
				final int itemCount = parseInteger(itemNode.getAttributes(), "count");
				items.add(new ItemHolder(itemId, itemCount));
			}));
			
			set.set("items", items);
			
			final List<PlayerClass> classRestriction = new ArrayList<>(1);
			forEach(rewardNode, "classId", classRestrictionNode -> classRestriction.add(PlayerClass.getPlayerClass(Integer.parseInt(classRestrictionNode.getTextContent()))));
			set.set("classRestriction", classRestriction);
			
			// Initial values in case handler doesn't exist
			set.set("handler", "");
			set.set("params", StatSet.EMPTY_STATSET);
			
			// Parse handler and parameters
			forEach(rewardNode, "handler", handlerNode ->
			{
				set.set("handler", parseString(handlerNode.getAttributes(), "name"));
				
				final StatSet params = new StatSet();
				set.set("params", params);
				forEach(handlerNode, "param", paramNode -> params.set(parseString(paramNode.getAttributes(), "name"), paramNode.getTextContent()));
			});
			
			final DailyMissionDataHolder holder = new DailyMissionDataHolder(set);
			_dailyMissionRewards.computeIfAbsent(holder.getId(), _ -> new ArrayList<>()).add(holder);
		}));
	}
	
	public Collection<DailyMissionDataHolder> getDailyMissionData()
	{
		return _dailyMissionData;
	}
	
	public Collection<DailyMissionDataHolder> getDailyMissionData(Player player)
	{
		final List<DailyMissionDataHolder> missionData = new LinkedList<>();
		for (DailyMissionDataHolder mission : _dailyMissionData)
		{
			if (mission.isDisplayable(player))
			{
				missionData.add(mission);
			}
		}
		
		return missionData;
	}
	
	public Collection<DailyMissionDataHolder> getDailyMissionData(int id)
	{
		return _dailyMissionRewards.get(id);
	}
	
	public boolean isAvailable()
	{
		return _isAvailable;
	}
	
	public static DailyMissionData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DailyMissionData INSTANCE = new DailyMissionData();
	}
}
