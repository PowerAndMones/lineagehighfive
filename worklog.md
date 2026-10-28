
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
