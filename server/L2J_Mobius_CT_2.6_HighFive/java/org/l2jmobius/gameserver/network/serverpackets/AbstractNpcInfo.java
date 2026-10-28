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

import java.util.List;

import org.l2jmobius.commons.network.buffer.WriteBuffer;
import org.l2jmobius.gameserver.config.NpcConfig;
import org.l2jmobius.gameserver.config.custom.ChampionMonstersConfig;
import org.l2jmobius.gameserver.config.custom.MultilingualSupportConfig;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.xml.NpcNameLocalisationData;
import org.l2jmobius.gameserver.entity.actor.Creature;
import org.l2jmobius.gameserver.entity.actor.Npc;
import org.l2jmobius.gameserver.entity.actor.Player;
import org.l2jmobius.gameserver.entity.actor.Summon;
import org.l2jmobius.gameserver.entity.actor.enums.creature.Team;
import org.l2jmobius.gameserver.entity.actor.instance.Monster;
import org.l2jmobius.gameserver.entity.actor.instance.Trap;
import org.l2jmobius.gameserver.entity.clan.Clan;
import org.l2jmobius.gameserver.entity.zone.ZoneId;
import org.l2jmobius.gameserver.entity.zone.type.TownZone;
import org.l2jmobius.gameserver.managers.TownManager;
import org.l2jmobius.gameserver.mechanics.skill.AbnormalVisualEffect;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.ServerPackets;
import org.l2jmobius.gameserver.network.enums.NpcInfoType;

public abstract class AbstractNpcInfo extends ServerPacket
{
	protected int _x;
	protected int _y;
	protected int _z;
	protected int _heading;
	protected int _displayId;
	protected boolean _isAttackable;
	protected boolean _isSummoned;
	protected int _mAtkSpd;
	protected int _pAtkSpd;
	protected final int _runSpd;
	protected final int _walkSpd;
	protected final int _swimRunSpd;
	protected final int _swimWalkSpd;
	protected final int _flyRunSpd;
	protected final int _flyWalkSpd;
	protected double _moveMultiplier;
	protected int _rhand;
	protected int _lhand;
	protected int _chest;
	protected int _enchantEffect;
	protected float _collisionHeight;
	protected float _collisionRadius;
	protected String _name = "";
	protected String _title = "";
	protected final boolean _gmSeeInvis;
	
	private static void addNpcInfoMask(byte[] masks, NpcInfoType type)
	{
		final int mask = type.getMask();
		masks[mask >> 3] |= AbstractMaskPacket.DEFAULT_FLAG_ARRAY[mask & 7];
	}
	
