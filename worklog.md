
---
Task ID: 20
Agent: main
Task: Фаза 4 - дубли предметов в инвентаре; порт ItemList-оркестрации, InventoryUpdate, StatusUpdate под протокол 140

Work Log:
- Лог теста v1.4: вход в мир УСПЕШЕН (UserInfo-графт фазы 3 сработал), игрок ходит, открывает инвентарь; НО каждое открытие инвентаря наслаивает записи (дубли), HP-строка кривая (1827/182), спам REQUEST_SAVE_INVENTORY_ORDER и REQUEST_ITEM_LIST
- Аудит по эталону: RequestSaveInventoryOrder идентичен (не виновник); writeItem140 совпадает байт-в-байт с эталоном; причины найдены в ОРКЕСТРАЦИИ и трёх других структурах:
  1) ItemList: эталон шлёт КВАРТЕТ ItemList(1)+ItemList(2)+ExQuestItemList(1)+ExQuestItemList(2); type 1 = header-only (byte 1, int 0, int size, block) = сброс/показ окна из кеша клиента; мы шлём только type 2 - клиент наслаивает
  2) InventoryUpdate (AbstractInventoryUpdate.writeItems): 140 = byte 0, int 0, writeInt count, [short change + mask-item]; наш H5 = short count, [short change + фикс-структура]
  3) StatusUpdate: 140 = int objId, int casterId, byte visible, byte count, [byte id, int value]; наш H5 = int objId, int count, [int id, int value] - объясняет кривой HP
- scripts/patch_inventory_phase4.py (17 патчей, CRLF-aware, идемпотентный): ItemList и ExQuestItemList переписаны (sendType 1/2, конструкторы (int,Player)); Player.sendItemList и EnterWorld - квартет в salvation-режиме; ItemList.runImpl не автошлёт ExQuestItemList для salvation; AbstractItemPacket.writeItem140 получил ItemInfo-ядро; writeItems(buffer, client) с 140-префиксом; StatusUpdate - 140 wire-формат (visible = playable && CUR_HP/MP/CP, caster = objectId)
- Ошибка компиляции: final _showWindow присваивался дважды при делегировании конструктора ItemList - развёрнуто без this()-делегирования; exit 0 (1909 файлов)
- Коммит + пуш + CI + Release v1.5

Stage Summary:
- Ожидаемый тест v1.5: инвентарь открывается чисто при каждом открытии, без дублей; HP/CP/MP согласованы; поднятие/надевание/выбрасывание вещей обновляются корректно (InventoryUpdate 140)
- Следующие вероятные точки: ShortcutInit (5 страниц быстрых слотов на скрине - возможно кривой формат), склад (WarehouseDeposit/WithdrawList - масочный writeItem(WarehouseItem) не портирован), торговля (TradeUpdate)
- Push a9837c74..e28dcefe SUCCESS (amend после chmod-фикса 755->644); CI 40684166558 success
- Артефакт 12813443778 (23.8MB -> внутренний zip 28,723,168B) в scripts/v15_extract/inner; верификация javap: ItemList(int,Player), ExQuestItemList(int,Player), StatusUpdate._isPlayable на месте
- Release v1.5 опубликован (id 426615276): https://github.com/PowerAndMones/lineagehighfive/releases/download/v1.5/L2J_Mobius_CT_2.6_HighFive_x50_Salvation140.zip; анонимная проверка HTTP 200

---
Task ID: 21
Agent: main
Task: Фаза 5 - невидимые мобы/NPC; масочный NpcInfo протокола 140

