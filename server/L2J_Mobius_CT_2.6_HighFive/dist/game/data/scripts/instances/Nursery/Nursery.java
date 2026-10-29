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
package instances.Nursery;

import org.l2jmobius.gameserver.entity.actor.Creature;
import org.l2jmobius.gameserver.entity.actor.Npc;
import org.l2jmobius.gameserver.entity.actor.Player;
import org.l2jmobius.gameserver.entity.instancezone.InstanceWorld;
import org.l2jmobius.gameserver.managers.InstanceManager;
import org.l2jmobius.gameserver.mechanics.script.InstanceScript;
import org.l2jmobius.gameserver.entity.zone.ZoneType;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExSendUIEvent;

/**
 * Nursery solo instance (Salvation 140 port).<br>
 * Adapted from the L2J Mobius 5.0 Salvation reference: essence-only mechanics
 * (Maguen mini-game, energy buff skills, class categories) are replaced with a
 * straightforward kill-for-points game suitable for this codebase.<br>
 * Entry: walk into the entry zones near the Sea of Spores, or talk to Soldier Tie.
 * @author St3eT
 */
public class Nursery extends InstanceScript
{
        // NPCs
        private static final int TIE = 33152;
        private static final int[] MONSTERS =
        {
                23033, // Failed Creation
                23034, // Failed Creation
                23035, // Failed Creation
                23036, // Failed Creation
                23037, // Failed Creation
        };
        
        // Items
        private static final int REWARD_ITEM = 17602; // Tissue Energy Crystal
        
        // Misc
        private static final int TEMPLATE_ID = 171;
        private static final int MIN_LEVEL = 85;
        private static final int GAME_TIME = 1500; // 25 minutes, in seconds
        private static final int ZONE_ENTER_1 = 23601;
        private static final int ZONE_ENTER_2 = 23602;
        
        public Nursery()
        {
                addStartNpc(TIE);
                addFirstTalkId(TIE);
                addTalkId(TIE);
                addKillId(MONSTERS);
                addEnterZoneId(ZONE_ENTER_1, ZONE_ENTER_2);
        }
        
        @Override
        public void onEnterInstance(Player player, InstanceWorld world, boolean firstEntrance)
        {
                if (firstEntrance)
                {
                        world.addAllowed(player);
                        world.setParameter("GAME_STAGE", 0);
                        world.setParameter("GAME_TIME", GAME_TIME);
                        world.setParameter("GAME_POINTS", 0);
                }
        }
        
        @Override
        public String onFirstTalk(Npc npc, Player player)
        {
                final InstanceWorld world = InstanceManager.getInstance().getWorld(npc);
                if (world != null)
                {
                        switch (world.getParameters().getInt("GAME_STAGE", 0))
                        {
                                case 0:
                                {
                                        return "GameManager-01.html";
                                }
                                case 2:
                                {
                                        return "GameManager-02.html";
                                }
                                case 3:
                                {
                                        return "GameManager-03.html";
                                }
                        }
                        
                        return null;
                }
                
                // Outdoor entry NPC.
                return "33152.html";
        }
        
