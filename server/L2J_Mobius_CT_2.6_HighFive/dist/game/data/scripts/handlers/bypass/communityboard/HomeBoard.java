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
package handlers.bypass.communityboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.config.custom.CommunityBoardConfig;
import org.l2jmobius.gameserver.config.custom.PremiumSystemConfig;
import org.l2jmobius.gameserver.config.custom.SchemeBufferConfig;
import org.l2jmobius.gameserver.data.SchemeBufferTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.xml.BuyListData;
import org.l2jmobius.gameserver.data.xml.ClassListData;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.entity.World;
import org.l2jmobius.gameserver.entity.actor.Creature;
import org.l2jmobius.gameserver.entity.actor.Player;
import org.l2jmobius.gameserver.entity.actor.Summon;
import org.l2jmobius.gameserver.entity.actor.enums.player.PlayerClass;
import org.l2jmobius.gameserver.entity.actor.holders.npc.BuffSkillHolder;
import org.l2jmobius.gameserver.entity.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.entity.zone.ZoneId;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.handler.IParseBoardHandler;
import org.l2jmobius.gameserver.managers.PcCafePointsManager;
import org.l2jmobius.gameserver.managers.PremiumManager;
import org.l2jmobius.gameserver.mechanics.skill.BuffInfo;
import org.l2jmobius.gameserver.mechanics.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.BuyList;
import org.l2jmobius.gameserver.network.serverpackets.ExBuySellList;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import org.l2jmobius.gameserver.network.serverpackets.ShowBoard;

/**
 * Home board.
 * @author Zoey76, Mobius
 */
public class HomeBoard implements IParseBoardHandler
{
	private static final DateTimeFormatter PREMIUM_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	// SQL Queries
	private static final String COUNT_FAVORITES = "SELECT COUNT(*) AS favorites FROM `bbs_favorites` WHERE `playerId`=?";
	private static final String NAVIGATION_PATH = "data/html/CommunityBoard/Custom/navigation.html";

	/** Scheme cast order: dances and songs are applied first. */
	private static final String[] CAST_ORDER =
	{
		"Dances",
		"Songs",
		"Chants",
		"Buffs",
		"Resist",
		"Special"
	};

	private static final String[] COMMANDS =
	{
		"_bbshome",
		"_bbstop",
	};

	private static final String[] CUSTOM_COMMANDS =
	{
		PremiumSystemConfig.PREMIUM_SYSTEM_ENABLED && CommunityBoardConfig.COMMUNITY_PREMIUM_SYSTEM_ENABLED ? "_bbspremium" : null,
		CommunityBoardConfig.COMMUNITYBOARD_ENABLE_MULTISELLS ? "_bbsexcmultisell" : null,
		CommunityBoardConfig.COMMUNITYBOARD_ENABLE_MULTISELLS ? "_bbsmultisell" : null,
		CommunityBoardConfig.COMMUNITYBOARD_ENABLE_MULTISELLS ? "_bbssell" : null,
		CommunityBoardConfig.COMMUNITYBOARD_ENABLE_TELEPORTS ? "_bbsteleport" : null,
		CommunityBoardConfig.COMMUNITYBOARD_ENABLE_BUFFS ? "_bbsbuff" : null,
		CommunityBoardConfig.COMMUNITYBOARD_ENABLE_BUFFS ? "_bbsscheme" : null,
		CommunityBoardConfig.COMMUNITYBOARD_ENABLE_CLASS_MASTER ? "_bbsclass" : null,
		CommunityBoardConfig.COMMUNITYBOARD_ENABLE_HEAL ? "_bbsheal" : null,
		CommunityBoardConfig.COMMUNITYBOARD_ENABLE_DELEVEL ? "_bbsdelevel" : null
	};

	private static final BiPredicate<String, Player> COMBAT_CHECK = (command, player) ->
	{
		boolean commandCheck = false;
		for (String c : CUSTOM_COMMANDS)
		{
			if ((c != null) && command.startsWith(c))
			{
				commandCheck = true;
				break;
			}
		}

		return commandCheck && (player.isCastingNow() || player.isCastingSimultaneouslyNow() || player.isInCombat() || player.isInDuel() || player.isInOlympiadMode() || player.isInsideZone(ZoneId.SIEGE) || player.isInsideZone(ZoneId.PVP) || (player.getPvpFlag() > 0) || player.isAlikeDead() || player.isOnEvent() || player.isInStoreMode());
	};

	private static final Predicate<Player> KARMA_CHECK = player -> CommunityBoardConfig.COMMUNITYBOARD_KARMA_DISABLED && (player.getKarma() > 0);

	@Override
	public String[] getCommandList()
	{
		final List<String> commands = new ArrayList<>();
		commands.addAll(Arrays.asList(COMMANDS));
		commands.addAll(Arrays.asList(CUSTOM_COMMANDS));
		return commands.stream().filter(Objects::nonNull).toArray(String[]::new);
	}

