/*
 * Salvation (protocol 140) opcode overrides for server packets.
 *
 * Generated against the L2J Mobius Salvation 5.0 reference build.
 * Applied by ServerPackets.writeId when the receiving client is in
 * salvation mode and the 140 opcode differs from the High Five one.
 */
package org.l2jmobius.gameserver.network;

import java.util.HashMap;
import java.util.Map;

public class ServerPacketsSalvation
{
        private static final Map<ServerPackets, int[]> OVERRIDES = new HashMap<>();

        public static int[] getOverride(ServerPackets packet)
        {
                return OVERRIDES.get(packet);
        }

        static
        {
                OVERRIDES.put(ServerPackets.EX_2ND_PASSWORD_ACK, new int[] { 0xFE, 0x107 });
                OVERRIDES.put(ServerPackets.EX_2ND_PASSWORD_CHECK, new int[] { 0xFE, 0x105 });
                OVERRIDES.put(ServerPackets.EX_2ND_PASSWORD_VERIFY, new int[] { 0xFE, 0x106 });
                OVERRIDES.put(ServerPackets.EX_AGIT_AUCTION_CMD, new int[] { 0xFE, 0xD2 });
                OVERRIDES.put(ServerPackets.EX_ASK_COUPLE_ACTION, new int[] { 0xFE, 0xBC });
                OVERRIDES.put(ServerPackets.EX_ASK_MODIFY_PARTY_LOOTING, new int[] { 0xFE, 0xC0 });
                OVERRIDES.put(ServerPackets.EX_ATTRIBUTE_ENCHANT_RESULT, new int[] { 0xFE, 0x62 });
                OVERRIDES.put(ServerPackets.EX_BASE_ATTRIBUTE_CANCEL_RESULT, new int[] { 0xFE, 0x76 });
                OVERRIDES.put(ServerPackets.EX_BASIC_ACTION_LIST, new int[] { 0xFE, 0x60 });
                OVERRIDES.put(ServerPackets.EX_BLOCK_UP_SET_LIST, new int[] { 0xFE, 0x98 });
                OVERRIDES.put(ServerPackets.EX_BLOCK_UP_SET_STATE, new int[] { 0xFE, 0x99 });
                OVERRIDES.put(ServerPackets.EX_BR_AGATHION_ENERGY_INFO, new int[] { 0xFE, 0xDF });
                OVERRIDES.put(ServerPackets.EX_BR_BROADCAST_EVENT_STATE, new int[] { 0xFE, 0xBD });
                OVERRIDES.put(ServerPackets.EX_BR_BUFF_EVENT_STATE, new int[] { 0xFE, 0xDC });
                OVERRIDES.put(ServerPackets.EX_BR_BUY_PRODUCT, new int[] { 0xFE, 0xD9 });
                OVERRIDES.put(ServerPackets.EX_BR_EXTRA_USER_INFO, new int[] { 0xFE, 0xDB });
                OVERRIDES.put(ServerPackets.EX_BR_GAME_POINT, new int[] { 0xFE, 0xD6 });
                OVERRIDES.put(ServerPackets.EX_BR_LOAD_EVENT_TOP_RANKERS, new int[] { 0xFE, 0xBE });
                OVERRIDES.put(ServerPackets.EX_BR_MINIGAME_LOAD_SCORES, new int[] { 0xFE, 0xDE });
                OVERRIDES.put(ServerPackets.EX_BR_PREMIUM_STATE, new int[] { 0xFE, 0xDA });
                OVERRIDES.put(ServerPackets.EX_BR_PRODUCT_INFO, new int[] { 0xFE, 0xD8 });
                OVERRIDES.put(ServerPackets.EX_BR_PRODUCT_LIST, new int[] { 0xFE, 0xD7 });
                OVERRIDES.put(ServerPackets.EX_BR_RECENT_PRODUCT_LIST, new int[] { 0xFE, 0xDD });
                OVERRIDES.put(ServerPackets.EX_BUY_SELL_LIST, new int[] { 0xFE, 0xB8 });
                OVERRIDES.put(ServerPackets.EX_CHANGE_CLIENT_EFFECT_INFO, new int[] { 0xFE, 0xC3 });
                OVERRIDES.put(ServerPackets.EX_CHANGE_NPC_STATE, new int[] { 0xFE, 0xBF });
                OVERRIDES.put(ServerPackets.EX_CHANGE_POST_STATE, new int[] { 0xFE, 0xB4 });
                OVERRIDES.put(ServerPackets.EX_CHOOSE_INVENTORY_ATTRIBUTE_ITEM, new int[] { 0xFE, 0x63 });
                OVERRIDES.put(ServerPackets.EX_CLEFT_LIST, new int[] { 0xFE, 0x95 });
                OVERRIDES.put(ServerPackets.EX_CLEFT_STATE, new int[] { 0xFE, 0x96 });
                OVERRIDES.put(ServerPackets.EX_CLOSE_RAID_SOCKET, new int[] { 0xFE, 0xB9 });
                OVERRIDES.put(ServerPackets.EX_CONFIRM_ADDING_POST_FRIEND, new int[] { 0xFE, 0xD3 });
                OVERRIDES.put(ServerPackets.EX_CURSED_WEAPON_LIST, new int[] { 0xFE, 0x47 });
                OVERRIDES.put(ServerPackets.EX_CURSED_WEAPON_LOCATION, new int[] { 0xFE, 0x48 });
                OVERRIDES.put(ServerPackets.EX_DISSMISS_MPCC_ROOM, new int[] { 0xFE, 0x9E });
                OVERRIDES.put(ServerPackets.EX_DOMINION_CHANNEL_SET, new int[] { 0xFE, 0x97 });
                OVERRIDES.put(ServerPackets.EX_DOMINION_WAR_END, new int[] { 0xFE, 0xA5 });
                OVERRIDES.put(ServerPackets.EX_DOMINION_WAR_START, new int[] { 0xFE, 0xA4 });
                OVERRIDES.put(ServerPackets.EX_DUEL_ASK_START, new int[] { 0xFE, 0x4D });
                OVERRIDES.put(ServerPackets.EX_DUEL_END, new int[] { 0xFE, 0x50 });
                OVERRIDES.put(ServerPackets.EX_DUEL_ENEMY_RELATION, new int[] { 0xFE, 0x5A });
                OVERRIDES.put(ServerPackets.EX_DUEL_READY, new int[] { 0xFE, 0x4E });
                OVERRIDES.put(ServerPackets.EX_DUEL_START, new int[] { 0xFE, 0x4F });
                OVERRIDES.put(ServerPackets.EX_DUEL_UPDATE_USER_INFO, new int[] { 0xFE, 0x51 });
                OVERRIDES.put(ServerPackets.EX_ENCHANT_SKILL_INFO_DETAIL, new int[] { 0xFE, 0x5F });
                OVERRIDES.put(ServerPackets.EX_ENCHANT_SKILL_RESULT, new int[] { 0xFE, 0xA8 });
                OVERRIDES.put(ServerPackets.EX_GET_PREMIUM_ITEM_LIST, new int[] { 0xFE, 0x87 });
                OVERRIDES.put(ServerPackets.EX_GM_VIEW_QUEST_ITEM_LIST, new int[] { 0xFE, 0xC8 });
                OVERRIDES.put(ServerPackets.EX_GOODS_INVENTORY_INFO, new int[] { 0xFE, 0x10D });
                OVERRIDES.put(ServerPackets.EX_GOODS_INVENTORY_RESULT, new int[] { 0xFE, 0x10E });
                OVERRIDES.put(ServerPackets.EX_HERO_LIST, new int[] { 0xFE, 0x7A });
                OVERRIDES.put(ServerPackets.EX_INITIALIZE_SEED, new int[] { 0xFE, 0xB6 });
                OVERRIDES.put(ServerPackets.EX_ITEM_AUCTION_INFO, new int[] { 0xFE, 0x69 });
                OVERRIDES.put(ServerPackets.EX_JUMP_TO_LOCATION, new int[] { 0xFE, 0x89 });
                OVERRIDES.put(ServerPackets.EX_LIST_MPCC_WAITING, new int[] { 0xFE, 0x9D });
                OVERRIDES.put(ServerPackets.EX_MANAGE_MPCC_ROOM_MEMBER, new int[] { 0xFE, 0x9F });
                OVERRIDES.put(ServerPackets.EX_MEMBERSHIP_INFO, new int[] { 0xFE, 0xC4 });
                OVERRIDES.put(ServerPackets.EX_MOVE_TO_LOCATION_IN_SUTTLE, new int[] { 0xFE, 0xCF });
                OVERRIDES.put(ServerPackets.EX_MPCC_PARTYMASTER_LIST, new int[] { 0xFE, 0xA3 });
                OVERRIDES.put(ServerPackets.EX_MPCC_ROOM_INFO, new int[] { 0xFE, 0x9C });
                OVERRIDES.put(ServerPackets.EX_MPCC_ROOM_MEMBER, new int[] { 0xFE, 0xA0 });
                OVERRIDES.put(ServerPackets.EX_NAVIT_ADVENT_EFFECT, new int[] { 0xFE, 0xE4 });
                OVERRIDES.put(ServerPackets.EX_NAVIT_ADVENT_POINT_INFO, new int[] { 0xFE, 0xE3 });
                OVERRIDES.put(ServerPackets.EX_NAVIT_ADVENT_TIME_CHANGE, new int[] { 0xFE, 0xE5 });
                OVERRIDES.put(ServerPackets.EX_NEED_TO_CHANGE_NAME, new int[] { 0xFE, 0x6A });
                OVERRIDES.put(ServerPackets.EX_NOTICE_POST_ARRIVED, new int[] { 0xFE, 0xAA });
                OVERRIDES.put(ServerPackets.EX_NOTIFY_PREMIUM_ITEM, new int[] { 0xFE, 0x86 });
                OVERRIDES.put(ServerPackets.EX_NPC_QUEST_HTML_MESSAGE, new int[] { 0xFE, 0x8E });
                // Salvation 140 client expects UI event timers at 0x8F (HF uses 0x8E); without this the
                // client misreads ExSendUIEvent as ExNpcQuestHtmlMessage (instance countdowns never show).
                OVERRIDES.put(ServerPackets.EX_SEND_UI_EVENT, new int[] { 0xFE, 0x8F });
                OVERRIDES.put(ServerPackets.EX_OLYMPIAD_MODE, new int[] { 0xFE, 0x7D });
                OVERRIDES.put(ServerPackets.EX_OLYMPIAD_SPELLED_INFO, new int[] { 0xFE, 0x7C });
                OVERRIDES.put(ServerPackets.EX_OLYMPIAD_USER_INFO, new int[] { 0xFE, 0x7B });
                OVERRIDES.put(ServerPackets.EX_PARTY_MEMBER_RENAMED, new int[] { 0xFE, 0xA7 });
                OVERRIDES.put(ServerPackets.EX_PARTY_PET_WINDOW_DELETE, new int[] { 0xFE, 0x6B });
                OVERRIDES.put(ServerPackets.EX_PERIODIC_ITEM_LIST, new int[] { 0xFE, 0x88 });
                OVERRIDES.put(ServerPackets.EX_PLAY_ANIMATION, new int[] { 0xFE, 0x5B });
                OVERRIDES.put(ServerPackets.EX_PLAY_SCENE, new int[] { 0xFE, 0x5D });
                OVERRIDES.put(ServerPackets.EX_PRIVATE_MARKET_LIST, new int[] { 0xFE, 0xBA });
                OVERRIDES.put(ServerPackets.EX_PUT_COMMISSION_RESULT_FOR_VARIATION_MAKE, new int[] { 0xFE, 0x56 });
                OVERRIDES.put(ServerPackets.EX_PUT_ENCHANT_SUPPORT_ITEM_RESULT, new int[] { 0xFE, 0x83 });
                OVERRIDES.put(ServerPackets.EX_PUT_ENCHANT_TARGET_ITEM_RESULT, new int[] { 0xFE, 0x82 });
                OVERRIDES.put(ServerPackets.EX_PUT_INTENSIVE_RESULT_FOR_VARIATION_MAKE, new int[] { 0xFE, 0x55 });
                OVERRIDES.put(ServerPackets.EX_PUT_ITEM_RESULT_FOR_VARIATION_CANCEL, new int[] { 0xFE, 0x58 });
                OVERRIDES.put(ServerPackets.EX_PUT_ITEM_RESULT_FOR_VARIATION_MAKE, new int[] { 0xFE, 0x54 });
                OVERRIDES.put(ServerPackets.EX_PVP_MATCH_RECORD, new int[] { 0xFE, 0x7F });
                OVERRIDES.put(ServerPackets.EX_PVP_MATCH_USER_DIE, new int[] { 0xFE, 0x80 });
                OVERRIDES.put(ServerPackets.EX_QUEST_ITEM_LIST, new int[] { 0xFE, 0xC7 });
                OVERRIDES.put(ServerPackets.EX_QUEST_NPC_LOG_LIST, new int[] { 0xFE, 0xC6 });
                OVERRIDES.put(ServerPackets.EX_RAID_CHARACTER_SELECTED, new int[] { 0xFE, 0xBB });
                OVERRIDES.put(ServerPackets.EX_RAID_RESERVE_RESULT, new int[] { 0xFE, 0xB7 });
                OVERRIDES.put(ServerPackets.EX_RECEIVE_OLYMPIAD, new int[] { 0xFE, 0xD5 });
                OVERRIDES.put(ServerPackets.EX_RECEIVE_SHOW_POST_FRIEND, new int[] { 0xFE, 0xD4 });
                OVERRIDES.put(ServerPackets.EX_RED_SKY, new int[] { 0xFE, 0x42 });
                OVERRIDES.put(ServerPackets.EX_REFUND_LIST, new int[] { 0xFE, 0xA9 });
                OVERRIDES.put(ServerPackets.EX_REPLY_DOMINION_INFO, new int[] { 0xFE, 0x93 });
                OVERRIDES.put(ServerPackets.EX_REPLY_HAND_OVER_PARTY_MASTER, new int[] { 0xFE, 0xC5 });
                OVERRIDES.put(ServerPackets.EX_REPLY_POST_ITEM_LIST, new int[] { 0xFE, 0xB3 });
                OVERRIDES.put(ServerPackets.EX_REPLY_RECEIVED_POST, new int[] { 0xFE, 0xAC });
                OVERRIDES.put(ServerPackets.EX_REPLY_REGISTER_DOMINION, new int[] { 0xFE, 0x92 });
                OVERRIDES.put(ServerPackets.EX_REPLY_SENT_POST, new int[] { 0xFE, 0xAE });
                OVERRIDES.put(ServerPackets.EX_REQUEST_HACK_SHIELD, new int[] { 0xFE, 0x4A });
                OVERRIDES.put(ServerPackets.EX_RESPONSE_FREE_SERVER, new int[] { 0xFE, 0x78 });
                OVERRIDES.put(ServerPackets.EX_RESPONSE_SHOW_CONTENTS, new int[] { 0xFE, 0xB1 });
                OVERRIDES.put(ServerPackets.EX_RESPONSE_SHOW_STEP_ONE, new int[] { 0xFE, 0xAF });
                OVERRIDES.put(ServerPackets.EX_RESPONSE_SHOW_STEP_TWO, new int[] { 0xFE, 0xB0 });
                OVERRIDES.put(ServerPackets.EX_RESTART_CLIENT, new int[] { 0xFE, 0x49 });
                OVERRIDES.put(ServerPackets.EX_RESTART_RESPONSE, new int[] { 0xFE, 0xC9 });
                OVERRIDES.put(ServerPackets.EX_ROTATION, new int[] { 0xFE, 0xC2 });
                OVERRIDES.put(ServerPackets.EX_RP_ITEM_LINK, new int[] { 0xFE, 0x6D });
                OVERRIDES.put(ServerPackets.EX_SEARCH_ORC, new int[] { 0xFE, 0x46 });
                OVERRIDES.put(ServerPackets.EX_SET_PARTY_LOOTING, new int[] { 0xFE, 0xC1 });
                OVERRIDES.put(ServerPackets.EX_SHOW_BASE_ATTRIBUTE_CANCEL_WINDOW, new int[] { 0xFE, 0x75 });
                OVERRIDES.put(ServerPackets.EX_SHOW_DOMINION_REGISTRY, new int[] { 0xFE, 0x91 });
                OVERRIDES.put(ServerPackets.EX_SHOW_FORTRESS_MAP_INFO, new int[] { 0xFE, 0x7E });
                OVERRIDES.put(ServerPackets.EX_SHOW_LINES, new int[] { 0xFE, 0xA6 });
                OVERRIDES.put(ServerPackets.EX_SHOW_OWNTHING_POS, new int[] { 0xFE, 0x94 });
                OVERRIDES.put(ServerPackets.EX_SHOW_PETITION_HTML, new int[] { 0xFE, 0xB2 });
                OVERRIDES.put(ServerPackets.EX_SHOW_PROCURE_CROP_DETAIL, new int[] { 0xFE, 0x79 });
                OVERRIDES.put(ServerPackets.EX_SHOW_RECEIVED_POST_LIST, new int[] { 0xFE, 0xAB });
                OVERRIDES.put(ServerPackets.EX_SHOW_SEED_MAP_INFO, new int[] { 0xFE, 0xA2 });
                OVERRIDES.put(ServerPackets.EX_SHOW_SENT_POST_LIST, new int[] { 0xFE, 0xAD });
                OVERRIDES.put(ServerPackets.EX_SHOW_TERRITORY, new int[] { 0xFE, 0x8D });
                OVERRIDES.put(ServerPackets.EX_SHOW_TRACE, new int[] { 0xFE, 0x68 });
                OVERRIDES.put(ServerPackets.EX_SHOW_VARIATION_CANCEL_WINDOW, new int[] { 0xFE, 0x53 });
                OVERRIDES.put(ServerPackets.EX_SHOW_VARIATION_MAKE_WINDOW, new int[] { 0xFE, 0x52 });
                OVERRIDES.put(ServerPackets.EX_SHUTTLE_INFO, new int[] { 0xFE, 0xCB });
                OVERRIDES.put(ServerPackets.EX_SPAWN_EMITTER, new int[] { 0xFE, 0x5E });
                OVERRIDES.put(ServerPackets.EX_START_SCENE_PLAYER, new int[] { 0xFE, 0x9A });
                OVERRIDES.put(ServerPackets.EX_STOP_MOVE_IN_SHUTTLE, new int[] { 0xFE, 0xD0 });
                OVERRIDES.put(ServerPackets.EX_SUTTLE_GET_OFF, new int[] { 0xFE, 0xCD });
                OVERRIDES.put(ServerPackets.EX_SUTTLE_GET_ON, new int[] { 0xFE, 0xCC });
                OVERRIDES.put(ServerPackets.EX_SUTTLE_MOVE, new int[] { 0xFE, 0xCE });
                OVERRIDES.put(ServerPackets.EX_TUTORIAL_LIST, new int[] { 0xFE, 0x6C });
                OVERRIDES.put(ServerPackets.EX_UI_SETTING, new int[] { 0xFE, 0x71 });
                OVERRIDES.put(ServerPackets.EX_USE_SHARED_GROUP_ITEM, new int[] { 0xFE, 0x4B });
                OVERRIDES.put(ServerPackets.EX_VALIDATE_LOCATION_IN_SHUTTLE, new int[] { 0xFE, 0xD1 });
                OVERRIDES.put(ServerPackets.EX_VARIATION_CANCEL_RESULT, new int[] { 0xFE, 0x59 });
                OVERRIDES.put(ServerPackets.EX_VARIATION_RESULT, new int[] { 0xFE, 0x57 });
                OVERRIDES.put(ServerPackets.EX_VITALITY_POINT_INFO, new int[] { 0xFE, 0xA1 });
                OVERRIDES.put(ServerPackets.EX_VOTE_SYSTEM_INFO, new int[] { 0xFE, 0xCA });
                OVERRIDES.put(ServerPackets.FLY_SELF_DESTINATION, new int[] { 0xFE, 0x44 });
                OVERRIDES.put(ServerPackets.PLEDGE_POWER_GRADE_LIST, new int[] { 0xFE, 0x3D });
                OVERRIDES.put(ServerPackets.PLEDGE_RECEIVE_MEMBER_INFO, new int[] { 0xFE, 0x3F });
                OVERRIDES.put(ServerPackets.PLEDGE_RECEIVE_POWER_INFO, new int[] { 0xFE, 0x3E });
                OVERRIDES.put(ServerPackets.PLEDGE_RECEIVE_SUB_PLEDGE_CREATED, new int[] { 0xFE, 0x41 });
                OVERRIDES.put(ServerPackets.PLEDGE_RECEIVE_UPDATE_POWER, new int[] { 0xFE, 0x43 });
                OVERRIDES.put(ServerPackets.PLEDGE_RECEIVE_WAR_LIST, new int[] { 0xFE, 0x40 });
        }
}
