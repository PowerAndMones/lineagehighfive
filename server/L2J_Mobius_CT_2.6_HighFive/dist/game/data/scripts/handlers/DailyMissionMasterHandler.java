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
package handlers;

import java.util.logging.Logger;

import org.l2jmobius.gameserver.handler.DailyMissionHandler;

import handlers.dailymissions.LevelDailyMissionHandler;

/**
 * Registers all daily mission handler factories used by DailyMission.xml.
 * @author UnAfraid
 */
public class DailyMissionMasterHandler
{
	private static final Logger LOGGER = Logger.getLogger(DailyMissionMasterHandler.class.getName());
	
	public static void main(String[] args)
	{
		DailyMissionHandler.getInstance().registerHandler("level", LevelDailyMissionHandler::new);
		LOGGER.info(DailyMissionMasterHandler.class.getSimpleName() + ": Loaded " + DailyMissionHandler.getInstance().size() + " handlers.");
	}
}
