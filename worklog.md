
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

## Task 30 - v2.4: порт Essence-систем (ежедневки, Beauty Shop, соло-инстанс) + аудит v2.2/v2.3

Дата: 29/10/2026. Запрос: «Ежедневки, соло инсты бюти шоп тоже можно. И досконально проверь предыдущие изменения».

Аудит предыдущих изменений (снапшот откатил репо к v2.1; восстановлено до v2.3 через fetch+reset, CRLF-шум 27691 файлов отброшен):
- Подтверждены на месте все фиксы v2.2/v2.3: Ex2ndPasswordCheck(PASSWORD_OK) в 3 точках (CharacterSelect:231, EnterWorld:471-474); ExTeleportToLocationActivate в Creature.onTeleported:879; AbnormalStatusUpdate 140-layout (sublevel+abnormalType+optional-int); RequestSkillCoolTime в ENTERING (ClientPackets:166); navigation.html без двойного </tr>, home/nav на Button_DF
- НАЙДЕН latent-баг: ExSendUIEvent шёл (0xFE 0x8E) без Salvation-override; у клиента 140 0x8E = ExNpcQuestHtmlMessage -> таймеры интерфейса (инстансы/события) клиент интерпретировал мусором. Fix: ServerPacketsSalvation +EX_SEND_UI_EVENT -> (0xFE, 0x8F)

Ресёрч (3 параллельных агента): Daily Missions = 11 ядерных классов + CM 0x142/0x143 + SM 0x1E0/0x1E1, XML 24 level-миссии, БД character_daily_rewards; Beauty Shop = 13 файлов, SM 0x134-0x137/0x179, CM 0xCA/CB/CD/E1, хранение в character_variables; соло-инстансы = полное ядро SAL (3.4k строк) нецелесообразно, минимальный путь - порт готового инстанса на HF-движке

Фаза A - Daily Missions (10 новых Java + скрипты, 9 правок):
- Ядро: DailyMissionData (XML-парсер + авто-создание таблицы), DailyMissionHandler (реестр фабрик, executeScript), AbstractDailyMissionHandler (SQL + выдача; спец-награды -200 репутация клана/-300 фама без SpecialItemType), DailyMissionDataHolder/PlayerEntry/Status (без dual-class)
- Пакеты: ExPledgeMissionInfo/ExPledgeMissionRewardCount (в enum сразу в Salvation-пространстве 0x1E0/0x1E1), RequestExPledgeMissionInfo/Reward (локальные id 0x98/0x99, SalvationProtocol CM-мапа [322]=152,[323]=153, ONLY_NAMES очищены)
- Интеграция: PlayerVariables.DAILY_MISSION_*, PlayerStat (пуш счётчика на левел-апе), Clan.addClanMember (пуш 2 пакетов), DailyResetManager.resetDailyMissionRewards (среда), GameServer init, ScriptEngine.ONE_DAY_REWARD_MASTER_HANDLER_FILE
- Скрипты: DailyMissionMasterHandler + LevelDailyMissionHandler (runtime-компиляция, exit 0)
- Датапак: DailyMission.xml (8 миссий 20/40/52/61/76/80/84/85 под max level 85; награды адена x50 + репутация + фама; Essence-предметы 45811+ в HF отсутствуют - заменены), XSD, character_daily_rewards.sql
- Инцидент: patch-скрипт применился 3 раза из-за неверной идемпотентности (insert-after-anchor) - дубликонстанты в ServerPackets/ExClientPackets вычищены; генератор таблиц SalvationProtocol ломал хвост (151;};) - исправлено; урок: идемпотентность по «new in text», таблицы править программно

Фаза B - Beauty Shop (12 новых Java + скрипт+HTML, 16 правок):
- Данные: BeautyShopData (isFemale->Sex вместо getSexType), BeautyData/BeautyItem (entity/beautyshop)
- Пакеты: 5 SM (ExBeautyItemList/ExResponseBeautyList/ExResponseBeautyRegistReset/ExResponseResetList/ExShowBeautyMenu - WriteBuffer вместо WritableBuffer), 4 CM (RequestShowBeautyList/RequestRegistBeauty/RequestShowResetShopList/NotifyExitBeautyShop - локальные 0x9A-0x9D, CM-мапа 202->154/203->155/205->156/225->157)
- Player: getBeautyTickets/reduceBeautyTickets (через getItemByItemId, без кэша инвентаря), visual-аксессоры visualHairId/visualHairColorId/visualFaceId (character_variables, fallback на базовый облик)
- UserInfo/CharInfo: APPAREANCE-блок и 2 ветки CharInfo пишут visual-значения; EnterWorld шлёт ExBeautyItemList; RequestActionUse case 89 (кнопка клиента) -> social 30 + broadcastUserInfo
- Датапак: BeautyShop.xml (6 рас, ERTHEIA вырезана - нет в HF Race), item 36308 (custom), NPC 33825+33854-33857 (custom, санитизация mpReward/skill-параметров), спавны 5 городов, LaVieEnRose.java (plain-строки вместо отсутствующих NpcStringId, sendMessage вместо SystemMessageId) + 5 HTML

