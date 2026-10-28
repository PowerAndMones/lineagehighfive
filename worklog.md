
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