Work Log:
- Тест v1.5: дубли инвентаря ИСЧЕЗЛИ, статус-бары ок; НО мобы и NPC невидимы; слоты экипировки "кривые"; альтернативная system-папка падает после выбора сервера (FL2OneDayRewardSystem::Serialize <- LoadAlternateGameData <- OnChangeServerType)
- NpcInfo: эталон - масочный (37 бит NpcInfoType: block1 attackable/relations/title + block2 по фиксированному порядку), наш HF - фиксированный CT2.6 - клиент молча дропал все NPC-пакеты
- scripts/patch_npcinfo_phase5.py: новый enums/NpcInfoType.java; AbstractNpcInfo.NpcInfo.writeImpl - salvation-ветка (маски+размеры строятся в writeImpl, порядок блоков из эталона; локальные: Lv/[A]/[G] титулы, чемпион-титул, клановые crest'ы, server-side name/title). Не портировано: ABNORMALS (в H5 AVE - bitmask без client id), NPCStringId, clone/transformation/follow; SummonInfo/TrapInfo остались H5
- Урок: якорь не совпал - реальная глубина 3 таба (Read показывает widen), в скрипт добавлен reindent()
- Слоты экипировки: ItemInfo.getLocation()=getLocationSlot, BodyPart маски, getEquipped - ВСЁ идентично эталону; CharSelected paperdoll-блока в 140 не имеет; причина не найдена статически - нужен скриншот от пользователя
- Альтернативная system: KeyPacket наш соответствует эталону (isClassic byte на месте); креш - клиентский DAT (сборка 2018) - юзеру использовать родную system
- ИНЦИДЕНТ: песочница откатилась к снапшоту v1.3 (git HEAD=0499e954, объекты a9837c74/e28dcefe пропали, /tmp/jdk21 снесён); восстановлено git fetch + reset --hard FETCH_HEAD (0727553b); JDK 21 перекачан
- Компиляция 1910 файлов exit 0

Stage Summary:
- Ожидаемый тест v1.6: мобы и NPC видимы, с именами/титулами; бой (атака мобов) следующий кандидат (Attack/StatusUpdate уже 140)
- CharInfo (другие игроки) в эталоне Grand-Crusade формата - портировать отдельной итерацией
- Push 0727553b..ce1292ab SUCCESS; CI 40688628500 success; артефакт верифицирован javap (NpcInfoType.class + addNpcInfoMask в jar)
- Release v1.6 опубликован (id 426647369): https://github.com/PowerAndMones/lineagehighfive/releases/download/v1.6/L2J_Mobius_CT_2.6_HighFive_x50_Salvation140.zip; анонимная проверка HTTP 200
- Примечание: середина сессии - Environment glitch #2 (файл AbstractNpcInfo мигал версиями, в индекс попала испорченная); вылечено фикс+ретраи+немедленный git add; в релиз ушёл корректный байт-код (CI компилирует из git)

Task ID: 22
Agent: main
Task: Фаза 5.2 - полный системный проход по протоколу 140 (опкоды + форматы + ExUserInfo)

Work Log:
- Тест v1.6 (лог 28/10 17:53): мобы/NPC видимы и атакуемы, НО без имён; телепорт в город - чёрный экран, персонаж исчез; скиллы кастуются. Запрос пользователя: "досконально пройтись по сборкам и что нужно с 5.0 Salvation сделать в HighFive" - не по одной фиче, а всё сразу
- ИНВЕНТАРИЗАЦИЯ: serverpackets REF=447 файлов vs OURS=390; ServerPackets REF=763 констант vs OURS=485
- ГЛАВНОЕ ОТКРЫТИЕ: таблицы SP-опкодов H5 и 140 различаются системно - 146 совпадающих по имени констант с разными ID (SkillList 0x5F->0x40, ExQuestItemList 0xC6->0xC7, ExBasicActionList 0x5F->0x60, ~143 Ex-сдвиги на +1). Т.е. клиент 140 молча дропал SkillList (в логе повторный REQUEST_SKILL_LIST - клиент не получал ответ!), квест-инвентарь, ExBasicActionList и пр.
- CM-таблица: расхождение ТОЛЬКО 0x38 RequestMagicSkillList (класса у нас нет; тихо игнорится)
- Имена NPC: XSD/датапак эталона тоже не шлёт NAME (usingServerSideName=false везде) - клиент 140 на эталоне берёт имена из DAT; у пользователя DAT-имена не показались -> форсировали hasName=true в salvation-ветке NpcInfo
- Чёрный город: наш TeleportToLocation обрезан (нет финального int 0 "Unknown"), ValidateLocation без финального byte 1; DeleteObject int вместо byte; SpawnItem без 3-byte хвоста
- Кривые ячейки экипировки: эталон шлёт ExUserInfoEquipSlot (0xFE 0x156) на входе (EnterWorld) и при каждой экипировке (Inventory.setPaperdollItem); наш вообще не шёл - клиент 140 рисует paperdoll из него. Плюс ExSubjobInfo/ExUserInfoInvenWeight/ExAdenaInvenCount/ExVitalityEffectInfo/ExWorldChatCnt
- Центральный механизм: ServerPacket.getCurrentWritingClient() (ThreadLocal, set/clear в write()) + ServerPacketsSalvation.java (146 override'ов) + writeId() проверяет режим клиента; 10 новых констант enum (EX_USER_INFO_* и др. с 140-значениями)
- Новые файлы (11): ServerPacketsSalvation, InventorySlot (маппинг в НАШИ paperdoll-индексы, AGATHION/BROOCH=-1), ExUserInfoEquipSlot, ExUserInfoInvenWeight, ExAdenaInvenCount, ExUserInfoCubic, ExUserInfoAbnormalVisualEffect, ExSubjobInfo, ExVitalityEffectInfo, ExWorldChatCnt, SalvationAveMap (49 общих AVE: H5-маска -> 140 clientId)
- Форматные ветки 140: Attack (первый хит inline + int flags + grade), MagicSkillUse (castingType, reuseGroup, actionId tail), ShortcutInit (ITEM aug/visual int'ы, SKILL short level+subLevel), SkillList (short level/subLevel, reuseDelayGroup, tail lastLearned), HennaInfo (8 short статов + premium-блок), RelationChanged (byte mask header, byte flags), ExStorageMaxCount (12 int), CreatureSay (WHISPER tail mask+level), Die (7 флагов + hideAnimation + items), CharInfo (полная 140-ветка writeSalvation140)
- EnterWorld: salvation -> шлёт ExSubjobInfo, ExUserInfoInvenWeight, ExAdenaInvenCount, ExUserInfoEquipSlot, ExVitalityEffectInfo, ExWorldChatCnt после ShortcutInit
- Inventory.setPaperdollItem: broadcast ExUserInfoEquipSlot при каждой смене экипировки (salvation)
- Фиксы сборки: двойные @Override (replace_method оставлял старый), ServerPacket вставка поля (anchor '{' не совпал - tab), SkillList потерял inner class Skill (восстановлен), ShortcutInit _player field добавлен, \timport -> import
- Компиляция 1921 файлов exit 0; javap-верификация (getCurrentWritingClient, ServerPacketsSalvation, _player, ExUserInfoEquipSlot)

Stage Summary:
- Ожидаемый тест v1.7: (1) имена у NPC/мобов; (2) телепорт без чёрного города; (3) экипировка в правильных ячейках (ExUserInfoEquipSlot + корректный SkillList/ShortcutInit); (4) список скиллов отображается (опкод 0x40); (5) другие игроки/декоры видны (CharInfo 140)
- Следующие кандидаты: SummonInfo/PetInfo (0x8B) для петов, ExPetInfo; CM 0x38 RequestMagicSkillList (ответить ExAcquirableSkillListByClass-заглушкой)

## Task 22 - Phase 6: mass wire-format port (v1.8)

Дата: 28/10/2026. Запрос пользователя: "Пройдись по всем скриптам 5.0 Salvation и сделай все в highfive чтобы вся совместимость была в каждом файле".

Аудит (scripts/audit_wire_diff3.py, audit_triage.py, audit_branches.py):
- 129 serverpackets/clientpackets с реальными различиями wire-форматов + 57 REF-ONLY пакетов
- Проверка существующих веток: UserInfo/CharInfo/NpcInfo-маска/ItemList/RelationChanged подтверждены байт-в-байт корректными

ЧЁРНЫЙ ГОРОД ПОСЛЕ ТЕЛЕПОРТА - НАЙДЕНА ПРИЧИНА: эталон после телепорта шлёт ExTeleportToLocationActivate (0xFE 0x14A) "teleport finished" - у нас отсутствовал. Создан пакет + отправка в Creature.onTeleported/Player.onTeleported.

СУММА (102 файла, +2635 строк):
- ServerPackets: +EX_TELEPORT_TO_LOCATION_ACTIVATE(0xFE,0x14A), +EX_PET_INFO(0xFE,0x15E), +SUMMON_INFO(0x8B)
- Новый ExTeleportToLocationActivate.java
- SystemMessage/ConfirmDlg: 140-сериализация (short id, byte счётчик, byte тип; castle/system/instance/class = short; +TYPE_BYTE/CLASS_ID/POPUP_ID/FACTION_NAME константы)
- AbnormalType.getClientId(): статическая карта 549 clientId из эталона
- Абнормальные иконки: AbnormalStatusUpdate/PartySpelled/ExOlympiadSpelledInfo (subLevel + abnormalType + short-time блоки)
- Пати: PartySmallWindowAdd/All (byte/short поля, vitality, race short), PartySmallWindowUpdate (mask-based 512-flags)
- Петомцы: SummonInfo -> SUMMON_INFO 0x8B маска-лейаут (для чужих); PetInfo -> полный PetSummonInfo лейаут (для владельца); ExPartyPetWindowAdd/Update/Delete; PetStatusShow/Update
- Магазины/трейд: BuyList/ExBuySellList/BuyListSeed(manus)/MultiSellList (полный 140-лейаут: entry header + augment/elemental/ensoul блоки)/ShopPreviewList/TradeStart (квартет sendType 1+2 как ItemList, sender в Player)+TradeOwnAdd/OtherAdd/Update (writeItem140)/PrivateStoreManageList* — wait, не ported: PrivateStore*, GMView*(частично)
- writeItem140: +overload с countOverride + writeItem140Product (Product)
- Склад: WareHouseDepositList/WithdrawalList sendType 2
- Почта: ExReplyPostItemList/ExReplyReceivedPost/ExReplySentPost/ExShowReceivedPostList/ExShowSentPostList
- Хенны: HennaEquipList (+HennaInfo был готов)
- Клан: PledgeCrest/AllyCrest(энюм ALLIANCE_CREST)/PledgeInfo/PledgeShowInfoUpdate/PledgeShowMemberListUpdate/PledgeStatusChanged/PledgeSkillList
- Misc: MyTargetSelected(+int 1 marker), ActionFailed(castingType 0), JoinParty/SocialAction(+tail), StopRotation(int), MagicSkillLaunched(castingType), FlyToLocation(+3 int), DropItem(byte stackable+tail), FriendAddRequest, ExAutoSoulShot(3 int), ExShowQuestMark, TutorialShowHtml/QuestionMark, NpcHtmlMessage(+int), EnchantResult(+level+options), ShortBuffStatusUpdate(sublevel), EtcStatusUpdate(7 полей + mask), AcquireSkillInfo(long spCost, type id), AcquireSkillList(140-шейп), AskJoinAlly/Pledge, ExAskJoinMPCC/PartyRoom, ExHeroList, ExGetPremiumItemList, ExShowCastleInfo, ObservationMode, ExChooseInventoryAttributeItem(count+stone list), ExCubeGameEnd, ExEnchantSkillList, ExListPartyMatchingWaitingRoom, ExUserInfoCubic(cubic ids), SendMacroList
- CM (19 файлов read-side): RequestAutoSoulShot(3), RequestDispel(short level), RequestExEnchantItemAttribute(+long count), RequestExEnchantSkill*/Info/InfoDetail(short levels), RequestExPledgeCrestLarge/RequestPledgeCrest(+clanId), RequestShortcutReg(short subLevel), RequestTutorialLinkHtml/QuestionMark, MultiSellChoose(ensoul tail), RequestListPartyMatchingWaitingRoom(class list+query), RequestMakeMacro(icon int), RequestPrivateStoreSell(visual/augment/ensoul хвосты), RequestRecipeItemMakeSelf/RequestRecipeShopMakeItem(offerings), SetPrivateStoreListBuy(полный 140-лейаут)
- ShortcutInit: augment option1/option2 split (augId>>16, &0xFFFF)

Не портировано (нет данных в H5-модели, отложено): GMViewCharacterInfo/GMViewSkillInfo/GMViewPledgeInfo/GmViewQuestInfo/GMHennaInfo (GM-просмотр), FakePlayerInfo (fake players), SiegeDefenderList, ExOlympiadMatchResult, ExOlympiadUserInfo, PrivateStoreManageListBuy/Sell/PrivateStoreListBuy/Sell (частично актуально), BuyListSeed, ExBeautyItemList/ExResponseBeautyList (beauty shop), MPCC room пакеты, pledge recruit пакеты
Компиляция 1922 файлов exit 0.

Ожидаемый тест v1.8: (1) чёрный город после телепорта должен уйти (ExTeleportToLocationActivate); (2) инвентарь/трейд/склад/мультиселл/почта корректные списки; (3) бафы-иконки с таймерами; (4) скиллы к изучению у NPC; (5) макросы; (6) петы видны/с именами; (7) system-сообщения корректного формата (критично: id short + byte типы)

## Task 24 - v1.9: QuestList - маска выполненных квестов (вопрос пользователя про квесты)

Дата: 29/10/2026. Вопрос пользователя: "А квесты все так же работают? Просто они нормально не отображались".

Аудит квест-пакетов (наш vs эталон Salvation 5.0):
- QuestList: опкод 0x86 совпадает; wire-формат (short count + int id + int status + 128 bytes tail) совпадает. НАЙДЕНО: наш tail = 128 нулевых байт, эталон шлёт РЕАЛЬНУЮ bitmask выполненных one-time квестов (id 1..255 и 10256..11023; index=(id%10000)/8, bit=id%8). Клиент 140 не отмечал выполненные квесты в журнале
- ExQuestNpcLogList: wire-совместим (int id+1000000, byte flag, int count); NPCString-вариант в наших данных не используется
- ExShowQuestInfo, NpcQuestHtmlMessage: идентичны эталону
- ExQuestItemList: 140-ветка с фазы 4 (квартет); ExShowQuestMark/TutorialShowQuestionMark: ветки с фазы 6
- Player.getAllActiveQuests() корректен (started, 1..19999) - активные квесты попадали в окно; имя квеста клиент берёт из своего DAT (content-layer)

Патч scripts/patch_questlist_v19.py (CRLF+tab-aware, идемпотентный):
- QuestList: поле _oneTimeQuestMask; конструктор строит маску из getAllQuestStates() по эталону (completed && NOT((255<id<10256)||(id>11023))); writeImpl шлёт реальную маску вместо нулей. H5-клиентам безвредно (формат тот же, маска теперь честная)
- Player: акцессор getAllQuestStates() (return new ArrayList<>(_quests.values()))

Сборка: JDK21 перекачан в /tmp/jdk21 (снапшот-откат снёс); компиляция 1922 файлов exit 0.
Коммит 11007073 (2 файла, +21/-1), chmod-контроль пройден, push 83930ea5..11007073 SUCCESS, CI 40734386801.

Следующее: Release v1.9 после CI; пользователь тестирует квест-журнал (выполненные one-time квесты должны получить отметки).

## Task 25 - v1.10: аудит "мёртвой проводки" + добивка совместимости (пока пользователь не тестировал)

Дата: 29/10/2026. Запрос: "Еще что можно делать пока я не проверил".

Аудит scripts/audit_dead_wiring.py (146 salvation-ремапов):
- 46 констант без writeId-сайта (классов нет вовсе) - шаттлы/MPCC/доминион/клифты и пр., фичи не запускаются, безвредно
- 32 пакета "класс есть, никто не шлёт" - ПОСЛЕ ручной проверки почти все ложные срабатывания: static-инстансы (ExNoticePostArrived - MailManager шлёт STATIC_PACKET), холдеры (дуэли шлются из Duel.java, энчант-скиллы из RequestExEnchantSkill*), мёртвые и в эталоне (ExShowVariationMakeWindow, ExHeroList, ExPrivateMarketList, ExTutorialList - ref-constructs=0)
- ExQuestNpcLogList никем не шлётся - ОБОСНОВАНО: в нашей aCis-стилистике квестов счётчики убийств обновляются повторной отправкой QuestList (3 send-сайта в QuestState); NpcLogList-модели данных в форке нет
- ExInitializeSeed/REQUEST_SEED_PHASE: эталон тоже НЕ отвечает на seed phase CM - открытый вопрос закрыт, поведение идентично эталону
- ВЕРДИКТ: реальных мёртвых проводок НЕТ, v1.8 чистая

VersionCheck (KeyPacket): writeImpl совпадает с эталоном КРОМЕ obfuscation key (у нас не-ноль - осознанное решение адаптера, у эталона 0). isClassic-байт на месте. Краш на "чужой" system - клиентский DAT 2018, вопрос закрыт окончательно.

CM 0x38 RequestMagicSkillList: у нас был null-хендлер, эталон отвечает sendSkillList(). Порт:
- новый network/clientpackets/RequestMagicSkillList.java (по эталону: readInt objectId, проверка, sendSkillList)
- ClientPackets: (0x38, null, ...) -> (0x38, RequestMagicSkillList::new, ...)

NpcInfo ABNORMALS (визуалы бафов на NPC, фаза 5 откладывала):
- масочная фаза: SalvationAveMap.toClientIds(getAbnormalVisualEffects()|getAbnormalVisualEffectSpecial(), isInvisible()); addNpcInfoMask(ABNORMALS) + blockSize += 2*count
- write-фаза: short count + short clientId на эффект (паттерн скопирован с работающего SummonInfo фазы 6)
- блок последний, после VISUAL_STATE - порядок записи эталона (порядок enum не совпадает с порядком записи - проверено, enum идентичен)

TrapInfo -> 140: у эталона НЕТ TrapInfo-класса, ловушки шлются масочным NpcInfo (Trap.java: broadcastPacket(new NpcInfo(this))). Наш TrapInfo (H5-фикс) для 140-клиента = рассинхрон стрима при спавне ловушки. Fix: TrapInfo.writeImpl salvation-ветка делегирует new NpcInfo(_trap, null).writeImpl(client, buffer).
- Для этого NpcInfo/SummonInfo конструкторы сделаны null-safe: super(cha, (attacker != null) && attacker.isGM()) (был NPE на null)

Сборка: 1923 файла (+1 новый), exit 0. Коммит 4ff44040, chmod-контроль пройден, push, CI 40737980495.

Следующее: Release v1.10 после CI. Тест-кандидаты: окно скиллов (0x38 теперь отвечает), бафы на NPC (иконки над мобами под дебаффами), ловушки (Decoy/Flash Trap) не ломают мир.

## Task 26 - v2.0: современный Community Board (Alt+B) - бафер со схемами, магазин, класс-мастер, рейтинг

Дата: 29/10/2026. Запрос: "С alt+b можно сделать современным? А то его вообще как будто нету у нас с функциями" + скриншоты BOHPTS OLDSCHOOL x7.

Разведка:
- Ядро CB в репо цело: ShowBoard/RequestShowBoard (0x5E совпадает с эталоном), CommunityBoardHandler, 9 ванильных бордов зарегистрированы в MasterHandler
- HomeBoard = кастомная Mobius-версия (_bbsbuff/_bbsteleport/_bbsmultisell/_bbspremium/_bbsheal/_bbsdelevel), включается CustomCommunityBoard=True (в dist уже True)
- В ЯДРЕ обнаружен SchemeBufferTable (data/SchemeBufferSkills.xml, категории Buffs/Dances/Songs/Chants/Resist/Special + пресеты FIGHTER_GROUP/MAGE_GROUP, 142 бафа; схема в БД buffer_schemes; init в GameServer, save в Shutdown) - потребителя в датапаке НЕ БЫЛО, потому бафер "не существовал"
- Магазин CB был мёртв: HTML ссылался на несуществующие мультиселлы 600010/600014/600016

Реализовано:
1. HomeBoard.java (расширение): полноценный бафер на SchemeBufferTable: _bbsscheme (главная: пресеты Воин/Маг, отменить бафы, лечить, список схем [Наложить/Редактор/Удалить], сохранить текущие бафы в схему через edit var, категории) / cat / edit / add / rem / use / save / del / cancel / heal / cast / group; каст по максимальному уровню скилла (SkillData.getMaxLevel), порядок Dances->Songs->Chants->остальные, цена = сумма цен XML или BufferStaticCostPerBuff; лимит схем из конфига, валидация имени схемы (16 симв.)
2. Класс-мастер на CB: _bbsclass / change;<classId> - Player.setPlayerClass (полный цикл: академия, хенны, rewardSkills), требования 20/40/76, цены CommunityClassPriceFirst/Second/Third
3. RankBoard.java (новый, зарегистрирован в MasterHandler): _bbsrank pvp|pk|online|rich, топ-10 из characters (accesslevel=0) и items(адена SUM count), async через ThreadPool, табы
4. HTML (RU, стиль BOHPTS): navigation.html (Главная/Бафер/Телепорт/Магазин/Премиум/Рейтинг/Профессия/Поиск дропа/Информация), home.html (%online%, %player_name%), gatekeeper/main.html (RU города + Arcan/Ertheia/Floran/Gainak в конфиге), merchant/main.html (9 категорий), premium/main.html (RU, 1д/7д/30д), info/main.html (новая)
5. Мультиселлы 600010-600019 (9 файлов, <npc>-1</npc> для работы из CB - в этой ветке isNpcAllowed строже эталона): расходники/шоты/оружие S,A,B/броня A,S/бижутерия (эпики 6656-6660,8191)/энчант+ЛС. Все ID выверены по data/stats/items
6. SchemeBufferSkills.xml: desc -> русские названия (142/142)
7. CommunityBoardConfig.java: +CommunityEnableClassMaster, +CommunityClassPriceFirst/Second/Third (ядро, compile OK)
8. dist/sql/buffer_schemes.sql (CREATE TABLE IF NOT EXISTS)
9. CommunityBoard.ini: +4 города, CommunityPremiumSystem=True, цена/день 500000, +класс-мастер; PremiumSystem.ini: EnablePremiumSystem=True

Багфиксы после ревизии: null-safe getPlayerSchemes (add), дедуп по skillId при касте схемы/пресета (дубли категорийных бафов 307/306/308/270/309/311), null-guard separateAndSend в legacy _bbsbuff/_bbsheal, try-catch PlayerClass.getPlayerClass, табы RankBoard активным текстом.

Сборка: ядро 1923 файла exit 0; скрипты 1292 файла exit 0 (локальная валидация javac с класспасом ядра).

ДЕПЛОЙ (для пользователя): 1) обновить files/dist; 2) ВЫПОЛНИТЬ dist/sql/buffer_schemes.sql на game DB; 3) проверить config/Custom/CommunityBoard.ini на живом сервере (CustomCommunityBoard=True обязательно); 4) рестарт GS. Alt+B: бафер/схемы/телепорт/магазин/премиум/рейтинг/профессии.