Фаза C - соло-инстанс Nursery (id 171) на HF-движке (полный порт ядра SAL 3.4k строк отклонён):
- Выбор: Nursery - единственный XML-истинно-соло инстанс эталона (GroupMax=1, 85+); Kartia solo 2767 строк + 30 хелперов - следующий кандидат
- Instance XML HF-формата: activityTime 60, spawnPoints (вход под Морем Спор), exitPoint, reenter ON_FINISH time=43200000 (ВАЖНО: в HF time в МИЛЛИСЕКУНДАХ - CrystalCaverns 86400000; исходные 720 минут были бы 0.7 сек), 50 фиксированных точек спавна (конверсия территорий SAL: кольцо r=700-1800 вокруг центра)
- Скрипт: kill-for-points (3-8 за моба, 6% -> +40), CLOCK_TIMER 25 мин с ExSendUIEvent(ELAPSED_TIME), награда Tissue Energy Crystal (17602) по таблице + 40000*points exp, finishInstance(0); Essence-механики заменены: Maguen-мини-игра и energy-баффы (скиллы 14228-14235 отсутствуют) выпилены, SIXTH_* категории классов убраны
- Вход: зоны 23601/23602 (ScriptZone в custom_script.xml) + outdoor Tie x2 у коридоров; checkConditions: 85+, соло, reenter
- NPC 33152/23033-23037 из SAL (санитизация), items 17602/17610 (custom)

Сборка: ядро 1945 файлов exit 0; скрипты DailyMission/LaVieEnRose/Nursery - runtime-компиляция проверена javac отдельно (exit 0). CI 40849006089 success; артефакт 12886865088 верифицирован (2 jar + installer + новые файлы).
Релиз v2.4 опубликован (id 427663280): https://github.com/PowerAndMones/lineagehighfive/releases/download/v2.4/L2J_Mobius_CT_2.6_HighFive_x50_Salvation140_v2_4.zip; анонимная проверка HTTP 200 (28.850.599 байт).

Ожидаемый тест v2.4: (1) клан-окно Missions: список 8 миссий, статус по уровню, получение награды (адена/репутация/фама); (2) Beauty Shop: NPC в городах/кнопка клиента, покупка причёски за адену, облик виден другим; (3) Nursery: вход у Моря Спор (соло, 85+), таймер на экране, очки за мобов, награда у Tie; (4) таймеры интерфейса (0x8F) на экране.
Следующие кандидаты: Kartia solo 85 (полный порт), VIP Attendance (ежедневный вход), оставшиеся пробелы протокола (GMView*, FakePlayerInfo, SiegeDefenderList, олимпиадные, MPCC, pledge recruit).


## Task 31 - v2.5: модальное PIN-окно в мире (блок UI), чистка Nursery NPC

Дата: 29/10/2026. Баг-репорт после теста v2.4: «Инвентарь не открывается, с нпс нету половина что творится, баффы так в клиенте где HP MP CP не показывает и 0 сек на скриншоте видно» + лог GS + 2 скриншота (модальное окно с цифровой клавиатурой ПРЯМО В МИРЕ; ошибка клиента «检查角色力量时出现问题(7680)» в окне «Бои Олимпиады» через ~6 сек после входа).

