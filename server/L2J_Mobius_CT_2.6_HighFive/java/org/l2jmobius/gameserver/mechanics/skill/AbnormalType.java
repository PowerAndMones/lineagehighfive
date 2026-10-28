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
package org.l2jmobius.gameserver.mechanics.skill;

import java.util.HashMap;
import java.util.Map;

/**
 * Abnormal type enumerate.
 * @author Zoey76, Mobius
 */
public enum AbnormalType
{
	ABILITY_CHANGE,
	ABNORMAL_INVINCIBILITY,
	ABNORMAL_ITEM,
	AB_HAWK_EYE,
	ALL_ATTACK_DOWN,
	ALL_REGEN_UP,
	ALL_SPEED_DOWN,
	ANESTHESIA,
	ANTARAS_DEBUFF,
	APELLA,
	ARCHER_SPECIAL,
	ARCHER_SPECIAL_I,
	ARMOR_EARTH,
	ARMOR_FIRE,
	ARMOR_HOLY,
	ARMOR_UNHOLY,
	ARMOR_WATER,
	ARMOR_WIND,
	ARROW_RAIN,
	ATTACK_SPEED_UP_BOW,
	ATTACK_TIME_DOWN,
	ATTACK_TIME_DOWN_SPECIAL,
	ATTACK_TIME_UP,
	ATTRIBUTE_POTION,
	AVOID_DOWN,
	AVOID_SKILL,
	AVOID_UP,
	AVOID_UP_SPECIAL,
	BERSERKER,
	BETRAYAL_MARK,
	BIG_BODY,
	BIG_HEAD,
	BLEEDING,
	BLESS_THE_BLOOD,
	BLOCK_RESURRECTION,
	BLOCK_SHIELD_UP,
	BLOCK_SPEED_UP,
	BLOCK_TRANSFORM,
	BLOOD_CONSTRACT,
	BOT_PENALTY,
	BR_EVENT_BUF1,
	BR_EVENT_BUF10,
	BR_EVENT_BUF2,
	BR_EVENT_BUF3,
	BR_EVENT_BUF5,
	BR_EVENT_BUF6,
	BR_EVENT_BUF7,
	BUFF_QUEEN_OF_CAT,
	BUFF_UNICORN_SERAPHIM,
	CANCEL_PROB_DOWN,
	CASTING_TIME_DOWN,
	CASTING_TIME_UP,
	CHEAP_MAGIC,
	COMBINATION,
	COUNTER_CRITICAL,
	COUNTER_CRITICAL_TRIGGER,
	COUNTER_SKILL,
	CP_UP,
	CRITICAL_DMG_DOWN,
	CRITICAL_DMG_UP,
	CRITICAL_POISON,
	CRITICAL_PROB_DOWN,
	CRITICAL_PROB_UP,
	CURSE_LIFE_FLOW,
	DAMAGE_AMPLIFY,
	DANCE_DEFENCE_MOTION1,
	DANCE_OF_ALIGNMENT,
	DANCE_OF_AQUA_GUARD,
	DANCE_OF_BERSERKER,
	DANCE_OF_BLADESTORM,
	DANCE_OF_CONCENTRATION,
	DANCE_OF_EARTH_GUARD,
	DANCE_OF_FIRE,
	DANCE_OF_FURY,
	DANCE_OF_INSPIRATION,
	DANCE_OF_LIGHT,
	DANCE_OF_MYSTIC,
	DANCE_OF_PROTECTION,
	DANCE_OF_SHADOW,
	DANCE_OF_SIREN,
	DANCE_OF_VAMPIRE,
	DANCE_OF_WARRIOR,
	DARK_SEED,
	DD_RESIST,
	DEATHWORM,
	DEATH_CLACK,
	DEATH_MARK,
	DEATH_PENALTY,
	DEBUFF_NIGHTSHADE,
	DEBUFF_SHIELD,
	DECREASE_WEIGHT_PENALTY,
	DERANGEMENT,
	DETECT_WEAKNESS,
	DISARM,
	DISTRUST,
	DMG_SHIELD,
	DOT_ATTR,
	DOT_MP,
	DRAGON_BUFF,
	DRAGON_BREATH,
	DUELIST_SPIRIT,
	DWARF_ATTACK_BUFF,
	DWARF_DEFENCE_BUFF,
	ELEMENTAL_ARMOR,
	ENERVATION,
	ENTRY_FOR_GAME,
	EVASION_BUFF,
	EVENT_GAWI,
	EVENT_SANTA_REWARD,
	EVENT_TERRITORY,
	EVENT_WIN,
	EVIL_BLOOD,
	EXPOSE_WEAK_POINT,
	FATAL_POISON,
	FINAL_SECRET,
	FIRE_DOT,
	FISHING_MASTERY_DOWN,
	FLAME_HAWK,
	FLY_AWAY,
	FOCUS_DAGGER,
	FORCE_MEDITATION,
	FORCE_OF_DESTRUCTION,
	FREEZING,
	GHOST_PIERCING,
	HEAL_EFFECT_DOWN,
	HEAL_EFFECT_UP,
	HEAL_POWER_UP,
	HERO_BUFF,
	HERO_DEBUFF,
	HIDE,
	HIT_DOWN,
	HIT_UP,
	HOLY_ATTACK,
	HOT_GROUND,
	HP_RECOVER,
	HP_REGEN_DOWN,
	HP_REGEN_UP,
	IMPROVE_CRT_RATE_DMG_UP,
	IMPROVE_HIT_DEFENCE_CRT_RATE_UP,
	IMPROVE_HP_MP_UP,
	IMPROVE_MA_MD_UP,
	IMPROVE_PA_PD_UP,
	IMPROVE_SHIELD_RATE_DEFENCE_UP,
	IMPROVE_SPEED_AVOID_UP,
	IMPROVE_VAMPIRIC_HASTE,
	INSTINCT,
	INVINCIBILITY,
	IRON_SHIELD,
	IRON_SHIELD_I,
	KAMAEL_SPECIAL,
	KNIGHT_AURA,
	LIFE_FORCE_KAMAEL,
	LIFE_FORCE_ORC,
	LIFE_FORCE_OTHERS,
	LIMIT,
	MAGICAL_STANCE,
	MAGIC_CRITICAL_UP,
	MAJESTY,
	MAXIMUM_ABILITY,
	MAX_BREATH_UP,
	MAX_HP_DOWN,
	MAX_HP_UP,
	MAX_HP_CP_UP,
	MAX_MP_UP,
	MA_DOWN,
	MA_MD_UP,
	MA_UP,
	MA_UP_HERB,
	MA_UP_SPECIAL,
	MD_DOWN,
	MD_UP,
	MD_UP_ATTR,
	MENTAL_IMPOVERISH,
	METEOR,
	MIGHT_MORTAL,
	MIRAGE,
	MIRAGE_TRAP,
	MP_COST_DOWN,
	MP_COST_UP,
	MP_REGEN_UP,
	MULTI_BUFF,
	MULTI_BUFF_A,
	MULTI_DEBUFF,
	MULTI_DEBUFF_A,
	MULTI_DEBUFF_B,
	MULTI_DEBUFF_C,
	MULTI_DEBUFF_D,
	MULTI_DEBUFF_E,
	MULTI_DEBUFF_F,
	MULTI_DEBUFF_FIRE,
	MULTI_DEBUFF_G,
	MULTI_DEBUFF_HOLY,
	MULTI_DEBUFF_SOUL,
	MULTI_DEBUFF_UNHOLY,
	MULTI_DEBUFF_WATER,
	MULTI_DEBUFF_WIND,
	NONE,
	NORMAL_ATTACK_BLOCK,
	OBLIVION,
	PARALYZE,
	PATIENCE,
	PA_DOWN,
	PA_PD_UP,
	PA_UP,
	PA_UP_HERB,
	PA_UP_SPECIAL,
	PD_DOWN,
	PD_UP,
	PD_UP_BOW,
	PD_UP_SPECIAL,
	PHYSICAL_STANCE,
	PINCH,
	PK_PROTECT,
	POISON,
	POLEARM_ATTACK,
	POSSESSION,
	POSSESSION_SPECIAL, // Used in High Five skills.
	POTION_OF_GENESIS,
	PRESERVE_ABNORMAL,
	PROTECTION,
	PUBLIC_SLOT,
	PVP_DMG_DOWN,
	PVP_WEAPON_BUFF,
	PVP_WEAPON_DEBUFF,
	RAGE_MIGHT,
	REAL_TARGET,
	RECHARGE_UP,
	REDUCE_DROP_PENALTY,
	REFLECT_ABNORMAL,
	REFLECT_MAGIC_DD,
	RESIST_BLEEDING,
	RESIST_DEBUFF_DISPEL,
	RESIST_DERANGEMENT,
	RESIST_HOLY_UNHOLY,
	RESIST_POISON,
	RESIST_SHOCK,
	RESIST_SPIRITLESS,
	RESURRECTION_SPECIAL,
	REUSE_DELAY_DOWN,
	REUSE_DELAY_UP,
	ROOT_MAGICALLY,
	ROOT_PHYSICALLY,
	SEED_OF_CRITICAL,
	SEED_OF_KNIGHT,
	SEIZURE_A,
	SEIZURE_B,
	SEIZURE_C,
	SEIZURE_PENALTY,
	SHIELD_DEFENCE_UP,
	SHIELD_PROB_UP,
	SIGNAL_A,
	SIGNAL_B,
	SIGNAL_C,
	SIGNAL_D,
	SIGNAL_E,
	SILENCE,
	SILENCE_ALL,
	SILENCE_PHYSICAL,
	SKILL_IGNORE,
	SLEEP,
	SNIPE,
	SOA_BUFF1,
	SOA_BUFF2,
	SOA_BUFF3,
	SONG_BATTLE_WHISPER,
	SONG_OF_CHAMPION,
	SONG_OF_EARTH,
	SONG_OF_ELEMENTAL,
	SONG_OF_FLAME_GUARD,
	SONG_OF_HUNTER,
	SONG_OF_INVOCATION,
	SONG_OF_LIFE,
	SONG_OF_MEDITATION,
	SONG_OF_PURIFICATION,
	SONG_OF_RENEWAL,
	SONG_OF_STORM_GUARD,
	SONG_OF_VENGEANCE,
	SONG_OF_VITALITY,
	SONG_OF_WARDING,
	SONG_OF_WATER,
	SONG_OF_WIND,
	SONG_OF_WINDSTORM,
	SPA_DISEASE_A,
	SPA_DISEASE_B,
	SPA_DISEASE_C,
	SPA_DISEASE_D,
	SPEED_DOWN,
	SPEED_UP,
	SPEED_UP_SPECIAL,
	SPITE,
	SPOIL_BOMB,
	SSQ_TOWN_BLESSING,
	SSQ_TOWN_CURSE,
	STAR_FALL,
	STEALTH,
	STIGMA_A,
	STIGMA_OF_SILEN,
	STUN,
	SUB_TRIGGER_CRT_RATE_UP,
	SUB_TRIGGER_DEFENCE,
	SUB_TRIGGER_HASTE,
	SUB_TRIGGER_SPIRIT,
	SUMMON_CONDITION,
	TALISMAN,
	TARGET_LOCK,
	THIN_SKIN,
	THRILL_FIGHT,
	TIME_CHECK,
	TOUCH_OF_DEATH,
	TOUCH_OF_LIFE,
	TRANSFER_DAMAGE,
	TRANSFORM,
	TRANSFORM_HANGOVER,
	TRANSFORM_SCRIFICE,
	TRANSFORM_SCRIFICE_P,
	TURN_FLEE,
	TURN_PASSIVE,
	TURN_STONE,
	T_CRT_DMG_DOWN,
	T_CRT_DMG_UP,
	T_CRT_RATE_UP,
	ULTIMATE_BUFF,
	ULTIMATE_DEBUFF,
	VALAKAS_ITEM,
	VAMPIRIC_ATTACK,
	VAMPIRIC_ATTACK_SPECIAL,
	VIBRATION,
	VOTE,
	VP_KEEP,
	VP_UP,
	WATCHER_GAZE,
	WATER_DOT,
	WEAK_CONSTITUTION,
	WEAPON_MASTERY,
	WILL,
	WIND_DOT,
	WP_CHANGE_EVENT;
	