---
Task 25b - v2.1: карточка персонажа на главной борда (top-up v2.0)
1. HomeBoard.java: +ServerConfig import; в custom-ветку _bbshome добавлены %slots% (MaximumOnlineUsers), %player_level%, %player_class% (ClassListData), %player_clan%, %player_pvp%, %player_pk%, %player_uptime% (formatUptime ч/м), %player_premium% (PremiumManager.getPremiumExpiration, ключ ACCOUNT_WIDE_PREMIUM ? account : name)
2. home.html: современный дизайн (панели L2UI_CT1.Windows_DF_TooltipBG 690px, шапка Онлайн X/Y, 9 вкладок, карточка ПЕРСОНАЖ, БЫСТРЫЕ ДЕЙСТВИЯ, новости); все bypass ведут на реальные хендлеры v2.0 (включая _bbsclass)
3. Валидация: ядро 1923 exit 0, скрипты 1292 exit 0
4. Примечание: локальная сессия независимо сделала то же направление (backup-v1.10-altb) - взята база v2.0, поверх неё добавлены плейсхолдеры и новая главная

## Task 27 - v2.1.1: хотфикс упаковки релиза (сервер падал при старте, Exit code 1)

Дата: 29/10/2026. Баг-репорт пользователя: "Ошибку почему то дает" + скриншот "Game Server terminated abnormally! Exit code: 1 (Uncaught Java exception or wrong Java version.) Check log/error.log".