Аудит (исчерпывающий, против эталона):
- Сверены ВСЕ 505 SM-опкодов после оверайдов с эталоном - 0 расхождений; коллизии id по чужим пакетам отсутствуют (для реально отправляемых)
- Побайтово сверены с эталоном и подтверждены корректными: ItemList-квартет (1/2 + ExQuestItemList 1/2, writeItem140 = эталонный writeItem), InventoryUpdate (writeByte0+writeInt0+size+short change+item), StatusUpdate (salvation-заголовок casterId/visible/count byte, таблица id 0x09/0x0A/0x0B/0x21 совпадает), SkillList (salvation-ветка = эталон), SkillCoolTime (TimeStamp._group всегда -1 в HF → формула sharedReuseGroup вырождается в skillId, эквивалентно эталону), AbnormalStatusUpdate/PartySpelled/ExOlympiadSpelledInfo (optional-int совпадает; isAura в HF Skill отсутствует, постоянные эффекты и так шлют -1), Ex2ndPasswordCheck ([2][0] байт-в-байт), CharSelectionInfo (salvation-заголовок + per-char = эталон), ExBeautyItemList
- CharacterSelect: SSQInfo -> Ex2ndPasswordCheck(PASSWORD_OK) -> CharSelected - порядок корректный
- Клиент НИ О РАЗУ не послал Ex 0xA6 (2nd password check) - подтверждено логами v2.2 и v2.4 (Ex-логирование активно)
- Эталон НИКОГДА не шлёт 2nd-password пакеты, когда клиент уже в мире; наш v2.3-фикс добавлял третью отправку отложенно +2 сек после EnterWorld

ДИАГНОЗ: модальное окно с клавиатурой (скриншот 2) открывается у клиента В МИРЕ; пока оно открыто, UI заблокирован - инвентарь не рендерится (клиент при этом ШЛЁТ RequestItemList 15+ раз - клик проходит, окно не открывается), диалоги НПС «наполовину нет», иконки бафов не отображаются/0 сек; ошибка 7680 появляется через ~6 сек после входа, отложенная отправка идёт на +2 сек - тайминги совпадают. Косвенное подтверждение из ранних репортов: «при переходе куда-то становится нормально» - телепорт реинициализирует UI мира и состояние лечится.

Фикс: удалена отложенная in-world отправка Ex2ndPasswordCheck (EnterWorld); pre-world ответы (после CharSelectionInfo в LoginServerThread:355 и перед CharSelected в CharacterSelect) сохранены - это штатная стадия протокола для данного пакета. Поток входа теперь полностью соответствует эталону.