        @Override
        public String onEvent(String event, Npc npc, Player player)
        {
                final InstanceWorld world = InstanceManager.getInstance().getWorld(npc);
                switch (event)
                {
                        case "enter":
                        {
                                if (world == null)
                                {
                                        enterInstance(player, TEMPLATE_ID);
                                }
                                break;
                        }
                        case "startGame":
                        {
                                if ((world != null) && (world.getParameters().getInt("GAME_STAGE", 0) == 0))
                                {
                                        world.spawnGroup("GAME_MONSTERS");
                                        world.setParameter("GAME_STAGE", 1);
                                        startQuestTimer("CLOCK_TIMER", 1000, npc, player);
                                }
                                break;
                        }
                        case "calculatePoints":
                        {
                                if ((world != null) && (world.getParameters().getInt("GAME_STAGE", 0) == 2))
                                {
                                        final int gamePoints = world.getParameters().getInt("GAME_POINTS", 0);
                                        int itemCount = 0;
                                        if ((gamePoints != 0) && (gamePoints <= 200))
                                        {
                                                itemCount = 10;
                                        }
                                        else if ((gamePoints > 200) && (gamePoints <= 400))
                                        {
                                                itemCount = 30;
                                        }
                                        else if ((gamePoints > 400) && (gamePoints <= 600))
                                        {
                                                itemCount = 80;
                                        }
                                        else if ((gamePoints > 600) && (gamePoints <= 800))
                                        {
                                                itemCount = 130;
                                        }
                                        else if (gamePoints > 800)
                                        {
                                                itemCount = 200;
                                        }
                                        
                                        if ((gamePoints != 0) && (itemCount > 0))
                                        {
                                                giveItems(player, REWARD_ITEM, itemCount);
                                                addExpAndSp(player, 40000L * gamePoints, 0);
                                        }
                                        
                                        world.setParameter("GAME_STAGE", 3);
                                        finishInstance(world, 0);
                                }
                                break;
                        }
                        case "CLOCK_TIMER":
                        {
                                if ((world != null) && (world.getParameters().getInt("GAME_STAGE", 0) == 1))
                                {
                                        final int gameTime = world.getParameters().getInt("GAME_TIME", GAME_TIME) - 1;
                                        world.setParameter("GAME_TIME", gameTime);
                                        final int gamePoints = world.getParameters().getInt("GAME_POINTS", 0);
                                        for (Player member : world.getAllowed())
                                        {
                                                if ((member != null) && member.isOnline())
                                                {
                                                        member.sendPacket(new ExSendUIEvent(member, false, false, gameTime, 0, NpcStringId.ELAPSED_TIME));
                                                }
                                        }
                                        
                                        if (gameTime <= 0)
                                        {
                                                for (Npc monster : world.getAliveNpcs(MONSTERS))
                                                {
                                                        monster.deleteMe();
                                                }
                                                
                                                world.setParameter("GAME_STAGE", 2);
                                        }
                                        else
                                        {
                                                startQuestTimer("CLOCK_TIMER", 1000, npc, player);
                                        }
                                }
                                break;
                        }
                }
                
                return super.onEvent(event, npc, player);
        }
        
        @Override
        public void onKill(Npc npc, Player killer, boolean isSummon)
        {
                final InstanceWorld world = InstanceManager.getInstance().getWorld(npc);
                if (world == null)
                {
                        return;
                }
                
                int points = getRandom(6) + 3;
                if (getRandom(100) < 6)
                {
                        points += 40;
                        if (killer != null)
                        {
                                showOnScreenMsg(killer, "You have received regeneration energy! +40 bonus points", 3000);
                        }
                }
                
                world.setParameter("GAME_POINTS", world.getParameters().getInt("GAME_POINTS", 0) + points);
        }
        
        @Override
        public void onEnterZone(Creature creature, ZoneType zone)
        {
                if (creature.isPlayer())
                {
                        enterInstance(creature.asPlayer(), TEMPLATE_ID);
                }
        }
        
        @Override
        protected boolean checkConditions(Player player)
        {
                if (player.getLevel() < MIN_LEVEL)
                {
                        player.sendMessage("You must be level " + MIN_LEVEL + " or above to enter the Nursery.");
                        return false;
                }
                
                if (player.isInParty())
                {
                        player.sendMessage("The Nursery accepts only one visitor at a time. Leave your party to enter.");
                        return false;
                }
                
                final long reenterTime = InstanceManager.getInstance().getInstanceTime(player.getObjectId(), TEMPLATE_ID);
                if ((reenterTime > 0) && (reenterTime > System.currentTimeMillis()))
                {
                        player.sendMessage("You cannot enter the Nursery yet: the re-entry limit has not expired.");
                        return false;
                }
                
                return true;
        }
        
        public static void main(String[] args)
        {
                new Nursery();
        }
}