Диагноз (без лога, по составу архивов):
- Сравнение zip v2.1 (26 920 419 байт) с v1.10 (28 775 509) и CI-артефактом коммита 4155889a (run 40813683917, 28 793 632) показало: в релизном архиве v2.1 ОТСУТСТВОВАЛИ libs/GameServer.jar (5.07 MB), libs/LoginServer.jar (298 KB), db_installer/DatabaseInstaller.jar, readme.txt, sql/buffer_schemes.sql
- Репак-скрипт релиза взял dist-контент и сторонние libs, но не включил собранные CI jar'ы; лишними оказались только -sources.jar (безвредно)
- Следствие: java -cp libs/* не находила Main-Class -> мгновенный exit 1 -> диалог лаунчера "Uncaught Java exception or wrong Java version" (ложное указание на Java)
- Проверено, что сам код v2.1 не виноват: SchemeBufferTable.loadPlayerSchemes обёрнут try/catch (отсутствие таблицы buffer_schemes не роняет GS); ScriptExecutor при ошибке компиляции скриптов бросает RuntimeException, но скрипты v2.0/v2.1 валидированы (1923 core + 1292 scripts exit 0)

Исправление:
- Взят нативный CI-артефакт коммита 4155889a (GameServer.jar Build-Date 2026-10-29 03:43:17, 2327 классов) - полный архив
- Опубликован релиз v2.1.1 (asset L2J_Mobius_CT_2.6_HighFive_x50_Salvation140_v2_1_1.zip, 28 793 632 байта)
- Битый asset v2.1 удалён и заменён тем же исправным архивом (старая ссылка теперь отдаёт рабочий zip)

Деплой для пользователя: скачать v2.1.1, распаковать с заменой, выполнить sql/buffer_schemes.sql на game DB, проверить config/Custom/CommunityBoard.ini (CustomCommunityBoard=True), рестарт.

Урок: перед публикацией релиза сверять список файлов архива с CI-артефактом (наличие GameServer.jar/LoginServer.jar) - добавить в чеклист publish-скриптов.

## Task 28 - v2.2: фикс Alt+B страниц (парсер HTML) + фикс окна "второго пароля" (чёрный экран после телепорта)

Дата: 29/10/2026. Баг-репорт: "Телепортнулся черный экран. И по середине экрана какой то пароль просит и еще алт-б кривое какой то и не открывает баффер телепорт" + 3 скриншота (окно вторичного пароля с цифровой клавиатурой; рабочий home борда; страница борда с ошибкой клиента "Nested tags not match - closed by TR while started with TABLE").

Баг 1: Alt+B - не открываются бафер/телепорт/магазин/премиум (ошибка парсера HTML клиента)
- navigation.html: каждая строка вкладок закрывалась ДВАЖДЫ ("</td></tr>" + лишний "</tr>" следующей строкой) - 8 лишних "</tr>"
- Все статические страницы (buffer/main, gatekeeper/main, merchant/main, premium/main, info/main, dropsearch) вставляют navigation через плейсхолдер %navigation% -> битая навигация ломала ВСЕ страницы
- home.html не использует %navigation% (свои 9 вкладок) - поэтому home рендерился, остальное нет
- Fix: убраны дубли "</tr>" (10 tr = 10 /tr); добавлен чекер scripts/check_cb_html.py (стек-валидатор как парсер клиента L2): 24 файла - 0 проблем; Java-страницы (HomeBoard/RankBoard) проверены подсчётом пар тегов - сбалансированы

Баг 2: окно "второго пароля" + чёрный экран после телепорта
- Клиент Salvation 140 шлёт CM D0 Ex 0xA6 REQUEST_EX_2ND_PASSWORD_CHECK в состоянии ENTERING (и повторно при реинициализации UI мира после телепорта)
- Наша мапа Salvation->local корректна (0xA6->0x93), но локальный enum пускал trio 2nd-password только в AUTHENTICATED -> ТИХИЙ дроп в Ex-пути GamePacketHandler (без лога!) -> сервер не отвечает Ex2ndPasswordCheck -> клиент показывает модальное окно пароля, блокирующее отрисовку (чёрный экран)
- Конфиг SecondaryAuth.xml enabled=false (как у эталона) - если бы пакет дошёл, хендлер ответил бы PASSWORD_OK и окна бы не было; проблема была в state-дропе
- Fix 1: ExClientPackets - REQUEST_EX_2ND_PASSWORD_CHECK/VERIFY/REQ теперь AUTHENTICATED+ENTERING+IN_GAME (хендлеры клиентские, Player не нужен - проверено)
- Fix 2: GamePacketHandler Ex-путь - дроп по состоянию теперь логируется в salvation-режиме (раньше молчал, что и спрятало баг); обычный путь уже логировал
- Красная селёдка из лога: "dropped client packet id=0xa6 ... ENTERING" - это обычный CM RequestSkillCoolTime (0xA6) в обычном пути, у эталона тоже IN_GAME-only, безвредно (клиент повторяет в IN_GAME)
- Опкоды сверены: Ex2ndPasswordCheck H5 FE:0xE5 -> Salvation FE:0x105 (override + translateOutgoingOpcode согласованы), входящая мапа 0xA6->0x93 на месте

Сборка: ядро 1923 файла exit 0. Деплой: заменить файлы, рестарт GS. SQL не нужен.

## Task 29 - v2.3: телепорт (чёрный экран), иконки бафов (0 сек), PIN-окно при входе, текстуры главной Alt+B

Дата: 29/10/2026. Баг-репорт после теста v2.2: "Телепорт так и не заработал. Алт-б кривое досихпор, но при переходе куда-то становится нормально, на главной все криво. Баффнутся смог, но половина бафов не видно и моргают как будто 0 сек осталось. При входе досихпор просит пароль какой-то" + лог GS (полная сессия 10:55-11:08) + скриншоты (PIN-окно "Бои Олимпиады" с клавиатурой; ошибка клиента "角色配对检查出现问题(7680)").

Ключевой факт из лога v2.2: клиент Salvation 140 так и НЕ прислал Ex 0xA6 (2nd password check) ни в AUTHENTICATED, ни в ENTERING - значит фикс v2.2 (расширение state-гейтов) не мог сработать, а диалог клиент рисует сам.

Баг 1: чёрный экран после телепорта (ДЕДЛОК)
- Сравнение с эталоном: в Creature.teleToLocation эталон шлёт ExTeleportToLocationActivate СРАЗУ после setXYZ ("Send teleport finished packet to player"), а у нас пакет уходил только из onTeleported()
- onTeleported() для присоединённого игрока вызывается только из Appearing (CM 0x3A) или TeleportWatchdogTask (включён при TeleportWatchdogTimeout>0, у нас дефолт 0 = выключен)
- Клиент Salvation завершает телепорт-переход ТОЛЬКО по ExTeleportToLocationActivate и шлёт Appearing только после него -> круговое ожидание: сервер ждёт Appearing, клиент ждёт Activate -> вечный чёрный экран. На стоковом HF-клиенте бага нет: там переход завершается по внутреннему таймеру
- Fix: ExTeleportToLocationActivate шлём немедленно в teleToLocation (только salvation-режим; в оригинальном HF enum пакета нет - HF-клиентам не шлём), из onTeleported (Creature+Player) убраны дубли; TeleportWatchdogTimeout default 0 -> 10 (страховка spawnMe)
- ExShowSeedMapInfo переведён на Salvation-формат [count]{[id][sysMsgId]} (был HF-формат с координатами - клиент читал мусор; клиент запрашивает seed phase при входе и вокруг телепорта)

Баг 2: иконки бафов - половина не видна, моргают "0 сек"
- Эталонный Salvation ServerPacket.writeOptionalInt: время = writeShort(value), либо writeShort(32767)+writeInt(value) если >= 32767. БЕЗ тегового байта
- Мы изобрели layout [byte 1 + short] / [byte 0 + int] - на 1 байт больше на каждый эффект -> десинхронизация AbnormalStatusUpdate после первого эффекта -> клиент парсит мусор (иконки пропадают, таймеры 0 сек)
- Fix: AbnormalStatusUpdate, PartySpelled, ExOlympiadSpelledInfo - правильный optional-int (toggle-скиллы шлют -1 как short)

Баг 3: PIN-окно при входе (фантомное "второй пароль")
- Раз клиент не шлёт Ex 0xA6 вообще, сервер должен ответить проактивно: Ex2ndPasswordCheck(PASSWORD_OK=2) отправляется в 3 точках: после CharSelectionInfo (LoginServerThread), перед CharSelected (CharacterSelect), отложенно через 2 сек после EnterWorld (закрывает диалог, если клиент его уже открыл)
- Идём по эталону: PASSWORD_OK = 2, формат пакета [windowType][0], SM Ex2ndPasswordCheck FE:0xE5 -> Salvation FE:0x105 ремапится корректно

Баг 4: Alt+B главная "кривая", остальные страницы нормальные
- home.html и navigation.html используют кнопки L2UI_CT1.OlympiadWnd_DF_* (26/18 штук) - набора нет в Essence-клиенте (кнопки рендерятся битыми боксами). Рабочие страницы (gatekeeper/buffer/merchant/premium/info) используют L2UI_CT1.Button_DF + Windows_DF_TooltipBG - потому и "при переходе нормально"
- Fix: все OlympiadWnd_DF_* заменены на Button_DF/Button_DF_Down в home.html и navigation.html
- CM 0xa6 RequestSkillCoolTime пущен в ENTERING (клиент шлёт его при инициализации UI мира)

Сборка: ядро 1923 файла exit 0. Деплой: заменить файлы, рестарт GS. SQL не нужен.
