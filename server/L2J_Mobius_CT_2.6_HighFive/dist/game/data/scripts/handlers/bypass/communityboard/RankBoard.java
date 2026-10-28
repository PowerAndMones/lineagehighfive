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

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.config.custom.CommunityBoardConfig;
import org.l2jmobius.gameserver.handler.CommunityBoardHandler;
import org.l2jmobius.gameserver.handler.IParseBoardHandler;
import org.l2jmobius.gameserver.entity.actor.Player;

/**
 * Player ranking board (top PvP / PK / online time / richest).
 * @author Mobius
 */
public class RankBoard implements IParseBoardHandler
{
        private static final String NAVIGATION_PATH = "data/html/CommunityBoard/Custom/navigation.html";

        private static final String[] COMMANDS =
        {
                "_bbsrank"
        };

        private static final String[] TABS =
        {
                "pvp",
                "pk",
                "online",
                "rich"
        };

        private static final String[] TAB_TITLES =
        {
                "Топ PvP",
                "Топ PK",
                "Топ по онлайну",
                "Топ богачей"
        };

        @Override
        public String[] getCommandList()
        {
                return COMMANDS;
        }

        @Override
        public boolean onCommand(String command, Player player)
        {
                String tab = "pvp";
                if (command.startsWith("_bbsrank;"))
                {
                        final String requested = command.substring("_bbsrank;".length()).trim().toLowerCase();
                        for (String t : TABS)
                        {
                                if (t.equals(requested))
                                {
                                        tab = requested;
                                        break;
                                }
                        }
                }

		// Database queries must not run on the network thread.
		final String selectedTab = tab;
		ThreadPool.schedule(() ->
		{
			final String html = buildRankPage(player, selectedTab);
			if (html != null)
			{
				CommunityBoardHandler.separateAndSend(html, player);
			}
		}, 1);

                return false;
        }