	/**
	 * Get the enumerate for the given String.
	 * @param type the abnormal type to get
	 * @return the abnormal type
	 */
	public static AbnormalType getAbnormalType(String type)
	{
		try
		{
			return Enum.valueOf(AbnormalType.class, type);
		}
		catch (Exception e)
		{
			return NONE;
		}
	}
	
	/**
	 * Verify if this enumerate is default.
	 * @return {@code true} if this enumerate is none, {@code false} otherwise
	 */
	public boolean isNone()
	{
		return this == NONE;
	}

	// Salvation (protocol 140): client ids for buff stacking groups, ported from the reference.
	private static final Map<String, Integer> CLIENT_IDS = new HashMap<>();
	static
	{
		Map<String, Integer> map = CLIENT_IDS;
		map.put("NONE", -1);
		map.put("ABILITY_CHANGE", -1);
		map.put("ABSORB", -1);
		map.put("ACADEMY_UP", -1);
		map.put("ANTI_SUMMON", -1);
		map.put("ARCANE_PROTECTION", -1);
		map.put("ARMOR_ELEMENT_ALL", -1);
		map.put("ATTACK_TIME_DOWN_SPECIAL", -1);
		map.put("AURA", -1);
		map.put("AVOID_SKILL_SPECIAL", -1);
		map.put("ASSASSINS_REFLEX", -1);
		map.put("BARRIER", -1);
		map.put("BATTLE_CRY", -1);
		map.put("BATTLE_TOLERANCE", -1);
		map.put("BIG_BODY_COMBINATION", -1);
		map.put("BLESS_THE_BLOOD", -1);
		map.put("BLOCK_ESCAPE", -1);
		map.put("BLOCK_INVINCIBILITY", -1);
		map.put("BLOCK_RESURRECTION", -1);
		map.put("BLOCK_SHIELD_UP", -1);
		map.put("BLOCK_SPEED_UP", -1);
		map.put("BLOCK_TRANSFORM", -1);
		map.put("BODY_DESTRUCTION", -1);
		map.put("BOT_PENALTY", -1);
		map.put("BR_EVENT_BUF1", -1);
		map.put("BR_EVENT_BUF2", -1);
		map.put("BR_EVENT_BUF3", -1);
		map.put("BR_EVENT_BUF4", -1);
		map.put("BR_EVENT_BUF6", -1);
		map.put("BR_EVENT_BUF7", -1);
		map.put("BRIGHTNESS_BLESS", -1);
		map.put("BUFF_MENTEE1", -1);
		map.put("BUFF_PCCAFE_EXP1", -1);
		map.put("BUFF_SPECIAL_ATTACK", -1);
		map.put("BUFF_SPECIAL_AURA", -1);
		map.put("BUFF_SPECIAL_CLASS", -1);
		map.put("BUFF_SPECIAL_CONDITION", -1);
		map.put("BUFF_SPECIAL_CRITICAL", -1);
		map.put("BUFF_SPECIAL_DEFENCE", -1);
		map.put("BUFF_SPECIAL_HITAVOID", -1);
		map.put("BUFF_SPECIAL_MOVE", -1);
		map.put("BUFF_SPECIAL_MULTI", -1);
		map.put("BUFF_TREE", -1);
		map.put("CHANGEBODY", -1);
		map.put("CLAN_ADVENT", -1);
		map.put("CLAN_BOUNDARY", -1);
		map.put("CLAN_FRIEND", -1);
		map.put("CLAN_PRISON", -1);
		map.put("CLASS_CHANGE", -1);
		map.put("CONFUSION", -1);
		map.put("COUNTER_CRITICAL", -1);
		map.put("COUNTER_FLIP", -1);
		map.put("COUNTER_CRITICAL_TRIGGER", -1);
		map.put("COMPELLING_WIND", -1);
		map.put("CRIPPLING_DANCE", -1);
		map.put("CRITICAL_SPECIAL", -1);
		map.put("CURIOUS_HOUSE", -1);
		map.put("CURSE_EXPOSURE", -1);
		map.put("DAMAGE_AMPLIFY", -1);
		map.put("DANCE_OF_BERSERKER", -1);
		map.put("DARK_BREAKER", -1);
		map.put("DC_MOD", -1);
		map.put("DD_RESIST", -1);
		map.put("DEATH_PENALTY_BLOCK", -1);
		map.put("DEATH_PENALTY_GD", -1);
		map.put("DEATHWORM", -1);
		map.put("DEPORT", -1);
		map.put("DISTRUST", -1);
		map.put("DITTY", -1);
		map.put("DIVINE_STORM", -1);
		map.put("DUAL_ATTACK_UP", -1);
		map.put("DUAL_DEFENCE_UP", -1);
		map.put("DUAL_DMG_SHIELD", -1);
		map.put("DUAL_SKILL_UP", -1);
		map.put("DWARF_ATTACK_BUFF", -1);
		map.put("DWARF_DEFENCE_BUFF", -1);
		map.put("EASTERLY_WIND_STRIKE", -1);
		map.put("EINHASADS_NECKLACE", -1);
		map.put("ENCHANTER_MOD", -1);
		map.put("ENERGY_OF_TOTEM_1", -1);
		map.put("ENERGY_OF_TOTEM_2", -1);
		map.put("ENERGY_OF_TOTEM_3", -1);
		map.put("ENERGY_OF_TOTEM_4", -1);
		map.put("EVAS_EARRING", -1);
		map.put("EVENT_GAWI", -1);
		map.put("EVENT_SANTA_REWARD", -1);
		map.put("EVENT_TERRITORY", -1);
		map.put("EVENT_WIN", -1);
		map.put("EXP_BOTTLE", -1);
		map.put("EXP_SPECIAL", -1);
		map.put("EYE_STORM", -1);
		map.put("FINAL_SECRET", -1);
		map.put("FISH_STEW", -1);
		map.put("FLAG_BUF", -1);
		map.put("FLAG_DEBUF", -1);
		map.put("FREEZE", -1);
		map.put("FREEZE_SLOW", -1);
		map.put("FORCE_HP_UP", -1);
		map.put("GREATER_SERVITOR_BUFF", -1);
		map.put("GRAN_KAINS_NECKLACE", -1);
		map.put("HIDE", -1);
		map.put("IMPROVE_HIT_DEFENCE_CRT_RATE_UP", -1);
		map.put("INSTANT_EV_BUFF1", -1);
		map.put("INSTANT_EV_BUFF2", -1);
		map.put("INSTANT_EV_BUFF6", -1);
		map.put("INVINCIBILITY_SPECIAL", -1);
		map.put("KALIE_BUFF", -1);
		map.put("KNIGHT_AURA", -1);
		map.put("KNIGHT_SHIELD", -1);
		map.put("LIFE_FORCE_HEALER", -1);
		map.put("LIFE_FORCE_HEALER_SELF", -1);
		map.put("LUMIERE_BUFF", -1);
		map.put("MANA_BURST", -1);
		map.put("MAPHRS_RING", -1);
		map.put("MARK_DEBUF_A", -1);
		map.put("MARK_DEBUF_B", -1);
		map.put("MARK_DEBUF_C", -1);
		map.put("MARK_DEBUF_D", -1);
		map.put("MARK_OF_LUMI", -1);
		map.put("MARK_OF_WEAKNESS", -1);
		map.put("MARK_OF_PLAGUE", -1);
		map.put("MARK_OF_TRICK", -1);
		map.put("MAX_HP_UP_K", -1);
		map.put("MD_DOWN_AWAKEN", -1);
		map.put("MORALE_UP", -1);
		map.put("MOTION_OF_DEFENCE", -1);
		map.put("MOVEMENT", -1);
		map.put("MP_SHIELD", -1);
		map.put("MULTI_BUFF_A", -1);
		map.put("MULTI_DEBUFF_A", -1);
		map.put("MULTI_DEBUFF_B", -1);
		map.put("MULTI_DEBUFF_C", -1);
		map.put("MULTI_DEBUFF_D", -1);
		map.put("MULTI_DEBUFF_E", -1);
		map.put("MULTI_DEBUFF_F", -1);
		map.put("MULTI_DEBUFF_G", -1);
		map.put("NOCHAT", -1);
		map.put("NOTICE_PORTAL", -1);
		map.put("NPC_ATTACK1", -1);
		map.put("NPC_ATTACK2", -1);
		map.put("NPC_ATTACK3", -1);
		map.put("NPC_FURY", -1);
		map.put("NPC_MULTI_BUFF1", -1);
		map.put("PATIENCE", -1);
		map.put("PET_FURY", -1);
		map.put("PAAGRIOS_EARRING", -1);
		map.put("POSSESSION_SPECIAL", -1);
		map.put("POWER_BLUFF", -1);
		map.put("PVP_WEAPON_BUFF", -1);
		map.put("PVP_WEAPON_DEBUFF", -1);
		map.put("RACE_DARKELF1", -1);
		map.put("RACE_DWAF1", -1);
		map.put("RACE_ELF1", -1);
		map.put("RACE_HUMAN1", -1);
		map.put("RACE_KAMAEL1", -1);
		map.put("RACE_ORC1", -1);
		map.put("RACE_ORC2", -1);
		map.put("REFLECT_MAGIC_DD", -1);
		map.put("RESIST_DEATH", -1);
		map.put("RESIST_CHEAT_DEATH", -1);
		map.put("RHAPSODY", -1);
		map.put("SACRIFICE", -1);
		map.put("SAYHA_FURY", -1);
		map.put("SAYHAS_RING", -1);
		map.put("SHADOW_FLASH", -1);
		map.put("SEED_OF_CRITICAL", -1);
		map.put("SEED_TALISMAN1", -1);
		map.put("SHIELD_ATTACK", -1);
		map.put("SHILLIEN_PROTECTION", -1);
		map.put("SHILENS_EARRING", -1);
		map.put("SHILENS_RING", -1);
		map.put("SHILENS_NECKLACE", -1);
		map.put("SHOOTING_STANCE", -1);
		map.put("SIGNAL_A", -1);
		map.put("SIGNAL_B", -1);
		map.put("SIGNAL_C", -1);
		map.put("SIGNAL_D", -1);
		map.put("SIGNAL_E", -1);
		map.put("SKILL_IGNORE", -1);
		map.put("SQUALL", -1);
		map.put("SOA_BUFF1", -1);
		map.put("SOA_BUFF2", -1);
		map.put("SOA_BUFF3", -1);
		map.put("SONG_OF_PURIFICATION", -1);
		map.put("SONG_OF_THIEF", -1);
		map.put("SONG_OF_WISDOM", -1);
		map.put("SPECIAL_BERSERKER", -1);
		map.put("SPECIAL_MOVE_UP", -1);
		map.put("SPECIAL_RIDE", -1);
		map.put("SPIRIT_SHARING", -1);
		map.put("STAR_AGATHION_EXP_SP_BUFF1", -1);
		map.put("STIGMA_A", -1);
		map.put("STIGMA_OF_SILEN", -1);
		map.put("STORM_SIGN", -1);
		map.put("SUBSTITUTE_BUFF", -1);
		map.put("SUMMONER_LINK", -1);
		map.put("SUPER_AVOID", -1);
		map.put("SUPER_BUFF", -1);
		map.put("SUPER_MOVE", -1);
		map.put("TALISMAN_TRIGGER_SPECIAL", -1);
		map.put("TIME_BOMB", -1);
		map.put("TURN_CRYSTAL", -1);
		map.put("VAMPIRIC_ATTACK_SPECIAL", -1);
		map.put("VIBRATION", -1);
		map.put("VOTE", -1);
		map.put("VP_CHANGE", -1);
		map.put("VP_KEEP", -1);
		map.put("VP_MENTOR_RUNE", -1);
		map.put("VP_UP", -1);
		map.put("WEAKENED_DEATH_PENALTY", -1);
		map.put("WEAPON_MASTER_SPECIAL", -1);
		map.put("WIND_BLEND", -1);
		map.put("WISPERING_OF_BATTLE", -1);
		map.put("WP_CHANGE_EVENT", -1);
		map.put("CLAN_TEAMWORK", -1);
		map.put("SONG_OF_ARCHERY", -1);
		map.put("DANCE_OF_SAGE", -1);
		map.put("SAYHA_BLESSING", -1);
		map.put("SAYHA_BUFF", -1);
		map.put("SONG_WEAPON", -1);
		map.put("HEAL_RESISTANCE", -1);
		map.put("TRIAD", -1);
		map.put("TALISMAN_SPECIAL", -1);
		map.put("EXP_HERB", -1);
		map.put("AURA_DEBUFF", -1);
		map.put("SHINING_HOURGLASS", -1);
		map.put("AB_HAWK_EYE", 0);
		map.put("ALL_ATTACK_DOWN", 1);
		map.put("ALL_ATTACK_UP", 2);
		map.put("ALL_SPEED_DOWN", 3);
		map.put("ALL_SPEED_UP", 4);
		map.put("ANTARAS_DEBUFF", 5);
		map.put("ARMOR_EARTH", 6);
		map.put("ARMOR_FIRE", 7);
		map.put("ARMOR_HOLY", 8);
		map.put("ARMOR_UNHOLY", 9);
		map.put("ARMOR_WATER", 10);
		map.put("ARMOR_WIND", 11);
		map.put("ATTACK_SPEED_UP_BOW", 12);
		map.put("ATTACK_TIME_DOWN", 13);
		map.put("ATTACK_TIME_UP", 14);
		map.put("AVOID_DOWN", 15);
		map.put("AVOID_UP", 16);
		map.put("AVOID_UP_SPECIAL", 17);
		map.put("BERSERKER", 18);
		map.put("BIG_BODY", 19);
		map.put("BIG_HEAD", 20);
		map.put("BLEEDING", 21);
		map.put("BOW_RANGE_UP", 22);
		map.put("BUFF_QUEEN_OF_CAT", 23);
		map.put("BUFF_UNICORN_SERAPHIM", 24);
		map.put("CANCEL_PROB_DOWN", 25);
		map.put("CASTING_TIME_DOWN", 26);
		map.put("CASTING_TIME_UP", 27);
		map.put("CHEAP_MAGIC", 28);
		map.put("CRITICAL_DMG_DOWN", 29);
		map.put("CRITICAL_DMG_UP", 30);
		map.put("CRITICAL_PROB_DOWN", 31);
		map.put("CRITICAL_PROB_UP", 32);
		map.put("DANCE_OF_AQUA_GUARD", 33);
		map.put("DANCE_OF_CONCENTRATION", 34);
		map.put("DANCE_OF_EARTH_GUARD", 35);
		map.put("DANCE_OF_FIRE", 36);
		map.put("DANCE_OF_FURY", 37);
		map.put("DANCE_OF_INSPIRATION", 38);
		map.put("DANCE_OF_LIGHT", 39);
		map.put("DANCE_OF_MYSTIC", 40);
		map.put("DANCE_OF_PROTECTION", 41);
		map.put("DANCE_OF_SHADOW", 42);
		map.put("DANCE_OF_SIREN", 43);
		map.put("DANCE_OF_VAMPIRE", 44);
		map.put("DANCE_OF_WARRIOR", 45);
		map.put("DEBUFF_NIGHTSHADE", 46);
		map.put("DEBUFF_SHIELD", 47);
		map.put("DECREASE_WEIGHT_PENALTY", 48);
		map.put("DERANGEMENT", 49);
		map.put("DETECT_WEAKNESS", 50);
		map.put("DMG_SHIELD", 51);
		map.put("DOT_ATTR", 52);
		map.put("DOT_MP", 53);
		map.put("DRAGON_BREATH", 54);
		map.put("DUELIST_SPIRIT", 55);
		map.put("FATAL_POISON", 56);
		map.put("FISHING_MASTERY_DOWN", 57);
		map.put("FLY_AWAY", 58);
		map.put("FOCUS_DAGGER", 59);
		map.put("HEAL_EFFECT_DOWN", 60);
		map.put("HEAL_EFFECT_UP", 61);
		map.put("HERO_BUFF", 62);
		map.put("HERO_DEBUFF", 63);
		map.put("HIT_DOWN", 64);
		map.put("HIT_UP", 65);
		map.put("HOLY_ATTACK", 66);
		map.put("HP_RECOVER", 67);
		map.put("HP_REGEN_DOWN", 68);
		map.put("HP_REGEN_UP", 69);
		map.put("LIFE_FORCE_ORC", 70);
		map.put("LIFE_FORCE_OTHERS", 71);
		map.put("MAGIC_CRITICAL_UP", 72);
		map.put("MAJESTY", 73);
		map.put("MAX_BREATH_UP", 74);
		map.put("MAX_HP_DOWN", 75);
		map.put("MAX_HP_UP", 76);
		map.put("MAX_MP_UP", 77);
		map.put("MA_DOWN", 78);
		map.put("MA_UP", 79);
		map.put("MA_UP_HERB", 80);
		map.put("MD_DOWN", 81);
		map.put("MD_UP", 82);
		map.put("MD_UP_ATTR", 83);
		map.put("MIGHT_MORTAL", 84);
		map.put("MP_COST_DOWN", 85);
		map.put("MP_COST_UP", 86);
		map.put("MP_RECOVER", 87);
		map.put("MP_REGEN_UP", 88);
		map.put("MULTI_BUFF", 89);
		map.put("MULTI_DEBUFF", 90);
		map.put("PARALYZE", 91);
		map.put("PA_DOWN", 92);
		map.put("PA_PD_UP", 93);
		map.put("PA_UP", 94);
		map.put("PA_UP_HERB", 95);
		map.put("PA_UP_SPECIAL", 96);
		map.put("PD_DOWN", 97);
		map.put("PD_UP", 98);
		map.put("PD_UP_BOW", 99);
		map.put("PD_UP_SPECIAL", 100);
		map.put("PINCH", 101);
		map.put("POISON", 102);
		map.put("POLEARM_ATTACK", 103);
		map.put("POSSESSION", 104);
		map.put("PRESERVE_ABNORMAL", 105);
		map.put("PUBLIC_SLOT", 106);
		map.put("RAGE_MIGHT", 107);
		map.put("REDUCE_DROP_PENALTY", 108);
		map.put("REFLECT_ABNORMAL", 109);
		map.put("RESIST_BLEEDING", 110);
		map.put("RESIST_DEBUFF_DISPEL", 111);
		map.put("RESIST_DERANGEMENT", 112);
		map.put("RESIST_HOLY_UNHOLY", 113);
		map.put("RESIST_POISON", 114);
		map.put("RESIST_SHOCK", 115);
		map.put("RESIST_SPIRITLESS", 116);
		map.put("REUSE_DELAY_DOWN", 117);
		map.put("REUSE_DELAY_UP", 118);
		map.put("ROOT_PHYSICALLY", 119);
		map.put("ROOT_MAGICALLY", 120);
		map.put("SHIELD_DEFENCE_UP", 121);
		map.put("SHIELD_PROB_UP", 122);
		map.put("SILENCE", 123);
		map.put("SILENCE_ALL", 124);
		map.put("SILENCE_PHYSICAL", 125);
		map.put("SLEEP", 126);
		map.put("SNIPE", 127);
		map.put("SONG_OF_CHAMPION", 128);
		map.put("SONG_OF_EARTH", 129);
		map.put("SONG_OF_FLAME_GUARD", 130);
		map.put("SONG_OF_HUNTER", 131);
		map.put("SONG_OF_INVOCATION", 132);
		map.put("SONG_OF_LIFE", 133);
		map.put("SONG_OF_MEDITATION", 134);
		map.put("SONG_OF_RENEWAL", 135);
		map.put("SONG_OF_STORM_GUARD", 136);
		map.put("SONG_OF_VENGEANCE", 137);
		map.put("SONG_OF_VITALITY", 138);
		map.put("SONG_OF_WARDING", 139);
		map.put("SONG_OF_WATER", 140);
		map.put("SONG_OF_WIND", 141);
		map.put("SPA_DISEASE_A", 142);
		map.put("SPA_DISEASE_B", 143);
		map.put("SPA_DISEASE_C", 144);
		map.put("SPA_DISEASE_D", 145);
		map.put("SPEED_DOWN", 146);
		map.put("SPEED_UP", 147);
		map.put("SPEED_UP_SPECIAL", 148);
		map.put("SSQ_TOWN_BLESSING", 149);
		map.put("SSQ_TOWN_CURSE", 150);
		map.put("STEALTH", 151);
		map.put("STUN", 152);
		map.put("THRILL_FIGHT", 153);
		map.put("TOUCH_OF_DEATH", 154);
		map.put("TOUCH_OF_LIFE", 155);
		map.put("TURN_FLEE", 156);
		map.put("TURN_PASSIVE", 157);
		map.put("TURN_STONE", 158);
		map.put("ULTIMATE_BUFF", 159);
		map.put("ULTIMATE_DEBUFF", 160);
		map.put("VALAKAS_ITEM", 161);
		map.put("VAMPIRIC_ATTACK", 162);
		map.put("WATCHER_GAZE", 163);
		map.put("RESURRECTION_SPECIAL", 164);
		map.put("COUNTER_SKILL", 165);
		map.put("AVOID_SKILL", 166);
		map.put("CP_UP", 167);
		map.put("CP_DOWN", 168);
		map.put("CP_REGEN_UP", 169);
		map.put("CP_REGEN_DOWN", 170);
		map.put("INVINCIBILITY", 171);
		map.put("ABNORMAL_INVINCIBILITY", 172);
		map.put("PHYSICAL_STANCE", 173);
		map.put("MAGICAL_STANCE", 174);
		map.put("COMBINATION", 175);
		map.put("ANESTHESIA", 176);
		map.put("CRITICAL_POISON", 177);
		map.put("SEIZURE_PENALTY", 178);
		map.put("ABNORMAL_ITEM", 179);
		map.put("SEIZURE_A", 180);
		map.put("SEIZURE_B", 181);
		map.put("SEIZURE_C", 182);
		map.put("FORCE_MEDITATION", 183);
		map.put("MIRAGE", 184);
		map.put("POTION_OF_GENESIS", 185);
		map.put("PVP_DMG_UP", 186);
		map.put("PVP_DMG_DOWN", 187);
		map.put("IRON_SHIELD", 188);
		map.put("TRANSFER_DAMAGE", 189);
		map.put("SONG_OF_ELEMENTAL", 190);
		map.put("DANCE_OF_ALIGNMENT", 191);
		map.put("ARCHER_SPECIAL", 192);
		map.put("SPOIL_BOMB", 193);
		map.put("FIRE_DOT", 194);
		map.put("WATER_DOT", 195);
		map.put("WIND_DOT", 196);
		map.put("EARTH_DOT", 197);
		map.put("HEAL_POWER_UP", 198);
		map.put("RECHARGE_UP", 199);
		map.put("NORMAL_ATTACK_BLOCK", 200);
		map.put("DISARM", 201);
		map.put("DEATH_MARK", 202);
		map.put("KAMAEL_SPECIAL", 203);
		map.put("TRANSFORM", 204);
		map.put("DARK_SEED", 205);
		map.put("REAL_TARGET", 206);
		map.put("FREEZING", 207);
		map.put("TIME_CHECK", 208);
		map.put("MA_MD_UP", 209);
		map.put("DEATH_CLACK", 210);
		map.put("HOT_GROUND", 211);
		map.put("EVIL_BLOOD", 212);
		map.put("ALL_REGEN_UP", 213);
		map.put("ALL_REGEN_DOWN", 214);
		map.put("IRON_SHIELD_I", 215);
		map.put("ARCHER_SPECIAL_I", 216);
		map.put("T_CRT_RATE_UP", 217);
		map.put("T_CRT_RATE_DOWN", 218);
		map.put("T_CRT_DMG_UP", 219);
		map.put("T_CRT_DMG_DOWN", 220);
		map.put("INSTINCT", 221);
		map.put("OBLIVION", 222);
		map.put("WEAK_CONSTITUTION", 223);
		map.put("THIN_SKIN", 224);
		map.put("ENERVATION", 225);
		map.put("SPITE", 226);
		map.put("MENTAL_IMPOVERISH", 227);
		map.put("ATTRIBUTE_POTION", 228);
		map.put("TALISMAN", 229);
		map.put("MULTI_DEBUFF_FIRE", 230);
		map.put("MULTI_DEBUFF_WATER", 231);
		map.put("MULTI_DEBUFF_WIND", 232);
		map.put("MULTI_DEBUFF_EARTH", 233);
		map.put("MULTI_DEBUFF_HOLY", 234);
		map.put("MULTI_DEBUFF_UNHOLY", 235);
		map.put("LIFE_FORCE_KAMAEL", 236);
		map.put("MA_UP_SPECIAL", 237);
		map.put("PK_PROTECT", 238);
		map.put("MAXIMUM_ABILITY", 239);
		map.put("TARGET_LOCK", 240);
		map.put("PROTECTION", 241);
		map.put("WILL", 242);
		map.put("SEED_OF_KNIGHT", 243);
		map.put("EXPOSE_WEAK_POINT", 244);
		map.put("FORCE_OF_DESTRUCTION", 245);
		map.put("ELEMENTAL_ARMOR", 246);
		map.put("SUMMON_CONDITION", 247);
		map.put("IMPROVE_PA_PD_UP", 248);
		map.put("IMPROVE_MA_MD_UP", 249);
		map.put("IMPROVE_HP_MP_UP", 250);
		map.put("IMPROVE_CRT_RATE_DMG_UP", 251);
		map.put("IMPROVE_SHIELD_RATE_DEFENCE_UP", 252);
		map.put("IMPROVE_SPEED_AVOID_UP", 253);
		map.put("LIMIT", 254);
		map.put("MULTI_DEBUFF_SOUL", 255);
		map.put("CURSE_LIFE_FLOW", 256);
		map.put("BETRAYAL_MARK", 257);
		map.put("TRANSFORM_HANGOVER", 258);
		map.put("TRANSFORM_SCRIFICE", 259);
		map.put("SONG_OF_WINDSTORM", 260);
		map.put("DANCE_OF_BLADESTORM", 261);
		map.put("IMPROVE_VAMPIRIC_HASTE", 262);
		map.put("WEAPON_MASTERY", 263);
		map.put("APELLA", 264);
		map.put("TRANSFORM_SCRIFICE_P", 265);
		map.put("SUB_TRIGGER_HASTE", 266);
		map.put("SUB_TRIGGER_DEFENCE", 267);
		map.put("SUB_TRIGGER_CRT_RATE_UP", 268);
		map.put("SUB_TRIGGER_SPIRIT", 269);
		map.put("MIRAGE_TRAP", 270);
		map.put("DEATH_PENALTY", 271);
		map.put("ENTRY_FOR_GAME", 272);
		map.put("BLOOD_CONSTRACT", 273);
		map.put("DWARF_BUFF", 274);
		map.put("EVASION_BUFF", 275);
		map.put("SOUL_SHIELD", 276);
		map.put("BR_UTHANKA_BUFF", 277);
		map.put("FIELD_RAID_BUFF1", 278);
		map.put("PD_UP_DMAGIC", 279);
		map.put("PREMIUM_BUFF", 280);
		map.put("RUNWAY_ARMOR", 281);
		map.put("RUNWAY_WEAPON", 282);
		map.put("G_EV_BUFF1", 283);
		map.put("MAX", 284);
		map.put("AIRBIND", 365);
		map.put("KNOCKDOWN", 367);
		map.put("EARTHWORM_DEBUFF", 424);
		map.put("SYNERGY_SIGEL", 433);
		map.put("SYNERGY_TIR", 434);
		map.put("SYNERGY_OTHEL", 435);
		map.put("SYNERGY_YR", 436);
		map.put("SYNERGY_FEOH", 437);
		map.put("SYNERGY_IS", 438);
		map.put("SYNERGY_WYNN", 439);
		map.put("SYNERGY_EOLH", 440);
		map.put("SYNERGY_PARTY_BUF", 465);
		map.put("WIZARD_BUFF1", 511);
		map.put("WIZARD_BUFF2", 512);
		map.put("WIZARD_BUFF3", 513);
		map.put("WIZARD_BUFF4", 514);
		map.put("ARCHER_BUFF1", 515);
		map.put("ARCHER_BUFF2", 516);
		map.put("ARCHER_BUFF3", 517);
		map.put("ARCHER_BUFF4", 518);
		map.put("SUMMON_BUFF1", 519);
		map.put("SUMMON_BUFF2", 520);
		map.put("SUMMON_BUFF3", 521);
		map.put("SUMMON_BUFF4", 522);
		map.put("POTION_OF_PROTECTION", 552);
		map.put("SIGH_OF_STORM", 580);
		map.put("SYNERGY_LENKER", 589);
		map.put("SYNERGY_SEER", 590);
		map.put("INSIDE_POSITION", 593);
		map.put("STEEL_MIND", 596);
		map.put("SIGEL_SHIELD", 597);
		map.put("HELLBOUND_BUFF", 598);
		map.put("MAPHR_AURA", 599);
		map.put("SAYHA_AURA", 600);
		map.put("EVAS_DEBUFF", 601);
		map.put("RIGHT_SIDESTEP", 602);
		map.put("GLOWING_DRAGON_POTION", 604);
		map.put("SUMMONER_BIGBODY1", 629);
	}

	public int getClientId()
	{
		return CLIENT_IDS.getOrDefault(name(), Integer.valueOf(-1)).intValue();
	}
}