	@Override
	public boolean onCommand(String command, Player player)
	{
		// Old custom conditions check move to here.
		if (CommunityBoardConfig.COMMUNITYBOARD_COMBAT_DISABLED && COMBAT_CHECK.test(command, player))
		{
			player.sendMessage("You can't use the Community Board right now.");
			return false;
		}

		if (KARMA_CHECK.test(player))
		{
			player.sendMessage("Players with Karma cannot use the Community Board.");
			return false;
		}

		if (CommunityBoardConfig.COMMUNITYBOARD_PEACE_ONLY && !player.isInsideZone(ZoneId.PEACE))
		{
			player.sendMessage("Community Board cannot be used out of peace zone.");
			return false;
		}

		String returnHtml = null;
		String navigation = null;

		if (CommunityBoardConfig.CUSTOM_CB_ENABLED)
		{
			navigation = HtmCache.getInstance().getHtm(player, NAVIGATION_PATH);
		}

		if (command.equals("_bbshome") || command.equals("_bbstop"))
		{
			final String customPath = CommunityBoardConfig.CUSTOM_CB_ENABLED ? "Custom/" : "";
			CommunityBoardHandler.getInstance().addBypass(player, "Home", command);
			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/" + customPath + "home.html");
			if (!CommunityBoardConfig.CUSTOM_CB_ENABLED)
			{
				returnHtml = returnHtml.replace("%fav_count%", Integer.toString(getFavoriteCount(player)));
				returnHtml = returnHtml.replace("%region_count%", Integer.toString(getRegionCount(player)));
				returnHtml = returnHtml.replace("%clan_count%", Integer.toString(ClanTable.getInstance().getClanCount()));
			}
			else if (returnHtml != null)
			{
				returnHtml = returnHtml.replace("%online%", Integer.toString(World.getPlayers().size()));
				returnHtml = returnHtml.replace("%player_name%", player.getName());
			}
		}
		else if (command.equals("_bbstop;buffer/main.html") && CommunityBoardConfig.COMMUNITYBOARD_ENABLE_BUFFS)
		{
			// Scheme buffer main page (dynamic content).
			returnHtml = buildBufferMain(player, navigation);
		}
		else if (command.startsWith("_bbstop;"))
		{
			final String customPath = CommunityBoardConfig.CUSTOM_CB_ENABLED ? "Custom/" : "";
			final String path = command.replace("_bbstop;", "");
			if (!path.isEmpty() && path.endsWith(".html"))
			{
				returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/" + customPath + path);
			}
		}
		else if (command.startsWith("_bbsmultisell"))
		{
			final String fullBypass = command.replace("_bbsmultisell;", "");
			final String[] buypassOptions = fullBypass.split(",");
			final int multisellId = Integer.parseInt(buypassOptions[0]);
			final String page = buypassOptions[1];
			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + page + ".html");
			ThreadPool.schedule(() -> MultisellData.getInstance().separateAndSend(multisellId, player, null, false), 100);
		}
		else if (command.startsWith("_bbsexcmultisell"))
		{
			final String fullBypass = command.replace("_bbsexcmultisell;", "");
			final String[] buypassOptions = fullBypass.split(",");
			final int multisellId = Integer.parseInt(buypassOptions[0]);
			final String page = buypassOptions[1];
			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + page + ".html");
			ThreadPool.schedule(() -> MultisellData.getInstance().separateAndSend(multisellId, player, null, true), 100);
		}
		else if (command.startsWith("_bbssell"))
		{
			final String page = command.replace("_bbssell;", "");
			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + page + ".html");
			ThreadPool.schedule(() ->
			{
				player.sendPacket(new BuyList(BuyListData.getInstance().getBuyList(423), player.getAdena(), 0));
				player.sendPacket(new ExBuySellList(player, false));
			}, 100);
		}
		else if (command.startsWith("_bbsteleport"))
		{
			final String teleBuypass = command.replace("_bbsteleport;", "");
			if (player.getInventory().getInventoryItemCount(CommunityBoardConfig.COMMUNITYBOARD_CURRENCY, -1) < CommunityBoardConfig.COMMUNITYBOARD_TELEPORT_PRICE)
			{
				player.sendMessage("Not enough currency!");
			}
			else if (CommunityBoardConfig.COMMUNITY_AVAILABLE_TELEPORTS.get(teleBuypass) != null)
			{
				player.disableAllSkills();
				player.sendPacket(new ShowBoard());
				player.destroyItemByItemId(ItemProcessType.FEE, CommunityBoardConfig.COMMUNITYBOARD_CURRENCY, CommunityBoardConfig.COMMUNITYBOARD_TELEPORT_PRICE, player, true);
				player.setIn7sDungeon(false);
				player.setInstanceId(0);
				player.teleToLocation(CommunityBoardConfig.COMMUNITY_AVAILABLE_TELEPORTS.get(teleBuypass), 0);
				ThreadPool.schedule(player::enableAllSkills, 3000);
			}
		}
		else if (command.startsWith("_bbsbuff"))
		{
			final String fullBypass = command.replace("_bbsbuff;", "");
			final String[] buypassOptions = fullBypass.split(";");
			final int buffCount = buypassOptions.length - 1;
			final String page = buypassOptions[buffCount];
			if (player.getInventory().getInventoryItemCount(CommunityBoardConfig.COMMUNITYBOARD_CURRENCY, -1) < (CommunityBoardConfig.COMMUNITYBOARD_BUFF_PRICE * buffCount))
			{
				player.sendMessage("Not enough currency!");
			}
			else
			{
				player.destroyItemByItemId(ItemProcessType.FEE, CommunityBoardConfig.COMMUNITYBOARD_CURRENCY, CommunityBoardConfig.COMMUNITYBOARD_BUFF_PRICE * buffCount, player, true);
				final Summon pet = player.getSummon();
				final List<Creature> targets = new ArrayList<>(4);
				targets.add(player);
				if (pet != null)
				{
					targets.add(pet);
				}

				for (int i = 0; i < buffCount; i++)
				{
					final Skill skill = SkillData.getInstance().getSkill(Integer.parseInt(buypassOptions[i].split(",")[0]), Integer.parseInt(buypassOptions[i].split(",")[1]));
					if (!CommunityBoardConfig.COMMUNITY_AVAILABLE_BUFFS.contains(skill.getId()))
					{
						continue;
					}

					for (Creature target : targets)
					{
						skill.applyEffects(player, target);
						if (CommunityBoardConfig.COMMUNITYBOARD_CAST_ANIMATIONS)
						{
							player.sendPacket(new MagicSkillUse(player, target, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
						}
					}
				}
			}

			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + page + ".html");
			if ((returnHtml != null) && CommunityBoardConfig.CUSTOM_CB_ENABLED)
			{
				returnHtml = returnHtml.replace("%navigation%", navigation);
			}
			if (returnHtml != null)
			{
				CommunityBoardHandler.separateAndSend(returnHtml, player);
			}
			return false;
		}
		else if (command.startsWith("_bbsheal"))
		{
			final String page = command.replace("_bbsheal;", "");
			if (player.getInventory().getInventoryItemCount(CommunityBoardConfig.COMMUNITYBOARD_CURRENCY, -1) < (CommunityBoardConfig.COMMUNITYBOARD_HEAL_PRICE))
			{
				player.sendMessage("Not enough currency!");
			}
			else
			{
				player.destroyItemByItemId(ItemProcessType.FEE, CommunityBoardConfig.COMMUNITYBOARD_CURRENCY, CommunityBoardConfig.COMMUNITYBOARD_HEAL_PRICE, player, true);
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentMp(player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
				if (player.hasSummon())
				{
					player.getSummon().setCurrentHp(player.getSummon().getMaxHp());
					player.getSummon().setCurrentMp(player.getSummon().getMaxMp());
					player.getSummon().setCurrentCp(player.getSummon().getMaxCp());
				}

				player.updateUserInfo();
				player.sendMessage("You used heal!");
			}

			returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/" + page + ".html");
			if ((returnHtml != null) && CommunityBoardConfig.CUSTOM_CB_ENABLED)
			{
				returnHtml = returnHtml.replace("%navigation%", navigation);
			}
			if (returnHtml != null)
			{
				CommunityBoardHandler.separateAndSend(returnHtml, player);
			}
			return false;
		}
		else if (command.equals("_bbsdelevel"))
		{
			if (player.getInventory().getInventoryItemCount(CommunityBoardConfig.COMMUNITYBOARD_CURRENCY, -1) < CommunityBoardConfig.COMMUNITYBOARD_DELEVEL_PRICE)
			{
				player.sendMessage("Not enough currency!");
			}
			else if (player.getLevel() == 1)
			{
				player.sendMessage("You are at minimum level!");
			}
			else
			{
				player.destroyItemByItemId(ItemProcessType.FEE, CommunityBoardConfig.COMMUNITYBOARD_CURRENCY, CommunityBoardConfig.COMMUNITYBOARD_DELEVEL_PRICE, player, true);
				final int newLevel = player.getLevel() - 1;
				player.setExp(ExperienceData.getInstance().getExpForLevel(newLevel));
				player.getStat().setLevel((byte) newLevel);
				player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
				player.broadcastUserInfo();
				player.checkPlayerSkills(); // Adjust skills according to new level.
				returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/delevel/complete.html");
			}
		}
		else if (command.startsWith("_bbspremium"))
		{
			final String fullBypass = command.replace("_bbspremium;", "");
			final String[] buypassOptions = fullBypass.split(",");
			final int premiumDays = Integer.parseInt(buypassOptions[0]);
			if ((premiumDays < 1) || (premiumDays > 30) || (player.getInventory().getInventoryItemCount(CommunityBoardConfig.COMMUNITY_PREMIUM_COIN_ID, -1) < (CommunityBoardConfig.COMMUNITY_PREMIUM_PRICE_PER_DAY * premiumDays)))
			{
				player.sendMessage("Not enough currency!");
			}
			else
			{
				final String premiumKey = PremiumSystemConfig.ACCOUNT_WIDE_PREMIUM ? player.getAccountName() : player.getName();
				player.destroyItemByItemId(ItemProcessType.FEE, CommunityBoardConfig.COMMUNITY_PREMIUM_COIN_ID, CommunityBoardConfig.COMMUNITY_PREMIUM_PRICE_PER_DAY * premiumDays, player, true);
				PremiumManager.getInstance().addPremiumTime(premiumKey, premiumDays, TimeUnit.DAYS);
				player.sendMessage("You will now have premium status until " + PREMIUM_FORMAT.format(Instant.ofEpochMilli(PremiumManager.getInstance().getPremiumExpiration(premiumKey)).atZone(java.time.ZoneId.systemDefault())) + ".");
				if (PremiumSystemConfig.PC_CAFE_RETAIL_LIKE)
				{
					PcCafePointsManager.getInstance().run(player);
				}

				returnHtml = HtmCache.getInstance().getHtm(player, "data/html/CommunityBoard/Custom/premium/thankyou.html");
			}
		}
		else if (command.startsWith("_bbsscheme") && CommunityBoardConfig.COMMUNITYBOARD_ENABLE_BUFFS)
		{
			returnHtml = handleSchemeCommand(command, player, navigation);
		}
		else if (command.startsWith("_bbsclass") && CommunityBoardConfig.COMMUNITYBOARD_ENABLE_CLASS_MASTER)
		{
			returnHtml = handleClassCommand(command, player, navigation);
		}

		if (returnHtml != null)
		{
			if (CommunityBoardConfig.CUSTOM_CB_ENABLED)
			{
				returnHtml = returnHtml.replace("%navigation%", navigation);
			}

			CommunityBoardHandler.separateAndSend(returnHtml, player);
		}

		return false;
	}

	/**
	 * Dispatches the scheme buffer bypasses and returns the HTML to display.
	 * @param command the bypass command
	 * @param player the player
	 * @param navigation the navigation HTML or null
	 * @return the HTML page or null
	 */
	private static String handleSchemeCommand(String command, Player player, String navigation)
	{
		final SchemeBufferTable table = SchemeBufferTable.getInstance();
		final int playerId = player.getObjectId();

		// Root: _bbsscheme
		if (command.equals("_bbsscheme"))
		{
			return buildBufferMain(player, navigation);
		}

		final String data = command.substring("_bbsscheme;".length());
		final String[] parts = data.split(";", 4);
		final String action = parts[0];

		switch (action)
		{
			case "cat":
			{
				if (parts.length < 2)
				{
					return buildBufferMain(player, navigation);
				}
				final String type = parts[1];
				final String editScheme = (parts.length >= 3) ? parts[2] : null;
				return buildBufferCategory(player, type, editScheme, navigation);
			}
			case "edit":
			{
				if (parts.length < 2)
				{
					return buildBufferMain(player, navigation);
				}
				return buildSchemeEditor(player, parts[1], navigation);
			}
			case "use":
			{
				if (parts.length < 2)
				{
					return buildBufferMain(player, navigation);
				}
				castScheme(player, table, parts[1]);
				return buildBufferMain(player, navigation);
			}
			case "group":
			{
				if (parts.length < 2)
				{
					return buildBufferMain(player, navigation);
				}
				castGroup(player, table, parts[1]);
				return buildBufferMain(player, navigation);
			}
			case "cast":
			{
				// _bbsscheme;cast;type;skillId
				if (parts.length < 3)
				{
					return buildBufferMain(player, navigation);
				}
				final String type = parts[1];
				final int skillId = parseIntSafe(parts[2]);
				final BuffSkillHolder holder = table.getAvailableBuff(type, skillId);
				if (holder != null)
				{
					if (payForBuffs(player, List.of(holder)))
					{
						applyBuff(player, holder);
					}
				}
				return buildBufferCategory(player, type, null, navigation);
			}
			case "add":
			{
				// _bbsscheme;add;schemeName;type;skillId
				if (parts.length < 4)
				{
					return buildBufferMain(player, navigation);
				}
				final String schemeName = parts[1];
				final String type = parts[2];
				final int skillId = parseIntSafe(parts[3]);
				final Map<String, List<Integer>> playerSchemes = table.getPlayerSchemes(playerId);
				if ((schemeName != null) && (playerSchemes != null) && playerSchemes.containsKey(schemeName))
				{
					if (table.getScheme(playerId, schemeName).size() >= MAX_SCHEME_BUFFS)
					{
						player.sendMessage("Scheme is full! Maximum " + MAX_SCHEME_BUFFS + " buffs.");
					}
					else
					{
						table.addSkillToScheme(playerId, schemeName, skillId);
					}
				}
				return buildBufferCategory(player, type, schemeName, navigation);
			}
			case "rem":
			{
				// _bbsscheme;rem;schemeName;skillId
				if (parts.length < 3)
				{
					return buildBufferMain(player, navigation);
				}
				final String schemeName = parts[1];
				final int skillId = parseIntSafe(parts[2]);
				table.removeSkillFromScheme(playerId, schemeName, skillId);
				return buildSchemeEditor(player, schemeName, navigation);
			}
			case "save":
			{
				// _bbsscheme;save;$name
				if (parts.length < 2)
				{
					return buildBufferMain(player, navigation);
				}
				saveCurrentBuffs(player, table, parts[1]);
				return buildBufferMain(player, navigation);
			}
			case "del":
			{
				if (parts.length < 2)
				{
					return buildBufferMain(player, navigation);
				}
				table.deleteScheme(playerId, parts[1]);
				player.sendMessage("Scheme deleted.");
				return buildBufferMain(player, navigation);
			}
			case "cancel":
			{
				player.stopAllEffects();
				player.updateUserInfo();
				player.sendMessage("All buffs removed.");
				return buildBufferMain(player, navigation);
			}
			case "heal":
			{
				if (player.getInventory().getInventoryItemCount(CommunityBoardConfig.COMMUNITYBOARD_CURRENCY, -1) < CommunityBoardConfig.COMMUNITYBOARD_HEAL_PRICE)
				{
					player.sendMessage("Not enough currency!");
				}
				else
				{
					player.destroyItemByItemId(ItemProcessType.FEE, CommunityBoardConfig.COMMUNITYBOARD_CURRENCY, CommunityBoardConfig.COMMUNITYBOARD_HEAL_PRICE, player, true);
					player.setCurrentHp(player.getMaxHp());
					player.setCurrentMp(player.getMaxMp());
					player.setCurrentCp(player.getMaxCp());
					if (player.hasSummon())
					{
						player.getSummon().setCurrentHp(player.getSummon().getMaxHp());
						player.getSummon().setCurrentMp(player.getSummon().getMaxMp());
						player.getSummon().setCurrentCp(player.getSummon().getMaxCp());
					}
					player.updateUserInfo();
					player.sendMessage("You used heal!");
				}
				return buildBufferMain(player, navigation);
			}
			default:
			{
				return buildBufferMain(player, navigation);
			}
		}
	}

	private static final int MAX_SCHEME_BUFFS = 28;

	private static int parseIntSafe(String value)
	{
		try
		{
			return Integer.parseInt(value.trim());
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	/**
	 * Saves the player's currently applied buffs as a scheme.
	 */
	private static void saveCurrentBuffs(Player player, SchemeBufferTable table, String schemeName)
	{
		if ((schemeName == null) || !schemeName.matches("[A-Za-zА-Яа-яЁё0-9_\\-]{1,16}"))
		{
			player.sendMessage("Invalid scheme name! Use up to 16 letters, digits, _ or -.");
			return;
		}

		final Map<String, List<Integer>> schemes = table.getPlayerSchemes(player.getObjectId());
		if ((schemes != null) && (schemes.size() >= SchemeBufferConfig.BUFFER_MAX_SCHEMES) && !schemes.containsKey(schemeName))
		{
			player.sendMessage("You already have maximum " + SchemeBufferConfig.BUFFER_MAX_SCHEMES + " schemes!");
			return;
		}

		final List<Integer> buffIds = new ArrayList<>();
		for (BuffInfo info : player.getEffectList().getBuffs())
		{
			final Skill skill = info.getSkill();
			if ((skill != null) && (table.getAvailableBuff(skill.getId()) != null) && !buffIds.contains(skill.getId()))
			{
				buffIds.add(skill.getId());
			}
		}

		if (buffIds.isEmpty())
		{
			player.sendMessage("No available buffs to save! Apply buffs first.");
			return;
		}

		table.setScheme(player.getObjectId(), schemeName, buffIds);
		player.sendMessage("Scheme '" + schemeName + "' saved with " + buffIds.size() + " buffs.");
	}

	/**
	 * Casts all buffs of a player scheme.
	 */
	private static void castScheme(Player player, SchemeBufferTable table, String schemeName)
	{
		final List<Integer> skillIds = table.getScheme(player.getObjectId(), schemeName);
		if (skillIds.isEmpty())
		{
			player.sendMessage("Scheme not found!");
			return;
		}

		final List<BuffSkillHolder> holders = new ArrayList<>(skillIds.size());
		final List<Integer> addedIds = new ArrayList<>(skillIds.size());
		for (String type : CAST_ORDER)
		{
			for (int skillId : skillIds)
			{
				if (addedIds.contains(skillId))
				{
					continue;
				}
				final BuffSkillHolder holder = table.getAvailableBuff(type, skillId);
				if (holder != null)
				{
					holders.add(holder);
					addedIds.add(skillId);
				}
			}
		}

		if (holders.isEmpty())
		{
			player.sendMessage("Scheme is empty!");
			return;
		}

		if (payForBuffs(player, holders))
		{
			for (BuffSkillHolder holder : holders)
			{
				applyBuff(player, holder);
			}
		}
	}

	/**
	 * Casts a preset buff group (FIGHTER_GROUP / MAGE_GROUP).
	 */
	private static void castGroup(Player player, SchemeBufferTable table, String groupType)
	{
		final List<Integer> skillIds = table.getSkillsIdsByType(groupType);
		if (skillIds.isEmpty())
		{
			player.sendMessage("Preset not available!");
			return;
		}

		final List<BuffSkillHolder> holders = new ArrayList<>(skillIds.size());
		final List<Integer> addedIds = new ArrayList<>(skillIds.size());
		for (int skillId : skillIds)
		{
			if (addedIds.contains(skillId))
			{
				continue;
			}
			final BuffSkillHolder holder = table.getAvailableBuff(groupType, skillId);
			if (holder != null)
			{
				holders.add(holder);
				addedIds.add(skillId);
			}
		}

		if (payForBuffs(player, holders))
		{
			for (BuffSkillHolder holder : holders)
			{
				applyBuff(player, holder);
			}
		}
	}

	/**
	 * Checks the currency amount and takes the payment for the given buffs.
	 * @return true if payment succeeded (or is free)
	 */
	private static boolean payForBuffs(Player player, List<BuffSkillHolder> holders)
	{
		int total = 0;
		for (BuffSkillHolder holder : holders)
		{
			total += SchemeBufferConfig.BUFFER_STATIC_BUFF_COST >= 0 ? SchemeBufferConfig.BUFFER_STATIC_BUFF_COST : holder.getPrice();
		}

		if (player.getInventory().getInventoryItemCount(SchemeBufferConfig.BUFFER_ITEM_ID, -1) < total)
		{
			player.sendMessage("Not enough currency! Needed: " + total);
			return false;
		}

		if (total > 0)
		{
			player.destroyItemByItemId(ItemProcessType.FEE, SchemeBufferConfig.BUFFER_ITEM_ID, total, player, true);
		}
		return true;
	}

	/**
	 * Applies a single buff (and pet) at the highest available skill level.
	 */
	private static void applyBuff(Player player, BuffSkillHolder holder)
	{
		int level = SkillData.getInstance().getMaxLevel(holder.getId());
		if (level < 1)
		{
			level = holder.getLevel();
		}
		Skill skill = SkillData.getInstance().getSkill(holder.getId(), level);
		if (skill == null)
		{
			skill = SkillData.getInstance().getSkill(holder.getId(), holder.getLevel());
		}
		if (skill == null)
		{
			return;
		}

		skill.applyEffects(player, player);
		if (player.hasSummon())
		{
			skill.applyEffects(player, player.getSummon());
		}

		if (CommunityBoardConfig.COMMUNITYBOARD_CAST_ANIMATIONS)
		{
			player.sendPacket(new MagicSkillUse(player, player, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
		}
	}

	/**
	 * Builds the scheme buffer main page.
	 */
	private static String buildBufferMain(Player player, String navigation)
	{
		final SchemeBufferTable table = SchemeBufferTable.getInstance();
		final StringBuilder sb = new StringBuilder(4096);

		// Presets.
		sb.append("<tr><td height=12></td></tr>");
		sb.append("<tr><td align=\"center\"><table align=center border=0 cellspacing=6><tr>");
		sb.append("<td>").append(cbButton("Пресет: Воин", "_bbsscheme;group;FIGHTER_GROUP", 180)).append("</td>");
		sb.append("<td>").append(cbButton("Пресет: Маг", "_bbsscheme;group;MAGE_GROUP", 180)).append("</td>");
		sb.append("</tr></table></td></tr>");

		// Service buttons.
		sb.append("<tr><td height=6></td></tr>");
		sb.append("<tr><td align=\"center\"><table align=center border=0 cellspacing=6><tr>");
		sb.append("<td>").append(cbButton("Отменить бафы", "_bbsscheme;cancel", 140)).append("</td>");
		sb.append("<td>").append(cbButton("Вылечить", "_bbsscheme;heal", 140)).append("</td>");
		sb.append("</tr></table></td></tr>");

		// Separator.
		sb.append("<tr><td height=8></td></tr>");
		sb.append("<tr><td><center><img src=\"L2UI.SquareGray\" width=500 height=1></center></td></tr>");

		// Schemes.
		sb.append("<tr><td height=8></td></tr>");
		sb.append("<tr><td align=\"center\"><font color=\"CDB67F\">Мои схемы</font></td></tr>");
		final Map<String, List<Integer>> schemes = table.getPlayerSchemes(player.getObjectId());
		if ((schemes != null) && !schemes.isEmpty())
		{
			sb.append("<tr><td align=\"center\"><table align=center border=0 cellspacing=4>");
			for (Map.Entry<String, List<Integer>> entry : schemes.entrySet())
			{
				sb.append("<tr>");
				sb.append("<td width=150 align=\"left\"><font color=\"LEVEL\">").append(entry.getKey()).append("</font> <font color=\"908899\">(").append(entry.getValue().size()).append(")</font></td>");
				sb.append("<td>").append(cbButton("Наложить", "_bbsscheme;use;" + entry.getKey(), 110)).append("</td>");
				sb.append("<td>").append(cbButton("Редактор", "_bbsscheme;edit;" + entry.getKey(), 110)).append("</td>");
				sb.append("<td>").append(cbButton("Удалить", "_bbsscheme;del;" + entry.getKey(), 110)).append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table></td></tr>");
		}
		else
		{
			sb.append("<tr><td align=\"center\"><font color=\"908899\">У вас пока нет схем.</font></td></tr>");
		}

		// Save current buffs.
		sb.append("<tr><td height=6></td></tr>");
		sb.append("<tr><td align=\"center\"><table align=center border=0><tr>");
		sb.append("<td><edit var=\"schname\" width=120 height=21></td>");
		sb.append("<td>").append(cbButton("Сохранить текущие бафы", "_bbsscheme;save $schname", 220)).append("</td>");
		sb.append("</tr></table></td></tr>");

		// Separator.
		sb.append("<tr><td height=8></td></tr>");
		sb.append("<tr><td><center><img src=\"L2UI.SquareGray\" width=500 height=1></center></td></tr>");

		// Categories.
		sb.append("<tr><td height=8></td></tr>");
		sb.append("<tr><td align=\"center\"><font color=\"CDB67F\">Категории бафов</font></td></tr>");
		sb.append("<tr><td align=\"center\"><table align=center border=0 cellspacing=6><tr>");
		sb.append("<td>").append(cbButton("Общие", "_bbsscheme;cat;Buffs", 120)).append("</td>");
		sb.append("<td>").append(cbButton("Резисты", "_bbsscheme;cat;Resist", 120)).append("</td>");
		sb.append("<td>").append(cbButton("Особые", "_bbsscheme;cat;Special", 120)).append("</td>");
		sb.append("</tr><tr>");
		sb.append("<td>").append(cbButton("Танцы", "_bbsscheme;cat;Dances", 120)).append("</td>");
		sb.append("<td>").append(cbButton("Песни", "_bbsscheme;cat;Songs", 120)).append("</td>");
		sb.append("<td>").append(cbButton("Хоралы", "_bbsscheme;cat;Chants", 120)).append("</td>");
		sb.append("</tr></table></td></tr>");

		return buildBoardShell(player, "Бафер", sb.toString(), navigation);
	}

	/**
	 * Builds a buff category page.
	 * @param type the category type
	 * @param editScheme the scheme being edited or null
	 */
	private static String buildBufferCategory(Player player, String type, String editScheme, String navigation)
	{
		final SchemeBufferTable table = SchemeBufferTable.getInstance();
		final StringBuilder sb = new StringBuilder(8192);

		// Validate the category type.
		boolean validType = false;
		for (String t : table.getSkillTypes())
		{
			if (t.equalsIgnoreCase(type))
			{
				validType = true;
				break;
			}
		}
		if (!validType)
		{
			return buildBufferMain(player, navigation);
		}

		sb.append("<tr><td height=8></td></tr>");
		if (editScheme != null)
		{
			sb.append("<tr><td align=\"center\"><font color=\"LEVEL\">Схема: ").append(editScheme).append("</font> <font color=\"908899\">— выбирайте бафы, чтобы добавить</font></td></tr>");
		}
		sb.append("<tr><td align=\"center\"><table align=center border=0 cellspacing=10>");

		final List<Integer> skillIds = table.getSkillsIdsByType(type);
		int count = 0;
		for (int skillId : skillIds)
		{
			final BuffSkillHolder holder = table.getAvailableBuff(type, skillId);
			if (holder == null)
			{
				continue;
			}
			if ((count % 4) == 0)
			{
				sb.append(count == 0 ? "<tr>" : "</tr><tr>");
			}
			count++;

			sb.append("<td width=115 align=\"center\">");
			sb.append("<button value=\" \" action=\"bypass _bbsscheme;").append(editScheme != null ? "add;" + editScheme + ";" + type : "cast;" + type).append(";").append(skillId).append("\" width=32 height=32 back=\"icon.skill").append(skillId).append("\" fore=\"icon.skill").append(skillId).append("\">");
			sb.append("<br1><font color=\"908899\">").append(holder.getDescription()).append("</font>");
			final int price = SchemeBufferConfig.BUFFER_STATIC_BUFF_COST >= 0 ? SchemeBufferConfig.BUFFER_STATIC_BUFF_COST : holder.getPrice();
			if (price > 0)
			{
				sb.append("<br1><font color=\"LEVEL\">").append(price).append(" адена</font>");
			}
			sb.append("</td>");
		}
		if ((count % 4) != 0)
		{
			sb.append("</tr>");
		}
		sb.append("</table></td></tr>");

		sb.append("<tr><td height=6></td></tr>");
		sb.append("<tr><td align=\"center\"><table align=center border=0 cellspacing=6><tr>");
		sb.append("<td>").append(cbButton("Назад", editScheme != null ? "_bbsscheme;edit;" + editScheme : "_bbsscheme", 150)).append("</td>");
		sb.append("</tr></table></td></tr>");

		return buildBoardShell(player, "Бафер — категория", sb.toString(), navigation);
	}

	/**
	 * Builds the scheme editor page.
	 */
	private static String buildSchemeEditor(Player player, String schemeName, String navigation)
	{
		final SchemeBufferTable table = SchemeBufferTable.getInstance();
		final StringBuilder sb = new StringBuilder(4096);

		final List<Integer> skillIds = table.getScheme(player.getObjectId(), schemeName);
		sb.append("<tr><td height=8></td></tr>");
		sb.append("<tr><td align=\"center\"><font color=\"LEVEL\">Схема: ").append(schemeName).append("</font> <font color=\"908899\">(").append(skillIds.size()).append("/").append(MAX_SCHEME_BUFFS).append(" бафов)</font></td></tr>");

		if (skillIds.isEmpty())
		{
			sb.append("<tr><td align=\"center\"><font color=\"908899\">Схема пуста. Добавьте бафы из категорий ниже.</font></td></tr>");
		}
		else
		{
			sb.append("<tr><td align=\"center\"><table align=center border=0 cellspacing=4>");
			int count = 0;
			for (int skillId : skillIds)
			{
				final BuffSkillHolder holder = table.getAvailableBuff(skillId);
				if (holder == null)
				{
					continue;
				}
				if ((count % 2) == 0)
				{
					sb.append(count == 0 ? "<tr>" : "</tr><tr>");
				}
				count++;
				sb.append("<td width=240 align=\"left\"><img src=\"icon.skill").append(skillId).append("\" width=16 height=16> <font color=\"908899\">").append(holder.getDescription()).append("</font></td>");
				sb.append("<td>").append(cbButton("Убрать", "_bbsscheme;rem;" + schemeName + ";" + skillId, 100)).append("</td>");
			}
			if ((count % 2) != 0)
			{
				sb.append("</tr>");
			}
			sb.append("</table></td></tr>");
		}

		sb.append("<tr><td height=6></td></tr>");
		sb.append("<tr><td><center><img src=\"L2UI.SquareGray\" width=500 height=1></center></td></tr>");
		sb.append("<tr><td height=6></td></tr>");
		sb.append("<tr><td align=\"center\"><font color=\"CDB67F\">Добавить бафы</font></td></tr>");
		sb.append("<tr><td align=\"center\"><table align=center border=0 cellspacing=6><tr>");
		sb.append("<td>").append(cbButton("Общие", "_bbsscheme;cat;Buffs;" + schemeName, 120)).append("</td>");
		sb.append("<td>").append(cbButton("Резисты", "_bbsscheme;cat;Resist;" + schemeName, 120)).append("</td>");
		sb.append("<td>").append(cbButton("Особые", "_bbsscheme;cat;Special;" + schemeName, 120)).append("</td>");
		sb.append("</tr><tr>");
		sb.append("<td>").append(cbButton("Танцы", "_bbsscheme;cat;Dances;" + schemeName, 120)).append("</td>");
		sb.append("<td>").append(cbButton("Песни", "_bbsscheme;cat;Songs;" + schemeName, 120)).append("</td>");
		sb.append("<td>").append(cbButton("Хоралы", "_bbsscheme;cat;Chants;" + schemeName, 120)).append("</td>");
		sb.append("</tr></table></td></tr>");

		sb.append("<tr><td height=6></td></tr>");
		sb.append("<tr><td align=\"center\"><table align=center border=0 cellspacing=6><tr>");
		sb.append("<td>").append(cbButton("Наложить схему", "_bbsscheme;use;" + schemeName, 160)).append("</td>");
		sb.append("<td>").append(cbButton("Назад", "_bbsscheme", 160)).append("</td>");
		sb.append("</tr></table></td></tr>");

		return buildBoardShell(player, "Редактор схемы", sb.toString(), navigation);
	}

	/**
	 * Dispatches the class master bypasses and returns the HTML to display.
	 */
	private static String handleClassCommand(String command, Player player, String navigation)
	{
		// Root: _bbsclass
		if (command.equals("_bbsclass"))
		{
			return buildClassMaster(player, navigation);
		}

		final String data = command.substring("_bbsclass;".length());
		final String[] parts = data.split(";", 2);

		if ("change".equals(parts[0]) && (parts.length >= 2))
		{
			final int classId = parseIntSafe(parts[1]);
			final PlayerClass current = player.getPlayerClass();
			PlayerClass target = null;
			try
			{
				target = PlayerClass.getPlayerClass(classId);
			}
			catch (Exception e)
			{
				target = null;
			}

			if ((target == null) || !current.getNextClasses().contains(target))
			{
				player.sendMessage("This profession is not available for you.");
			}
			else
			{
				final int requiredLevel = getRequiredLevel(target);
				final int price = getClassPrice(target);
				if (player.getLevel() < requiredLevel)
				{
					player.sendMessage("You need level " + requiredLevel + " to take this profession.");
				}
				else if (player.getInventory().getInventoryItemCount(CommunityBoardConfig.COMMUNITYBOARD_CURRENCY, -1) < price)
				{
					player.sendMessage("Not enough currency! Needed: " + price);
				}
				else
				{
					if (price > 0)
					{
						player.destroyItemByItemId(ItemProcessType.FEE, CommunityBoardConfig.COMMUNITYBOARD_CURRENCY, price, player, true);
					}
					player.setPlayerClass(classId);
					player.sendMessage("Congratulations! You are now a " + ClassListData.getInstance().getClass(target).getClassName() + ".");
				}
			}
		}

		return buildClassMaster(player, navigation);
	}

	/**
	 * Builds the class master page.
	 */
	private static String buildClassMaster(Player player, String navigation)
	{
		final StringBuilder sb = new StringBuilder(4096);
		final PlayerClass current = player.getPlayerClass();

		sb.append("<tr><td height=10></td></tr>");
		sb.append("<tr><td align=\"center\"><font color=\"908899\">Текущая профессия:</font> <font color=\"LEVEL\">").append(ClassListData.getInstance().getClass(current).getClassName()).append("</font> <font color=\"908899\">(ур. ").append(current.level()).append(", уровень персонажа ").append(player.getLevel()).append(")</font></td></tr>");

		final List<PlayerClass> nextClasses = new ArrayList<>(current.getNextClasses());
		if (nextClasses.isEmpty())
		{
			sb.append("<tr><td height=10></td></tr>");
			sb.append("<tr><td align=\"center\"><font color=\"CDB67F\">Все профессии пройдены!</font></td></tr>");
		}
		else
		{
			sb.append("<tr><td height=8></td></tr>");
			sb.append("<tr><td><center><img src=\"L2UI.SquareGray\" width=500 height=1></center></td></tr>");
			sb.append("<tr><td height=8></td></tr>");
			sb.append("<tr><td align=\"center\"><table align=center border=0 cellspacing=6>");
			for (PlayerClass next : nextClasses)
			{
				final int requiredLevel = getRequiredLevel(next);
				final int price = getClassPrice(next);
				final boolean canChange = (player.getLevel() >= requiredLevel) && (player.getInventory().getInventoryItemCount(CommunityBoardConfig.COMMUNITYBOARD_CURRENCY, -1) >= price);
				sb.append("<tr>");
				sb.append("<td width=230 align=\"left\"><font color=\"LEVEL\">").append(ClassListData.getInstance().getClass(next).getClassName()).append("</font><br1><font color=\"908899\">Требуется уровень: ").append(requiredLevel).append("</font></td>");
				sb.append("<td width=120 align=\"center\"><font color=\"908899\">").append(price > 0 ? price + " адена" : "Бесплатно").append("</font></td>");
				sb.append("<td align=\"center\">");
				if (canChange)
				{
					sb.append(cbButton("Сменить", "_bbsclass;change;" + next.getId(), 110));
				}
				else
				{
					sb.append("<font color=\"696969\">Недоступно</font>");
				}
				sb.append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table></td></tr>");
		}

		sb.append("<tr><td height=10></td></tr>");
		sb.append("<tr><td align=\"center\"><font color=\"696969\">После смены профессии новые навыки выдаются автоматически.</font></td></tr>");

		return buildBoardShell(player, "Профессии", sb.toString(), navigation);
	}

	private static int getRequiredLevel(PlayerClass playerClass)
	{
		switch (playerClass.level())
		{
			case 1:
			{
				return 20;
			}
			case 2:
			{
				return 40;
			}
			case 3:
			{
				return 76;
			}
			default:
			{
				return 1;
			}
		}
	}

	private static int getClassPrice(PlayerClass playerClass)
	{
		switch (playerClass.level())
		{
			case 1:
			{
				return CommunityBoardConfig.COMMUNITYBOARD_CLASS_PRICE_FIRST;
			}
			case 2:
			{
				return CommunityBoardConfig.COMMUNITYBOARD_CLASS_PRICE_SECOND;
			}
			case 3:
			{
				return CommunityBoardConfig.COMMUNITYBOARD_CLASS_PRICE_THIRD;
			}
			default:
			{
				return 0;
			}
		}
	}

	/**
	 * Builds the common community board page shell with navigation.
	 */
	private static String buildBoardShell(Player player, String title, String content, String navigation)
	{
		final StringBuilder html = new StringBuilder(8192);
		html.append("<html noscrollbar><body>");
		html.append("<table width=700><tr><td height=10></td></tr></table>");
		html.append("<table width=20><tr><td>");
		if (navigation != null)
		{
			html.append(navigation);
		}
		html.append("</td><td><center>");
		html.append("<table border=0 cellpadding=0 cellspacing=0 width=555 height=455 background=\"L2UI_CT1.Windows_DF_TooltipBG\">");
		html.append("<tr><td height=25></td></tr>");
		html.append("<tr><td height=25 align=\"center\"><font name=\"hs12\" color=\"CDB67F\">").append(title).append("</font></td></tr>");
		html.append("<tr><td><center><img src=\"L2UI.SquareGray\" width=200 height=1></center></td></tr>");
		html.append(content);
		html.append("</table></center></td></tr></table></body></html>");
		return html.toString();
	}

	/**
	 * Creates a standard community board button.
	 */
	private static String cbButton(String value, String action, int width)
	{
		return "<button value=\"" + value + "\" action=\"bypass " + action + "\" width=" + width + " height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
	}

	/**
	 * Gets the Favorite links for the given player.
	 * @param player the player
	 * @return the favorite links count
	 */
	private static int getFavoriteCount(Player player)
	{
		int count = 0;
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(COUNT_FAVORITES))
		{
			ps.setInt(1, player.getObjectId());
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					count = rs.getInt("favorites");
				}
			}
		}
		catch (Exception e)
		{
			LOG.warning(FavoriteBoard.class.getSimpleName() + ": Coudn't load favorites count for " + player);
		}

		return count;
	}

	/**
	 * Gets the registered regions count for the given player.
	 * @param player the player
	 * @return the registered regions count
	 */
	private static int getRegionCount(Player player)
	{
		return 0; // TODO: Implement.
	}
}