	public AbstractNpcInfo(Creature creature, boolean gmSeeInvis)
	{
		_isSummoned = creature.isShowSummonAnimation();
		_x = creature.getX();
		_y = creature.getY();
		_z = creature.getZ();
		_heading = creature.getHeading();
		_mAtkSpd = creature.getMAtkSpd();
		_pAtkSpd = (int) creature.getPAtkSpd();
		_moveMultiplier = creature.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(creature.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(creature.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(creature.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(creature.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = creature.isFlying() ? _runSpd : 0;
		_flyWalkSpd = creature.isFlying() ? _walkSpd : 0;
		_gmSeeInvis = gmSeeInvis;
	}
	
	/**
	 * Packet for Npcs
	 */
	public static class NpcInfo extends AbstractNpcInfo
	{
		private final Npc _npc;
		private int _clanCrest = 0;
		private int _allyCrest = 0;
		private int _allyId = 0;
		private int _clanId = 0;
		private int _displayEffect = 0;
		
		public NpcInfo(Npc cha, Creature attacker)
		{
			super(cha, (attacker != null) && attacker.isGM());
			_npc = cha;
			_displayId = cha.getTemplate().getDisplayId(); // On every subclass
			_rhand = cha.getRightHandItem(); // On every subclass
			_lhand = cha.getLeftHandItem(); // On every subclass
			_enchantEffect = cha.getEnchantEffect();
			_collisionHeight = cha.getTemplate().getFCollisionHeight(); // On every subclass
			_collisionRadius = cha.getTemplate().getFCollisionRadius(); // On every subclass
			_isAttackable = cha.isAutoAttackable(attacker);
			
			// npc crest of owning clan/ally of castle
			if (cha.isNpc() && cha.isTargetable() && cha.isShowName() && !cha.isMonster() && cha.isInsideZone(ZoneId.TOWN) && (NpcConfig.SHOW_CREST_WITHOUT_QUEST || cha.getCastle().getShowNpcCrest()) && (cha.getCastle().getOwnerId() != 0))
			{
				final TownZone town = TownManager.getTown(_x, _y, _z);
				if (town != null)
				{
					final int townId = town.getTownId();
					if ((townId != 33) && (townId != 22))
					{
						final Clan clan = ClanTable.getInstance().getClan(cha.getCastle().getOwnerId());
						_clanCrest = clan.getCrestId();
						_clanId = clan.getId();
						_allyCrest = clan.getAllyCrestId();
						_allyId = clan.getAllyId();
					}
				}
			}
			
			_displayEffect = cha.getDisplayEffect();
		}
		
		@Override
		public void writeImpl(GameClient client, WriteBuffer buffer)
		{
			if (_npc.isDecayed())
			{
				return;
			}
			
			// Localisation related.
			String[] localisation = null;
			if (MultilingualSupportConfig.MULTILANG_ENABLE)
			{
				final Player player = client.getPlayer();
				if (player != null)
				{
					final String lang = player.getLang();
					if ((lang != null) && !lang.equals("en"))
					{
						localisation = NpcNameLocalisationData.getInstance().getLocalisation(lang, _npc.getId());
						if (localisation != null)
						{
							_name = localisation[0];
							_title = localisation[1];
						}
					}
				}
			}
			
			if ((client != null) && client.isSalvationMode())
			{
				// Salvation (protocol 140) mask-based NpcInfo layout (ported from the reference).
				final boolean hasName = true; // Salvation: always send the server-side name.
				final String name = (localisation != null) ? localisation[0] : _npc.getName();
				
				String title;
				boolean hasTitle;
				if (_npc.isInvisible())
				{
					title = "Invisible";
					hasTitle = true;
				}
				else if (localisation != null)
				{
					title = localisation[1];
					hasTitle = true;
				}
				else
				{
					title = _npc.getTemplate().isUsingServerSideTitle() ? _npc.getTemplate().getTitle() : _npc.getTitle();
					hasTitle = _npc.getTemplate().isUsingServerSideTitle();
				}
				
				if (_npc.isMonster() && (NpcConfig.SHOW_NPC_LEVEL || NpcConfig.SHOW_NPC_AGGRESSION))
				{
					String t1 = "";
					if (NpcConfig.SHOW_NPC_LEVEL)
					{
						t1 += "Lv " + _npc.getLevel();
					}
					
					String t2 = "";
					if (NpcConfig.SHOW_NPC_AGGRESSION)
					{
						if (!t1.isEmpty())
						{
							t2 += " ";
						}
						
						final Monster monster = _npc.asMonster();
						if (monster.isAggressive())
						{
							t2 += "[A]"; // Aggressive.
						}
						
						if ((monster.getTemplate().getClans() != null) && (monster.getTemplate().getClanHelpRange() > 0))
						{
							t2 += "[G]"; // Group.
						}
					}
					
					t1 += t2;
					if (!title.isEmpty())
					{
						t1 += " " + title;
					}
					
					title = _npc.isChampion() ? ChampionMonstersConfig.CHAMP_TITLE + " " + t1 : t1;
					hasTitle = true;
				}
				else if (_npc.isChampion())
				{
					title = ChampionMonstersConfig.CHAMPION_ENABLE ? ChampionMonstersConfig.CHAMP_TITLE : title;
					hasTitle = true;
				}
				
				final byte[] masks = new byte[5];
				int initSize = 0;
				int blockSize = 0;
				int statusMask = 0x00;
				
				// Block 1 components.
				addNpcInfoMask(masks, NpcInfoType.ATTACKABLE);
				initSize += NpcInfoType.ATTACKABLE.getBlockLength();
				addNpcInfoMask(masks, NpcInfoType.RELATIONS);
				initSize += NpcInfoType.RELATIONS.getBlockLength();
				if (hasTitle)
				{
					addNpcInfoMask(masks, NpcInfoType.TITLE);
					initSize += NpcInfoType.TITLE.getBlockLength() + (title.length() * 2);
				}
				
				// Block 2 components.
				addNpcInfoMask(masks, NpcInfoType.ID);
				blockSize += NpcInfoType.ID.getBlockLength();
				addNpcInfoMask(masks, NpcInfoType.POSITION);
				blockSize += NpcInfoType.POSITION.getBlockLength();
				if (_heading > 0)
				{
					addNpcInfoMask(masks, NpcInfoType.HEADING);
					blockSize += NpcInfoType.HEADING.getBlockLength();
				}
				
				if ((_pAtkSpd > 0) || (_mAtkSpd > 0))
				{
					addNpcInfoMask(masks, NpcInfoType.ATK_CAST_SPEED);
					blockSize += NpcInfoType.ATK_CAST_SPEED.getBlockLength();
				}
				
				if (_npc.getRunSpeed() > 0)
				{
					addNpcInfoMask(masks, NpcInfoType.SPEED_MULTIPLIER);
					blockSize += NpcInfoType.SPEED_MULTIPLIER.getBlockLength();
				}
				
				if ((_rhand > 0) || (_lhand > 0))
				{
					addNpcInfoMask(masks, NpcInfoType.EQUIPPED);
					blockSize += NpcInfoType.EQUIPPED.getBlockLength();
				}
				
				addNpcInfoMask(masks, NpcInfoType.STOP_MODE);
				blockSize += NpcInfoType.STOP_MODE.getBlockLength();
				addNpcInfoMask(masks, NpcInfoType.MOVE_MODE);
				blockSize += NpcInfoType.MOVE_MODE.getBlockLength();
				if (_npc.isInsideZone(ZoneId.WATER) || _npc.isFlying())
				{
					addNpcInfoMask(masks, NpcInfoType.SWIM_OR_FLY);
					blockSize += NpcInfoType.SWIM_OR_FLY.getBlockLength();
				}
				
				if (_npc.getTeam() != Team.NONE)
				{
					addNpcInfoMask(masks, NpcInfoType.TEAM);
					blockSize += NpcInfoType.TEAM.getBlockLength();
				}
				
				if (_enchantEffect > 0)
				{
					addNpcInfoMask(masks, NpcInfoType.ENCHANT);
					blockSize += NpcInfoType.ENCHANT.getBlockLength();
				}
				
				if (_npc.isFlying())
				{
					addNpcInfoMask(masks, NpcInfoType.FLYING);
					blockSize += NpcInfoType.FLYING.getBlockLength();
				}
				
				addNpcInfoMask(masks, NpcInfoType.PET_EVOLUTION_ID);
				blockSize += NpcInfoType.PET_EVOLUTION_ID.getBlockLength();
				if (_displayEffect > 0)
				{
					addNpcInfoMask(masks, NpcInfoType.DISPLAY_EFFECT);
					blockSize += NpcInfoType.DISPLAY_EFFECT.getBlockLength();
				}
				
				if (_npc.getCurrentHp() <= _npc.getMaxHp())
				{
					addNpcInfoMask(masks, NpcInfoType.CURRENT_HP);
					blockSize += NpcInfoType.CURRENT_HP.getBlockLength();
				}
				
				if (_npc.getCurrentMp() <= _npc.getMaxMp())
				{
					addNpcInfoMask(masks, NpcInfoType.CURRENT_MP);
					blockSize += NpcInfoType.CURRENT_MP.getBlockLength();
				}
				
				if (_npc.getMaxHp() > 0)
				{
					addNpcInfoMask(masks, NpcInfoType.MAX_HP);
					blockSize += NpcInfoType.MAX_HP.getBlockLength();
				}
				
				if (_npc.getMaxMp() > 0)
				{
					addNpcInfoMask(masks, NpcInfoType.MAX_MP);
					blockSize += NpcInfoType.MAX_MP.getBlockLength();
				}
				
				if (_isSummoned)
				{
					addNpcInfoMask(masks, NpcInfoType.SUMMONED);
					blockSize += NpcInfoType.SUMMONED.getBlockLength();
				}
				
				if (hasName)
				{
					addNpcInfoMask(masks, NpcInfoType.NAME);
					blockSize += NpcInfoType.NAME.getBlockLength() + (name.length() * 2);
				}
				
				if (_clanId > 0)
				{
					addNpcInfoMask(masks, NpcInfoType.CLAN);
					blockSize += NpcInfoType.CLAN.getBlockLength();
				}
				
				if (_npc.isInCombat())
				{
					statusMask |= 0x01;
				}
				if (_npc.isDead())
				{
					statusMask |= 0x02;
				}
				if (_npc.isTargetable())
				{
					statusMask |= 0x04;
				}
				if (_npc.isShowName())
				{
					statusMask |= 0x08;
				}
				if (statusMask != 0x00)
				{
					addNpcInfoMask(masks, NpcInfoType.VISUAL_STATE);
					blockSize += NpcInfoType.VISUAL_STATE.getBlockLength();
				}
				
				// Salvation (protocol 140): abnormal visual effects as short client ids (see SummonInfo / reference NpcInfo).
				final List<Short> abnormalClientIds = SalvationAveMap.toClientIds(_npc.getAbnormalVisualEffects() | _npc.getAbnormalVisualEffectSpecial(), _npc.isInvisible());
				if (!abnormalClientIds.isEmpty())
				{
					addNpcInfoMask(masks, NpcInfoType.ABNORMALS);
					blockSize += NpcInfoType.ABNORMALS.getBlockLength() + (abnormalClientIds.size() * 2);
				}
				
				ServerPackets.NPC_INFO.writeId(this, buffer);
				buffer.writeInt(_npc.getObjectId());
				buffer.writeByte(_isSummoned ? 2 : 0); // 0=teleported 1=default 2=summoned
				buffer.writeShort(37); // mask_bits_37
				buffer.writeBytes(masks);
				
				// Block 1
				buffer.writeByte(initSize);
				buffer.writeByte(_isAttackable);
				buffer.writeInt(0); // relations
				if (hasTitle)
				{
					buffer.writeString(title);
				}
				
				// Block 2
				buffer.writeShort(blockSize);
				buffer.writeInt(_displayId + 1000000); // npctype id
				buffer.writeInt(_x);
				buffer.writeInt(_y);
				buffer.writeInt(_z);
				if (_heading > 0)
				{
					buffer.writeInt(_heading);
				}
				
				if ((_pAtkSpd > 0) || (_mAtkSpd > 0))
				{
					buffer.writeInt(_pAtkSpd);
					buffer.writeInt(_mAtkSpd);
				}
				
				if (_npc.getRunSpeed() > 0)
				{
					buffer.writeFloat((float) _moveMultiplier);
					buffer.writeFloat((float) _npc.getAttackSpeedMultiplier());
				}
				
				if ((_rhand > 0) || (_lhand > 0))
				{
					buffer.writeInt(_rhand);
					buffer.writeInt(0); // armor id
					buffer.writeInt(_lhand);
				}
				
				buffer.writeByte(!_npc.isDead()); // stop mode
				buffer.writeByte(_npc.isRunning()); // move mode
				if (_npc.isInsideZone(ZoneId.WATER) || _npc.isFlying())
				{
					buffer.writeByte(_npc.isInsideZone(ZoneId.WATER) ? 1 : _npc.isFlying() ? 2 : 0);
				}
				
				if (_npc.getTeam() != Team.NONE)
				{
					buffer.writeByte(_npc.getTeam().getId());
				}
				
				if (_enchantEffect > 0)
				{
					buffer.writeInt(_enchantEffect);
				}
				
				if (_npc.isFlying())
				{
					buffer.writeInt(_npc.isFlying());
				}
				
				buffer.writeInt(0); // pet evolution id
				if (_displayEffect > 0)
				{
					buffer.writeInt(_displayEffect);
				}
				
				if (_npc.getCurrentHp() <= _npc.getMaxHp())
				{
					buffer.writeInt((int) _npc.getCurrentHp());
				}
				
				if (_npc.getCurrentMp() <= _npc.getMaxMp())
				{
					buffer.writeInt((int) _npc.getCurrentMp());
				}
				
				if (_npc.getMaxHp() > 0)
				{
					buffer.writeInt(_npc.getMaxHp());
				}
				
				if (_npc.getMaxMp() > 0)
				{
					buffer.writeInt(_npc.getMaxMp());
				}
				
				if (_isSummoned)
				{
					buffer.writeByte(0); // 2 - do some animation on spawn
				}
				
				if (hasName)
				{
					buffer.writeString(name);
				}
				
				if (_clanId > 0)
				{
					buffer.writeInt(_clanId);
					buffer.writeInt(_clanCrest);
					buffer.writeInt(0); // large crest
					buffer.writeInt(_allyId);
					buffer.writeInt(_allyCrest);
				}
				
				if (statusMask != 0x00)
				{
					buffer.writeByte(statusMask);
				}
				
				if (!abnormalClientIds.isEmpty())
				{
					buffer.writeShort(abnormalClientIds.size());
					for (short aveClientId : abnormalClientIds)
					{
						buffer.writeShort(aveClientId);
					}
				}
				return;
			}
			
			ServerPackets.NPC_INFO.writeId(this, buffer);
			buffer.writeInt(_npc.getObjectId());
			buffer.writeInt(_displayId + 1000000); // npctype id
			buffer.writeInt(_isAttackable);
			buffer.writeInt(_x);
			buffer.writeInt(_y);
			buffer.writeInt(_z);
			buffer.writeInt(_heading);
			buffer.writeInt(0);
			buffer.writeInt(_mAtkSpd);
			buffer.writeInt(_pAtkSpd);
			buffer.writeInt(_runSpd);
			buffer.writeInt(_walkSpd);
			buffer.writeInt(_swimRunSpd);
			buffer.writeInt(_swimWalkSpd);
			buffer.writeInt(_flyRunSpd);
			buffer.writeInt(_flyWalkSpd);
			buffer.writeInt(_flyRunSpd);
			buffer.writeInt(_flyWalkSpd);
			buffer.writeDouble(_moveMultiplier);
			buffer.writeDouble(_npc.getAttackSpeedMultiplier());
			buffer.writeDouble(_collisionRadius);
			buffer.writeDouble(_collisionHeight);
			buffer.writeInt(_rhand); // right hand weapon
			buffer.writeInt(_chest);
			buffer.writeInt(_lhand); // left hand weapon
			buffer.writeByte(1); // name above char 1=true ... ??
			buffer.writeByte(_npc.isRunning());
			buffer.writeByte(_npc.isInCombat());
			buffer.writeByte(_npc.isAlikeDead());
			buffer.writeByte(_isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
			buffer.writeInt(-1); // High Five NPCString ID
			if ((localisation == null) && _npc.getTemplate().isUsingServerSideName())
			{
				_name = _npc.getName(); // On every subclass
			}
			
			buffer.writeString(_name);
			buffer.writeInt(-1); // High Five NPCString ID
			if (_npc.isInvisible())
			{
				_title = "Invisible";
			}
			else if (localisation == null)
			{
				if (_npc.getTemplate().isUsingServerSideTitle())
				{
					_title = _npc.getTemplate().getTitle(); // On every subclass
				}
				else
				{
					_title = _npc.getTitle(); // On every subclass
				}
			}
			
			// Custom level titles
			if (_npc.isMonster() && (NpcConfig.SHOW_NPC_LEVEL || NpcConfig.SHOW_NPC_AGGRESSION))
			{
				String t1 = "";
				if (NpcConfig.SHOW_NPC_LEVEL)
				{
					t1 += "Lv " + _npc.getLevel();
				}
				
				String t2 = "";
				if (NpcConfig.SHOW_NPC_AGGRESSION)
				{
					if (!t1.isEmpty())
					{
						t2 += " ";
					}
					
					final Monster monster = _npc.asMonster();
					if (monster.isAggressive())
					{
						t2 += "[A]"; // Aggressive.
					}
					
					if ((monster.getTemplate().getClans() != null) && (monster.getTemplate().getClanHelpRange() > 0))
					{
						t2 += "[G]"; // Group.
					}
				}
				
				t1 += t2;
				if ((_title != null) && !_title.isEmpty())
				{
					t1 += " " + _title;
				}
				
				_title = _npc.isChampion() ? ChampionMonstersConfig.CHAMP_TITLE + " " + t1 : t1;
			}
			else if (ChampionMonstersConfig.CHAMPION_ENABLE && _npc.isChampion())
			{
				_title = (ChampionMonstersConfig.CHAMP_TITLE); // On every subclass
			}
			
			buffer.writeString(_title);
			buffer.writeInt(0); // Title color 0=client default
			buffer.writeInt(0); // pvp flag
			buffer.writeInt(0); // karma
			buffer.writeInt(_npc.isInvisible() ? _npc.getAbnormalVisualEffects() | AbnormalVisualEffect.STEALTH.getMask() : _npc.getAbnormalVisualEffects());
			buffer.writeInt(_clanId); // clan id
			buffer.writeInt(_clanCrest); // crest id
			buffer.writeInt(_allyId); // ally id
			buffer.writeInt(_allyCrest); // all crest
			buffer.writeByte(_npc.isInsideZone(ZoneId.WATER) ? 1 : _npc.isFlying() ? 2 : 0); // C2
			buffer.writeByte(_npc.getTeam().getId());
			buffer.writeDouble(_collisionRadius);
			buffer.writeDouble(_collisionHeight);
			buffer.writeInt(_enchantEffect); // C4
			buffer.writeInt(_npc.isFlying()); // C6
			buffer.writeInt(0);
			buffer.writeInt(_npc.getColorEffect()); // CT1.5 Pet form and skills, Color effect
			buffer.writeByte(_npc.isTargetable());
			buffer.writeByte(_npc.isShowName());
			buffer.writeInt(_npc.getAbnormalVisualEffectSpecial());
			buffer.writeInt(_displayEffect);
		}
	}
	
	public static class TrapInfo extends AbstractNpcInfo
	{
		private final Trap _trap;
		
		public TrapInfo(Trap cha, Creature attacker)
		{
			super(cha, (attacker != null) && attacker.isGM());
			_trap = cha;
			_displayId = cha.getTemplate().getDisplayId();
			_isAttackable = cha.isAutoAttackable(attacker);
			_rhand = 0;
			_lhand = 0;
			_collisionHeight = _trap.getTemplate().getFCollisionHeight();
			_collisionRadius = _trap.getTemplate().getFCollisionRadius();
			if (cha.getTemplate().isUsingServerSideName())
			{
				_name = cha.getName();
			}
			
			_title = cha.getOwner() != null ? cha.getOwner().getName() : "";
		}
		
		@Override
		public void writeImpl(GameClient client, WriteBuffer buffer)
		{
			if ((client != null) && client.isSalvationMode())
			{
				// Salvation (protocol 140) has no TrapInfo opcode: the reference renders traps via the mask-based NpcInfo layout.
				new NpcInfo(_trap, null).writeImpl(client, buffer);
				return;
			}
			
			ServerPackets.NPC_INFO.writeId(this, buffer);
			buffer.writeInt(_trap.getObjectId());
			buffer.writeInt(_displayId + 1000000); // npctype id
			buffer.writeInt(_isAttackable);
			buffer.writeInt(_x);
			buffer.writeInt(_y);
			buffer.writeInt(_z);
			buffer.writeInt(_heading);
			buffer.writeInt(0);
			buffer.writeInt(_mAtkSpd);
			buffer.writeInt(_pAtkSpd);
			buffer.writeInt(_runSpd);
			buffer.writeInt(_walkSpd);
			buffer.writeInt(_swimRunSpd);
			buffer.writeInt(_swimWalkSpd);
			buffer.writeInt(_flyRunSpd);
			buffer.writeInt(_flyWalkSpd);
			buffer.writeInt(_flyRunSpd);
			buffer.writeInt(_flyWalkSpd);
			buffer.writeDouble(_moveMultiplier);
			buffer.writeDouble(_trap.getAttackSpeedMultiplier());
			buffer.writeDouble(_collisionRadius);
			buffer.writeDouble(_collisionHeight);
			buffer.writeInt(_rhand); // right hand weapon
			buffer.writeInt(_chest);
			buffer.writeInt(_lhand); // left hand weapon
			buffer.writeByte(1); // name above char 1=true ... ??
			buffer.writeByte(1);
			buffer.writeByte(_trap.isInCombat());
			buffer.writeByte(_trap.isAlikeDead());
			buffer.writeByte(_isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
			buffer.writeInt(-1); // High Five NPCString ID
			buffer.writeString(_name);
			buffer.writeInt(-1); // High Five NPCString ID
			buffer.writeString(_title);
			buffer.writeInt(0); // title color 0 = client default
			buffer.writeInt(_trap.getPvpFlag());
			buffer.writeInt(_trap.getKarma());
			buffer.writeInt(_trap.isInvisible() ? _trap.getAbnormalVisualEffects() | AbnormalVisualEffect.STEALTH.getMask() : _trap.getAbnormalVisualEffects());
			buffer.writeInt(0); // clan id
			buffer.writeInt(0); // crest id
			buffer.writeInt(0); // C2
			buffer.writeInt(0); // C2
			buffer.writeByte(0); // C2
			buffer.writeByte(_trap.getTeam().getId());
			buffer.writeDouble(_collisionRadius);
			buffer.writeDouble(_collisionHeight);
			buffer.writeInt(0); // C4
			buffer.writeInt(0); // C6
			buffer.writeInt(0);
			buffer.writeInt(0); // CT1.5 Pet form and skills
			buffer.writeByte(1);
			buffer.writeByte(1);
			buffer.writeInt(0);
		}
	}
	
	/**
	 * Packet for summons.
	 */
	public static class SummonInfo extends AbstractNpcInfo
	{
		private final Summon _summon;
		private final int _form;
		private final int _value;
		
		public SummonInfo(Summon cha, Creature attacker, int value)
		{
			super(cha, (attacker != null) && attacker.isGM());
			_summon = cha;
			_value = value;
			_form = cha.getFormId();
			_isAttackable = cha.isAutoAttackable(attacker);
			_rhand = cha.getWeapon();
			_lhand = 0;
			_chest = cha.getArmor();
			_enchantEffect = cha.getTemplate().getWeaponEnchant();
			_name = cha.getName();
			_title = (cha.getOwner() != null) && cha.getOwner().isOnline() ? cha.getOwner().getName() : "";
			_displayId = cha.getTemplate().getDisplayId();
			_collisionHeight = cha.getTemplate().getFCollisionHeight();
			_collisionRadius = cha.getTemplate().getFCollisionRadius();
		}
		
		@Override
		public void writeImpl(GameClient client, WriteBuffer buffer)
		{
			if ((client != null) && client.isSalvationMode())
			{
				// SALVATION_140_SUMMON_INFO: mask-based layout (SUMMON_INFO opcode).
				ServerPackets.SUMMON_INFO.writeId(this, buffer);
				final String summonName = _summon.getName() != null ? _summon.getName() : "";
				final String summonTitle = _title != null ? _title : "";
				int initSize = NpcInfoType.ATTACKABLE.getBlockLength() + NpcInfoType.RELATIONS.getBlockLength() + NpcInfoType.TITLE.getBlockLength() + (summonTitle.length() * 2);
				int blockSize = NpcInfoType.ID.getBlockLength() + NpcInfoType.POSITION.getBlockLength() + NpcInfoType.STOP_MODE.getBlockLength() + NpcInfoType.MOVE_MODE.getBlockLength() + NpcInfoType.PET_EVOLUTION_ID.getBlockLength() + NpcInfoType.NAME.getBlockLength() + (summonName.length() * 2);
				final byte[] salvationMasks = new byte[5];
				addNpcInfoMask(salvationMasks, NpcInfoType.ATTACKABLE);
				addNpcInfoMask(salvationMasks, NpcInfoType.RELATIONS);
				addNpcInfoMask(salvationMasks, NpcInfoType.TITLE);
				addNpcInfoMask(salvationMasks, NpcInfoType.ID);
				addNpcInfoMask(salvationMasks, NpcInfoType.POSITION);
				if (_heading > 0)
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.HEADING);
					blockSize += NpcInfoType.HEADING.getBlockLength();
				}
				if ((_pAtkSpd > 0) || (_mAtkSpd > 0))
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.ATK_CAST_SPEED);
					blockSize += NpcInfoType.ATK_CAST_SPEED.getBlockLength();
				}
				if (_summon.getRunSpeed() > 0)
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.SPEED_MULTIPLIER);
					blockSize += NpcInfoType.SPEED_MULTIPLIER.getBlockLength();
				}
				if ((_rhand > 0) || (_chest > 0))
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.EQUIPPED);
					blockSize += NpcInfoType.EQUIPPED.getBlockLength();
				}
				addNpcInfoMask(salvationMasks, NpcInfoType.STOP_MODE);
				addNpcInfoMask(salvationMasks, NpcInfoType.MOVE_MODE);
				if (_summon.isInsideZone(ZoneId.WATER) || _summon.isFlying())
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.SWIM_OR_FLY);
					blockSize += NpcInfoType.SWIM_OR_FLY.getBlockLength();
				}
				if (_summon.getTeam() != Team.NONE)
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.TEAM);
					blockSize += NpcInfoType.TEAM.getBlockLength();
				}
				if (_enchantEffect > 0)
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.ENCHANT);
					blockSize += NpcInfoType.ENCHANT.getBlockLength();
				}
				if (_summon.isFlying())
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.FLYING);
					blockSize += NpcInfoType.FLYING.getBlockLength();
				}
				if (_summon.getCurrentHp() <= _summon.getMaxHp())
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.CURRENT_HP);
					blockSize += NpcInfoType.CURRENT_HP.getBlockLength();
				}
				if (_summon.getCurrentMp() <= _summon.getMaxMp())
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.CURRENT_MP);
					blockSize += NpcInfoType.CURRENT_MP.getBlockLength();
				}
				if (_summon.getMaxHp() > 0)
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.MAX_HP);
					blockSize += NpcInfoType.MAX_HP.getBlockLength();
				}
				if (_summon.getMaxMp() > 0)
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.MAX_MP);
					blockSize += NpcInfoType.MAX_MP.getBlockLength();
				}
				if (_isSummoned)
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.SUMMONED);
					blockSize += NpcInfoType.SUMMONED.getBlockLength();
				}
				if (_summon.getPvpFlag() > 0)
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.PVP_FLAG);
					blockSize += NpcInfoType.PVP_FLAG.getBlockLength();
				}
				if (_summon.getKarma() != 0)
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.REPUTATION);
					blockSize += NpcInfoType.REPUTATION.getBlockLength();
				}
				int statusMask = 0;
				if (_summon.isInCombat())
				{
					statusMask |= 0x01;
				}
				if (_summon.isDead())
				{
					statusMask |= 0x02;
				}
				if (_summon.isTargetable())
				{
					statusMask |= 0x04;
				}
				{
					statusMask |= 0x08; // Summons always show their name.
				}
				if (statusMask != 0x00)
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.VISUAL_STATE);
					blockSize += NpcInfoType.VISUAL_STATE.getBlockLength();
				}
				final List<Short> summonAves = SalvationAveMap.toClientIds(_summon.getAbnormalVisualEffects(), _gmSeeInvis && _summon.isInvisible());
				if (!summonAves.isEmpty())
				{
					addNpcInfoMask(salvationMasks, NpcInfoType.ABNORMALS);
				}
				
				buffer.writeInt(_summon.getObjectId());
				buffer.writeByte(_isSummoned ? 2 : _value);
				buffer.writeShort(37); // mask_bits_37
				buffer.writeBytes(salvationMasks);
				
				// Block 1
				buffer.writeByte(initSize);
				buffer.writeByte(_isAttackable);
				buffer.writeInt(0); // Relations
				buffer.writeString(summonTitle);
				
				// Block 2
				buffer.writeShort(blockSize);
				buffer.writeInt(_displayId + 1000000);
				buffer.writeInt(_x);
				buffer.writeInt(_y);
				buffer.writeInt(_z);
				if (_heading > 0)
				{
					buffer.writeInt(_heading);
				}
				if ((_pAtkSpd > 0) || (_mAtkSpd > 0))
				{
					buffer.writeInt(_pAtkSpd);
					buffer.writeInt(_mAtkSpd);
				}
				if (_summon.getRunSpeed() > 0)
				{
					buffer.writeFloat((float) _moveMultiplier);
					buffer.writeFloat((float) _summon.getAttackSpeedMultiplier());
				}
				if ((_rhand > 0) || (_chest > 0))
				{
					buffer.writeInt(_rhand);
					buffer.writeInt(_chest);
					buffer.writeInt(0);
				}
				buffer.writeByte(!_summon.isDead());
				buffer.writeByte(_summon.isRunning());
				if (_summon.isInsideZone(ZoneId.WATER) || _summon.isFlying())
				{
					buffer.writeByte(_summon.isInsideZone(ZoneId.WATER) ? 1 : _summon.isFlying() ? 2 : 0);
				}
				if (_summon.getTeam() != Team.NONE)
				{
					buffer.writeByte(_summon.getTeam().getId());
				}
				if (_enchantEffect > 0)
				{
					buffer.writeInt(_enchantEffect);
				}
				if (_summon.isFlying())
				{
					buffer.writeInt(_summon.isFlying());
				}
				buffer.writeInt(0); // Pet evolution id
				if (_summon.getCurrentHp() <= _summon.getMaxHp())
				{
					buffer.writeInt((int) _summon.getCurrentHp());
				}
				if (_summon.getCurrentMp() <= _summon.getMaxMp())
				{
					buffer.writeInt((int) _summon.getCurrentMp());
				}
				if (_summon.getMaxHp() > 0)
				{
					buffer.writeInt(_summon.getMaxHp());
				}
				if (_summon.getMaxMp() > 0)
				{
					buffer.writeInt(_summon.getMaxMp());
				}
				if (_isSummoned)
				{
					buffer.writeByte(0);
				}
				buffer.writeString(summonName);
				if (_summon.getPvpFlag() > 0)
				{
					buffer.writeByte(_summon.getPvpFlag());
				}
				if (_summon.getKarma() != 0)
				{
					buffer.writeInt(_summon.getKarma());
				}
				if (statusMask != 0x00)
				{
					buffer.writeByte(statusMask);
				}
				if (!summonAves.isEmpty())
				{
					buffer.writeShort(summonAves.size());
					for (short aveClientId : summonAves)
					{
						buffer.writeShort(aveClientId);
					}
				}
				return;
			}
			

			ServerPackets.NPC_INFO.writeId(this, buffer);
			buffer.writeInt(_summon.getObjectId());
			buffer.writeInt(_displayId + 1000000); // npctype id
			buffer.writeInt(_isAttackable);
			buffer.writeInt(_x);
			buffer.writeInt(_y);
			buffer.writeInt(_z);
			buffer.writeInt(_heading);
			buffer.writeInt(0);
			buffer.writeInt(_mAtkSpd);
			buffer.writeInt(_pAtkSpd);
			buffer.writeInt(_runSpd);
			buffer.writeInt(_walkSpd);
			buffer.writeInt(_swimRunSpd);
			buffer.writeInt(_swimWalkSpd);
			buffer.writeInt(_flyRunSpd);
			buffer.writeInt(_flyWalkSpd);
			buffer.writeInt(_flyRunSpd);
			buffer.writeInt(_flyWalkSpd);
			buffer.writeDouble(_moveMultiplier);
			buffer.writeDouble(_summon.getAttackSpeedMultiplier());
			buffer.writeDouble(_collisionRadius);
			buffer.writeDouble(_collisionHeight);
			buffer.writeInt(_rhand); // right hand weapon
			buffer.writeInt(_chest);
			buffer.writeInt(_lhand); // left hand weapon
			buffer.writeByte(1); // name above char 1=true ... ??
			buffer.writeByte(1); // always running 1=running 0=walking
			buffer.writeByte(_summon.isInCombat());
			buffer.writeByte(_summon.isAlikeDead());
			buffer.writeByte(_isSummoned ? 2 : _value); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
			buffer.writeInt(-1); // High Five NPCString ID
			buffer.writeString(_name);
			buffer.writeInt(-1); // High Five NPCString ID
			buffer.writeString(_title);
			buffer.writeInt(1); // Title color 0=client default
			buffer.writeInt(_summon.getPvpFlag());
			buffer.writeInt(_summon.getKarma());
			buffer.writeInt(_gmSeeInvis && _summon.isInvisible() ? _summon.getAbnormalVisualEffects() | AbnormalVisualEffect.STEALTH.getMask() : _summon.getAbnormalVisualEffects());
			buffer.writeInt(0); // clan id
			buffer.writeInt(0); // crest id
			buffer.writeInt(0); // C2
			buffer.writeInt(0); // C2
			buffer.writeByte(_summon.isInsideZone(ZoneId.WATER) ? 1 : _summon.isFlying() ? 2 : 0); // C2
			buffer.writeByte(_summon.getTeam().getId());
			buffer.writeDouble(_collisionRadius);
			buffer.writeDouble(_collisionHeight);
			buffer.writeInt(_enchantEffect); // C4
			buffer.writeInt(0); // C6
			buffer.writeInt(0);
			buffer.writeInt(_form); // CT1.5 Pet form and skills
			buffer.writeByte(1);
			buffer.writeByte(1);
			buffer.writeInt(_summon.getAbnormalVisualEffectSpecial());
		}
	}
}