        private static String buildRankPage(Player player, String tab)
        {
                final StringBuilder sb = new StringBuilder(4096);

                sb.append("<tr><td height=10></td></tr>");

                // Tabs.
                sb.append("<tr><td align=\"center\"><table align=center border=0 cellspacing=4><tr>");
                for (int i = 0; i < TABS.length; i++)
                {
                        if (TABS[i].equals(tab))
                        {
                                sb.append("<td width=125 align=\"center\"><font color=\"LEVEL\">").append(TAB_TITLES[i]).append("</font></td>");
                        }
                        else
                        {
                                sb.append("<td>").append("<button value=\"" + TAB_TITLES[i] + "\" action=\"bypass _bbsrank;" + TABS[i] + "\" width=125 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">").append("</td>");
                        }
                }
                sb.append("</tr></table></td></tr>");

                sb.append("<tr><td height=6></td></tr>");
                sb.append("<tr><td><center><img src=\"L2UI.SquareGray\" width=500 height=1></center></td></tr>");
                sb.append("<tr><td height=6></td></tr>");

                sb.append("<tr><td align=\"center\"><table align=center border=0 width=490 cellspacing=2>");
                sb.append("<tr>");
                sb.append("<td width=30 align=\"center\"><font color=\"CDB67F\">#</font></td>");
                sb.append("<td width=220 align=\"left\"><font color=\"CDB67F\">Игрок</font></td>");
                sb.append("<td width=60 align=\"center\"><font color=\"CDB67F\">Ур.</font></td>");
                sb.append("<td width=180 align=\"center\"><font color=\"CDB67F\">").append(getValueTitle(tab)).append("</font></td>");
                sb.append("</tr>");

                int rank = 0;
                try (Connection con = DatabaseFactory.getConnection())
                {
                        switch (tab)
                        {
                                case "pvp":
                                {
                                        try (PreparedStatement ps = con.prepareStatement("SELECT char_name, level, pvpkills FROM characters WHERE accesslevel = 0 ORDER BY pvpkills DESC, level DESC LIMIT 10");
                                                ResultSet rs = ps.executeQuery())
                                        {
                                                while (rs.next())
                                                {
                                                        appendRow(sb, ++rank, rs.getString(1), rs.getInt(2), String.valueOf(rs.getInt(3)));
                                                }
                                        }
                                        break;
                                }
                                case "pk":
                                {
                                        try (PreparedStatement ps = con.prepareStatement("SELECT char_name, level, pkkills FROM characters WHERE accesslevel = 0 ORDER BY pkkills DESC, level DESC LIMIT 10");
                                                ResultSet rs = ps.executeQuery())
                                        {
                                                while (rs.next())
                                                {
                                                        appendRow(sb, ++rank, rs.getString(1), rs.getInt(2), String.valueOf(rs.getInt(3)));
                                                }
                                        }
                                        break;
                                }
                                case "online":
                                {
                                        try (PreparedStatement ps = con.prepareStatement("SELECT char_name, level, onlinetime FROM characters WHERE accesslevel = 0 ORDER BY onlinetime DESC LIMIT 10");
                                                ResultSet rs = ps.executeQuery())
                                        {
                                                while (rs.next())
                                                {
                                                        final long seconds = rs.getLong(3);
                                                        appendRow(sb, ++rank, rs.getString(1), rs.getInt(2), (seconds / 3600) + " ч. " + ((seconds % 3600) / 60) + " мин.");
                                                }
                                        }
                                        break;
                                }
                                case "rich":
                                {
                                        try (PreparedStatement ps = con.prepareStatement("SELECT c.char_name, c.level, SUM(i.count) FROM items i INNER JOIN characters c ON i.owner_id = c.charId WHERE i.item_id = 57 GROUP BY i.owner_id ORDER BY SUM(i.count) DESC LIMIT 10");
                                                ResultSet rs = ps.executeQuery())
                                        {
                                                while (rs.next())
                                                {
                                                        appendRow(sb, ++rank, rs.getString(1), rs.getInt(2), String.format("%,d", rs.getLong(3)).replace(',', ' '));
                                                }
                                        }
                                        break;
                                }
                        }
                }
                catch (Exception e)
                {
                        LOG.warning(RankBoard.class.getSimpleName() + ": Couldn't load rank data: " + e);
                }

                if (rank == 0)
                {
                        sb.append("<tr><td align=\"center\" colspan=4><font color=\"908899\">Данные пока отсутствуют.</font></td></tr>");
                }

                sb.append("</table></td></tr>");

                // Shell.
                final StringBuilder html = new StringBuilder(8192);
                html.append("<html noscrollbar><body>");
                html.append("<table width=700><tr><td height=10></td></tr></table>");
                html.append("<table width=20><tr><td>");
                final String navigation = HtmCache.getInstance().getHtm(player, NAVIGATION_PATH);
                if (navigation != null)
                {
                        html.append(navigation);
                }
                html.append("</td><td><center>");
                html.append("<table border=0 cellpadding=0 cellspacing=0 width=555 height=455 background=\"L2UI_CT1.Windows_DF_TooltipBG\">");
                html.append("<tr><td height=25></td></tr>");
                html.append("<tr><td height=25 align=\"center\"><font name=\"hs12\" color=\"CDB67F\">Рейтинг игроков</font></td></tr>");
                html.append("<tr><td><center><img src=\"L2UI.SquareGray\" width=200 height=1></center></td></tr>");
                html.append(sb);
                html.append("</table></center></td></tr></table></body></html>");
                return html.toString();
        }

        private static String getValueTitle(String tab)
        {
                switch (tab)
                {
                        case "pvp":
                        {
                                return "Убийства PvP";
                        }
                        case "pk":
                        {
                                return "Убийства PK";
                        }
                        case "online":
                        {
                                return "Время в игре";
                        }
                        case "rich":
                        {
                                return "Адена";
                        }
                        default:
                        {
                                return "";
                        }
                }
        }

        private static void appendRow(StringBuilder sb, int rank, String name, int level, String value)
        {
                final String color = (rank % 2) == 0 ? "908899" : "A5A5A5";
                sb.append("<tr>");
                sb.append("<td align=\"center\"><font color=\"").append(color).append("\">").append(rank).append("</font></td>");
                sb.append("<td align=\"left\"><font color=\"").append(rank == 1 ? "LEVEL" : color).append("\">").append(escape(name)).append("</font></td>");
                sb.append("<td align=\"center\"><font color=\"").append(color).append("\">").append(level).append("</font></td>");
                sb.append("<td align=\"center\"><font color=\"").append(color).append("\">").append(value).append("</font></td>");
                sb.append("</tr>");
        }

        private static String escape(String text)
        {
                if (text == null)
                {
                        return "";
                }
                return text.replace("<", "&lt;").replace(">", "&gt;");
        }
}
