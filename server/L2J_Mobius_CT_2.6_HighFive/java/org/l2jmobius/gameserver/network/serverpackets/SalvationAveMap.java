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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.l2jmobius.gameserver.mechanics.skill.AbnormalVisualEffect;

/**
 * Salvation (protocol 140): converts the High Five abnormal visual effect
 * bitmask into the list of 140 client ids. Generated against the reference
 * build's AbnormalVisualEffect enum.
 * @author Mobius
 */
public class SalvationAveMap
{
	private static final Map<String, Short> CLIENT_IDS = new HashMap<>();
	
	static
	{
		CLIENT_IDS.put("DOT_BLEEDING", (short) 1);
		CLIENT_IDS.put("DOT_POISON", (short) 2);
		CLIENT_IDS.put("DOT_FIRE", (short) 3);
		CLIENT_IDS.put("DOT_WATER", (short) 4);
		CLIENT_IDS.put("DOT_WIND", (short) 5);
		CLIENT_IDS.put("DOT_SOIL", (short) 6);
		CLIENT_IDS.put("STUN", (short) 7);
		CLIENT_IDS.put("SLEEP", (short) 8);
		CLIENT_IDS.put("SILENCE", (short) 9);
		CLIENT_IDS.put("ROOT", (short) 10);
		CLIENT_IDS.put("PARALYZE", (short) 11);
		CLIENT_IDS.put("FLESH_STONE", (short) 12);
		CLIENT_IDS.put("DOT_MP", (short) 13);
		CLIENT_IDS.put("BIG_HEAD", (short) 14);
		CLIENT_IDS.put("DOT_FIRE_AREA", (short) 15);
		CLIENT_IDS.put("CHANGE_TEXTURE", (short) 16);
		CLIENT_IDS.put("BIG_BODY", (short) 17);
		CLIENT_IDS.put("FLOATING_ROOT", (short) 18);
		CLIENT_IDS.put("DANCE_ROOT", (short) 19);
		CLIENT_IDS.put("GHOST_STUN", (short) 20);
		CLIENT_IDS.put("STEALTH", (short) 21);
		CLIENT_IDS.put("SEIZURE1", (short) 22);
		CLIENT_IDS.put("SEIZURE2", (short) 23);
		CLIENT_IDS.put("MAGIC_SQUARE", (short) 24);
		CLIENT_IDS.put("FREEZING", (short) 25);
		CLIENT_IDS.put("SHAKE", (short) 26);
		CLIENT_IDS.put("ULTIMATE_DEFENCE", (short) 28);
		CLIENT_IDS.put("VP_UP", (short) 29);
		CLIENT_IDS.put("REAL_TARGET", (short) 30);
		CLIENT_IDS.put("DEATH_MARK", (short) 31);
		CLIENT_IDS.put("TURN_FLEE", (short) 32);
		CLIENT_IDS.put("VP_KEEP", (short) 29);
		CLIENT_IDS.put("INVINCIBILITY", (short) 33);
		CLIENT_IDS.put("AIR_BATTLE_SLOW", (short) 34);
		CLIENT_IDS.put("AIR_BATTLE_ROOT", (short) 35);
		CLIENT_IDS.put("CHANGE_WP", (short) 36);
		CLIENT_IDS.put("CHANGE_HAIR_G", (short) 37);
		CLIENT_IDS.put("CHANGE_HAIR_P", (short) 38);
		CLIENT_IDS.put("CHANGE_HAIR_B", (short) 39);
		CLIENT_IDS.put("STIGMA_OF_SILEN", (short) 41);
		CLIENT_IDS.put("SPEED_DOWN", (short) 42);
		CLIENT_IDS.put("FROZEN_PILLAR", (short) 43);
		CLIENT_IDS.put("CHANGE_VES_S", (short) 44);
		CLIENT_IDS.put("CHANGE_VES_C", (short) 45);
		CLIENT_IDS.put("CHANGE_VES_D", (short) 46);
		CLIENT_IDS.put("TIME_BOMB", (short) 47);
		CLIENT_IDS.put("MP_SHIELD", (short) 48);
		CLIENT_IDS.put("NAVIT_ADVENT", (short) 52);
		CLIENT_IDS.put("BR_SOUL_AVATAR", (short) 64);
	}
	
	public static List<Short> toClientIds(int h5Mask, boolean addStealth)
	{
		final List<Short> result = new ArrayList<>();
		for (AbnormalVisualEffect ave : AbnormalVisualEffect.values())
		{
			if ((ave.getMask() != 0) && ((h5Mask & ave.getMask()) != 0))
			{
				final Short clientId = CLIENT_IDS.get(ave.name());
				if (clientId != null)
				{
					result.add(clientId);
				}
			}
		}
		
		if (addStealth)
		{
			final Short stealth = CLIENT_IDS.get("STEALTH");
			if ((stealth != null) && !result.contains(stealth))
			{
				result.add(stealth);
			}
		}
		
		return result;
	}
}