Чистка Nursery: из custom NPC-статов удалены несуществующие в HF скиллы 14231/14232/14233/14765 (Creation's Rage, Yoke of Thorns, Blood Siphon Resistance) и Essence-предметы 36562/36563/17526/17527/30423/30425/34849/34850/40205/40210 - убраны warning'и NpcData/SkillData при старте (47 строк).

Инцидент: Edit-инструмент при правке EnterWorld сконвертировал табы всего файла в пробелы (дифф 1576 строк); файл восстановлен из HEAD~1, правка наложена python-скриптом с сохранением табов, дифф стал 11 строк. Урок: правки Java - только python со строгим newline=''.

Сборка: ядро 1945 файлов exit 0 (дважды: до и после восстановления табов). CI 40883515507 для 49a22c1b.

Ожидаемый тест v2.5: (1) вход в мир БЕЗ модального окна пароля и ошибки 7680; (2) инвентарь открывается; (3) диалоги НПС полные; (4) иконки бафов с таймерами; (5) если окно пароля всё ещё появляется при входе (до мира) - триггер чисто клиентский (локальный кэш аккаунта клиента), тогда проверить на другом аккаунте/персонаже и с чистым клиентом.

## Task 32 - v2.6: двойной ремап Ex-опкодов (системный баг протокола), аудит Dragon Valley

Дата: 29/10/2026. Баг-репорт после v2.5: «в Dragon Valley нет мобов; HF-мобы в принципе не показываются» (+ ранее: инвентарь не открывается, NPC «наполовину», бафы 0 сек).

ДИАГНОЗ (главная причина ВСЕХ трёх групп симптомов из v2.4/v2.5):
Пакет v2.3/v2.4/v2.5 отправлял Ex-пакеты клиенту 140 с ДВОЙНЫМ ремапом опкода:
(1) ServerPackets.writeId - в salvation-режиме писал Salvation-ID из таблицы оверайдов ServerPacketsSalvation (147 пакетов);
(2) GameClient.encrypt - затем прогонял уже записанный опкод через EX_SM_LOCAL_TO_SALVATION (remap локального HF-ID) ЕЩЁ РАЗ.
Итог: 127/147 Ex-пакетов приходили клиенту со сдвинутым ID (например ExQuestItemList 0xC7->0xC8 = ExGmViewQuestItemList; ExBuySellList 0xB8->0xB9 = ExCloseRaidSocket; ExBrExtraUserInfo 0xDB->0xDC; Pledge/duel/manor/variation/olympiad - весь блок). Клиент парсил их как ЧУЖИЕ пакеты - каскадные UI-сбои (инвентарь/NPC/бафы/почта/клан/магазин).

Фикс (единая точка ремапа):
- ServerPackets.writeId: убрана ветка оверайдов - ID теперь всегда локальный; Salvation-ремап ТОЛЬКО в GameClient.encrypt через таблицу
- SalvationProtocol.EX_SM_LOCAL_TO_SALVATION: 21 исправленная запись по эталону (вся airship-семья 0x60-0x9A, ExSendUiEvent 0x8E->0x8F - сохранён фикс 0x8F, ExNotifyBirthday 0x8F->0x90, ExGetBookmarkInfo 0x84->0x85, MPCC 0x4B/0x5B, PVP_MATCH 0x89-0x8B, PcCafe 0x44)
- ServerPacketsSalvation.java удалён
- Заглушены в salvation-режиме пакеты, отсутствующие в эталоне и парсируемые клиентом как чужие: ExSubPledgeSkillAdd (0x4B = NpcInfoAbnormalVisualEffect!) - Clan.java; ExNoticePostSent (0xB4 = ExChangePostState) - RequestSendPost.java. Остальные коллизии - мёртвый код (не инстанцируются) либо пакеты-тёзки (различие имён enum)
- CM-направление верифицировано полностью: 170/170 regular опкодов совпадают, Ex CM - 0 ложных обработчиков; UNMAPPED (220) - Salvation-only фичи, отсутствующие в HF (клиент шлёт - сервер игнорирует, не коррупция)

Верификация после фикса (скрипты в /home/z/my-project/scripts):
- verify_double_remap.py: 0 corrupted / 147
- resolve_collisions.py: 0 TABLE-FIX
- verify_opcodes.py / verify_cm_mapping.py / check_collisions.py - зелёные
- Побайтово подтверждены: ItemList/ExQuestItemList (sendType 1/2 + writeItem140 = эталонный writeItem), StatusUpdate, ShortBuffStatusUpdate, AbnormalStatusUpdate (sublevel+abnormalType+optional time), UserInfo (mask-блоки 1:1, UserInfoType enum идентичен), NpcInfo (salvation-ветка mask-based = эталон, NpcInfoType идентичен)

Dragon Valley (сервер-сайд проверен): спавны на месте - dist/game/data/spawns/Giran/DragonValley.xml (3 зоны giran06_cave_01..03: Cave Servant/Maiden/Keeper, gargoyles, Headless Knight, Blood Queen) + блочные спавны Others/24_21.xml и др. Невидимость мобов = следствие битых Ex-опкодов/NpcInfo-каскада, не спавнов.

Сборка: ядро 1944 файла exit 0 (после удаления ServerPacketsSalvation). GameServer.jar 2511 классов (1:1 с v2.5 минус удалённый класс). Zip v2.6: 26756 записей, на базе v2.5-пакета с заменённым GameServer.jar.

Ожидаемый тест v2.6: (1) инвентарь открывается; (2) НПС отображаются полностью, мобы видны (включая Dragon Valley); (3) иконки бафов с корректным временем; (4) магазины/клан/почта/олимпиадные окна работают; (5) модальное окно пароля в мире не появляется (фикс v2.5 в этой сборке тоже присутствует).
## v2.5 — полная диагностика трёх багов клиента 140 + порт спавнов/шаблонов Salvation 5.0

Диагностика (побайтовая сверка с эталоном L2J_Mobius_05.0_Salvation):
- NpcInfo/ServerObjectInfo/ItemList/ExQuestItemList/InventoryUpdate/ExStorageMaxCount/HennaInfo/MultiSellList/склады/GM-просмотры/почта — верифицированы байт-в-байт, БЕЗ изменений
- Базовые (не-EX) опкоды SM/CM между HF и Salvation — идентичны (дифф 505/763 пакетов), допущение адаптера подтверждено
- writeString (UTF-16LE+null), NpcInfoType (маски/длины), SalvationAveMap — идентичны/безопасны
- КОРЕНЬ БАГА 1 (мобы невидимы, Dragon Valley пуст): клиент 140 не имеет npcgrp/npcname-данных для СТАРЫХ HF-ID мобов перестроенных зон (DV 22537-22552 заменены в Essence на 23423+). Эталон спавнит мир ЗОНОВЫМ датапаком (165 файлов, 33893 записи) только с клиент-валидными ID; старые HF-тайлы у него в spawns/old (не грузятся)

Фикс БАГА 1 (порт датапака):
- spawns/ заменён на эталонный (минус old/): 165 файлов, 33789 конверсий respawnTime="Xsec|min|hour"->respawnDelay="N"; ai="DayNightSpawns" (231 файл) -> сглаживание групп dayTime/nightTime в periodOfDay="day|night" (нативный DayNightSpawnManager HF); spawnByDefault/ai атрибуты сняты; <list enabled="true"> добавлен (HF-парсер падал NPE - теперь null-safe)
- 5379 NPC-шаблонов долиты из эталона (10461->15840; 65 новых файлов 23000-25999/26000-26999/34000+; покрытие: 0 отсутствующих шаблонов для всех спавнов эталона)
- Кастомы сохранены: BeautyShop, Nursery, PlunderousPlains, DaimonsAltar, YetisTable, TreasureBoxes, Gremlins, UndergroundColiseum, FakePlayers
- XSD расширены до диалекта эталона: spawns.xsd - spawn контент перестроен на repeat-choice (AIData/territory/banned_territory/territories/parameters/npc/group в любом порядке), group+npc смешанные (parameters/minions/locations), npc attrs dbSave/dbName, x/y/z опциональны, territoryDef; npcs.xsd - mpReward, lucky (dropLists), abnormalResist (stats), critical/attackSpeed -> decimal, status/@isDeathPenalty
- respawnRandom "12hour" и пр. -> секунды (2701 конверсий); RaidbossSpawns territories/dbSave валидны; EilhalderVonHellmann locations -> первая точка
- Валидация lxml: 189/189 npc, 173/173 spawns XSD-валидны; известное ограничение: HarnakUndergroundRuins (107 территория-спавнов, инстанс-зона) скипается HF-загрузчиком (нет inline-territories)

Фикс БАГА 3 (бафы "0 сек" / пустой бар у HP/MP/CP) + совместимость:
- НОВЫЙ пакет ExAbnormalStatusUpdateFromTarget (ref EX 0xE6): id/level/sublevel/abnormalType(short)/optionalTime/effectorObjectId; шлётся в Player.setTarget при salvation-режиме (в HF этого пакета не было ВООБЩЕ - у клиента 140 бар бафов цели питается им)
- НОВЫЙ пакет NpcInfoAbnormalVisualEffect (0x4B): лёгкий AVE-апдейт NPC (objectId, transformation=0, AVE clientIds через SalvationAveMap); Npc.updateAbnormalEffect шлёт его в salvation-режиме вместо полного NpcInfo
- RelationChanged: маски SEND_ONE/SEND_MULTI исправлены 0x01/0x02 -> 0x02/0x04 (ref)
- ConfirmDlg: тип параметра byte -> int (ref; диалоги подтверждений читались неверно)
- AskJoinPledge: добавлено имя просителя (новый конструктор + вызов в RequestJoinPledge)
- ExListPartyMatchingWaitingRoom: переписан по ref (name/class/level/instanceId/instanceTimes; _members наполнялся только в legacy-ветке - salvation всегда слал пустой список)
- ExUserInfoCubic: реальный agathionId; SkillList subLevel/lastLearned - остаются 0 (в HF модели нет данных)
- AbnormalStatusUpdate: isAura() недоступен в HF-модели - остаётся isToggle()->-1; layout верифицирован как идентичный ref
- Player.sendItemList (salvation): +ExAdenaInvenCount/ExUserInfoInvenWeight через 800мс (ref sendItemList/updateAdenaAndWeight)

Фикс БАГА 2 (инвентарь не открывается):
- Все инвентарные пакеты сверлены с эталоном и совпадают (ItemList(1)/(2), ExQuestItemList, InventoryUpdate, item-запись cddcQcchQccddc 43 байта); EnterWorld-набор полный
- Вероятная причина у пользователя - каскад от невидимых мобов/битых пакетов выше; weight-block добавлен как ref-parity. Требуется свежий лог GS если не воспроизведётся

Сборка: ядро 1947 файлов exit 0 (javac 21.0.2, preview).
Релиз v2.5: см. ниже.

