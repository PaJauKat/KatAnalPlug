package com.example.vampires;

import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.Collections.query.PlayerQuery;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.*;
import com.example.PacketUtils.WidgetInfoExtended;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.TileItemPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Provides;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "<html>[<font color=\"#FA4444\">P</font>] Vampiras Sexys</html>",
        tags = {"pajau"},
        enabledByDefault = false
)
@Slf4j
public class VampiresPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private VampiresConfig config;

    @Inject
    private VampiresOverlay vampiresOverlay;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ConfigManager configManager;

    private boolean encendido = false;
    private State estado;
    private Random N = new Random();
    private int idleTicks = 0;
    private WorldPoint spotFino = null;

    private List<ETileItem> items2loot = new ArrayList<>();

    //private List<Pair<ETileItem, Integer>> itemsSaved = new ArrayList<>();
    private List<Object[]> itemsSaved = new ArrayList<>();
    private int randomWait = 0;
    private List<Integer> gearIDs = new ArrayList<>();
    private List<Widget> items2bank = new ArrayList<>();

    private List<Integer> itemUsed = new ArrayList<>();

    private int c = 0;

    private final List<Integer> vyreClothIDs = List.of(
            ItemID.VYRE_NOBLE_TOP,
            ItemID.VYRE_NOBLE_LEGS,
            ItemID.VYRE_NOBLE_SHOES
    );
    private final List<Integer> itemsUnbankeables = List.of(
            ItemID.DRAKANS_MEDALLION,
            ItemID.VYRE_NOBLE_LEGS,
            ItemID.VYRE_NOBLE_SHOES,
            ItemID.VYRE_NOBLE_TOP,

            ItemID.RUNE_POUCH,
            ItemID.RUNE_POUCH_L,
            ItemID.DIVINE_RUNE_POUCH,
            ItemID.DIVINE_RUNE_POUCH_L,
            ItemID.NATURE_RUNE,
            ItemID.FIRE_RUNE,
            ItemID.COINS_995

    );

    private final List<WorldPoint> tileBackUp = List.of(
            new WorldPoint(3598, 3361, 0),
            new WorldPoint(3597, 3357, 0),
            new WorldPoint(3605, 3362, 0)
    );

    private final WorldArea areaAltar = new WorldArea(3603, 3356, 5, 2, 0);
    private final WorldArea areaBank = new WorldArea(3603, 3365, 5, 3, 0);

    private final Set<Integer> alcheableItems = Set.of(
            ItemID.ADAMANT_PLATELEGS,
            ItemID.RUNE_DAGGER,
            ItemID.ADAMANT_PLATEBODY,
            ItemID.RUNE_FULL_HELM,
            ItemID.RUNE_KITESHIELD
    );
    private boolean checkInventory = true;
    private List<List<Object>> items2withdraw;
    private List<Integer> itemNeeded;
    private boolean reCheckItems = false;
    private int timeout = 0;
    private int potTimeout = 0;
    private int alchTimeout=0;


    @Provides
    VampiresConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(VampiresConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        reset();
        estado = State.OFF;
        encendido = false;
    }

    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
    }

    private enum State {
        OFF,
        RECHARGING_PRAY,
        AFKING,
        BANKING

    }

    void checkGear(List<Integer> IDs) {
        List<Widget> gearInInventory = Inventory.search().idInList(IDs).result();
        //log.info("size gear: {}",gearInInventory.size());
        if (gearInInventory.size() > 0) {
            log.info("equipando");
            for (Widget item : gearInInventory) {
                WidgetPackets.queueWidgetActionPacket(3, item.getId(), item.getItemId(), item.getIndex());
            }
        }
    }

    void checkBoost(Skill skill) {
        if (potTimeout > 0) {
            return;
        }
        Optional<Widget> pot = Optional.empty();
        if (skill == Skill.ATTACK) {
            pot = Inventory.search().matchesWildCardNoCase(config.atkBoost().getName()).first();
        } else if (skill == Skill.STRENGTH) {
            pot = Inventory.search().matchesWildCardNoCase(config.strBoost().getName()).first();
        }
        if (client.getBoostedSkillLevel(skill) >= client.getRealSkillLevel(skill) - 2
                && client.getBoostedSkillLevel(skill) < config.lvl2reboost()) {
            if (pot.isPresent()) {
                InventoryInteraction.useItem(pot.get(), "Drink");
                potTimeout = 2;
            } else {
                log.info("No se encontro boost potion");
            }
        }
    }

    @Subscribe
    void onGameTick(GameTick event) {

        if (potTimeout > 0) {
            potTimeout--;
        }

        if (alchTimeout > 0) {
            alchTimeout--;
        }

        if (timeout > 0) {
            timeout--;
            return;
        }

        Player player = client.getLocalPlayer();
        log.info("estado: {}",estado);


        if (estado == State.AFKING) {
            if (client.getBoostedSkillLevel(Skill.PRAYER) < config.rechargePP()) {
                estado = State.RECHARGING_PRAY;
                return;
            }
            if (client.getVarbitValue(172) == 1) {
                WidgetPackets.queueWidgetActionPacket(1, WidgetInfo.COMBAT_AUTO_RETALIATE.getPackedId(), -1, -1);
                return;
            }

            if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MELEE) == 0) {
                InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.PRAYER_PROTECT_FROM_MELEE.getPackedId());
            }
            if (client.getVarbitValue(Varbits.PRAYER_PIETY) == 0 && config.piety()) {
                InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.PRAYER_PIETY.getPackedId());
            }

            checkBoost(Skill.STRENGTH);
            checkBoost(Skill.ATTACK);

            //revisar lista items, si esta guardado, si lo esta se sube el tick, si no se agrega a la lista toAdd si es q no esta
            //se agrega toAdd , y se eliminan los items guardados que ya no esten.
            List<Object[]> toAdd = new ArrayList<>();

            List<ETileItem> items = TileItems.search().stackAboveXValue(config.minValLoot()).withinDistance(9).result();

            boolean found = false;
            for (int i = 0; i < items.size(); i++) {
                found=false;
                if (itemsSaved.size() > 0) {
                    for (int j = 0; j < itemsSaved.size(); j++) {
                        ETileItem item = (ETileItem) itemsSaved.get(j)[0];
                        if (items.get(i).tileItem.getId() == item.tileItem.getId()) {
                            log.info("woof");
                            itemsSaved.get(j)[1] = (int) itemsSaved.get(j)[1] + 1;
                            if ((int) itemsSaved.get(j)[1] >= randomWait) {
                                log.info("meow");
                                if (!items2loot.contains(item)) {
                                    log.info("added to items2loot");
                                    items2loot.add(item);
                                }
                            }
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    toAdd.add(new Object[]{items.get(i),0});
                }
            }

            itemsSaved.addAll(toAdd);
            itemsSaved.removeIf(x->!items.stream().map(ETileItem::getTileItem).collect(Collectors.toList()).contains( ((ETileItem) x[0]).tileItem ));
            items2loot.removeIf(x -> {
                randomWait = 3+N.nextInt(15);
                return !items.stream().map(ETileItem::getTileItem).collect(Collectors.toList()).contains(x.tileItem);
            });


            for (int i = 0; i < itemsSaved.size(); i++) {
                log.info("itemsSaved: item {}         ticks: {}",((ETileItem) itemsSaved.get(i)[0]).getTileItem().getId(),itemsSaved.get(i)[1]);
            }
            for (int i = 0; i < items2loot.size(); i++) {
                log.info("items2loot: item {}", items2loot.get(i).getTileItem().getId());
            }
            log.info("----------------");



            //Optional<ETileItem> itemsStack2loot = TileItems.search().stackAboveXValue(config.minValLoot()).first();
            //Optional<ETileItem> items2loot = TileItems.search().eachItemAboveXValue(config.minValLoot()).withinDistance(9).first();
            Optional<Widget> alchItem = Optional.empty();
            if (config.alch()) {
                alchItem = Inventory.search().withSet(alcheableItems).first();
            }
            Optional<ETileItem> bloodShard = TileItems.search().eachItemAboveXValue(1000000).first();



            Widget alch = client.getWidget(218, 40);
            if (alchItem.isPresent() && alch != null && alch.getSpriteId() == 41 && alchTimeout == 0) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetOnWidget(alch, alchItem.get());
                alchTimeout = 3 + N.nextInt(5);
            } else if (isIdle(player)) {
                if (bloodShard.isPresent()) {
                    MousePackets.queueClickPacket();
                    TileItemPackets.queueTileItemAction(bloodShard.get(), false);
                } else if (Inventory.getEmptySlots() <= 2) {
                    checkGear(vyreClothIDs);
                    if (NPCs.search().interactingWithLocal().empty()) {
                        estado = State.BANKING;
                    }

                } else if (items2loot.size() > 0) {
                    MousePackets.queueClickPacket();
                    TileItemPackets.queueTileItemAction(items2loot.get(0), false);
                    log.info("recogiendo: {}", items2loot.get(0).getTileItem().getId());
                } else if (player.getInteracting() == null) {
                    checkGear(gearIDs);
                    idleTicks++;
                    if (idleTicks > 30) {
                        if (spotFino != null && !player.getWorldLocation().equals(spotFino)) {
                            if (EthanApiPlugin.canPathToTile(spotFino).isReachable()) {
                                MousePackets.queueClickPacket();
                                MovementPackets.queueMovement(spotFino);
                            }
                        }
                    }
                } else {
                    idleTicks = 0;
                }
            }
        } else if (estado == State.RECHARGING_PRAY) {
            if (client.getBoostedSkillLevel(Skill.PRAYER) < config.rechargePP()) {
                if (isIdle(player)) {
                    Optional<TileObject> prayDoor = TileObjects.search().withId(39406).atLocation(new WorldPoint(3605,3358,0)).first();
                    if (prayDoor.isPresent()) {
                        TileObjectInteraction.interact(prayDoor.get(), "Open");
                    } else {
                        Optional<TileObject> altar = TileObjects.search().withId(39234).first();
                        if (altar.isPresent()) {
                            TileObjectInteraction.interact(altar.get(), "Pray-at");
                        }
                    }
                }
            } else {
                if (player.getWorldLocation().isInArea(areaAltar)) {
                    if (isIdle(player)) {
                        Optional<TileObject> prayDoor = TileObjects.search().withId(39406).atLocation(new WorldPoint(3605,3358,0)).first();
                        if (prayDoor.isPresent()) {
                            TileObjectInteraction.interact(prayDoor.get(), "Open");
                        } else {
                            if (spotFino != null) {
                                MousePackets.queueClickPacket();
                                MovementPackets.queueMovement(spotFino);
                            } else {
                                MousePackets.queueClickPacket();
                                MovementPackets.queueMovement(tileBackUp.get(N.nextInt(tileBackUp.size())));
                            }

                        }
                    }
                } else {
                    estado = State.AFKING;
                }

            }
        } else if (estado == State.BANKING) {
            if (Bank.isOpen()) {

                if (checkInventory) {
                    checkInventory = false;
                    itemUsed.addAll(itemsUnbankeables);
                    itemUsed.addAll(gearIDs);
                    itemUsed.add(ItemID.SUPER_STRENGTH4);
                    itemUsed.add(ItemID.SUPER_ATTACK4);
                    items2bank = BankInventory.search().filter(x -> !itemUsed.contains(x.getItemId())).filterUnique().result();
                    items2withdraw = checkSupplies();
                }
                if (items2bank.size() > 0) {
                    int m = Math.min(1 + N.nextInt(3), items2bank.size() - c);
                    for (int i = c; i < c + m; i++) {
                        BankInventoryInteraction.useItem(items2bank.get(i), "Deposit-All");
                    }
                    c += m;
                    if (c == items2bank.size()) {
                        checkInventory = true;
                        c = 0;
                    }
                    return;
                }

                if (items2withdraw.size() > 0) {
                    Optional<Widget> item2withdraw = Bank.search().withId((int) items2withdraw.get(c).get(0)).first();
                    if (item2withdraw.isPresent()) {
                        BankInteraction.withdrawX(item2withdraw.get(), (int)items2withdraw.get(c).get(1));
                    } else {
                        log.info("{} not found",items2withdraw.get(c).get(2));
                        estado = State.AFKING;
                    }
                    timeout =3+ N.nextInt(5);
                    c++;
                    if (c == items2withdraw.size()) {
                        checkInventory = true;
                        c = 0;
                    }
                    return;
                }
                client.runScript(138);
                client.runScript(29);



            } else if (isIdle(player)) {

                if (Inventory.getEmptySlots() <= 2) {
                    Optional<TileObject> bankDoor = TileObjects.search().withId(39406).atLocation(new WorldPoint(3605, 3365, 0)).first();
                    if (bankDoor.isPresent()) {
                        TileObjectInteraction.interact(bankDoor.get(), "Open");
                    } else {
                        List<TileObject> bank = TileObjects.search().withId(39238).result();
                        if (bank.size() > 0) {
                            TileObjectInteraction.interact(bank.get(N.nextInt(bank.size())), "Bank");
                        }
                    }

                } else {
                    if (player.getWorldLocation().isInArea(areaBank)) {
                        Optional<TileObject> bankDoor = TileObjects.search().withId(39406).atLocation(new WorldPoint(3605, 3365, 0)).first();
                        if (bankDoor.isPresent()) {
                            TileObjectInteraction.interact(bankDoor.get(), "Open");
                        } else {
                            if (spotFino != null) {
                                MousePackets.queueClickPacket();
                                MovementPackets.queueMovement(spotFino);
                            } else {
                                MousePackets.queueClickPacket();
                                MovementPackets.queueMovement(tileBackUp.get(N.nextInt(tileBackUp.size())));
                            }
                        }
                    } else {
                        estado = State.AFKING;
                        checkInventory = true;
                    }


                }



            }
        }
    }

    private List<List<Object>> checkSupplies() {
        List<List<Object>> suppliesNeeded = new ArrayList<>();
        if (config.atkBoost() != VampiresConfig.AtkBoost.NONE) {
            suppliesNeeded.add(List.of(config.atkBoost().getId(),config.atkPotAmount() ,config.atkBoost().name()));
        }

        if (config.strBoost() != VampiresConfig.StrBoost.NONE) {
            suppliesNeeded.add(List.of(config.strBoost().getId(), config.strPotAmount(), config.strBoost().name()));
        }

        List<List<Object>> suppFaltante = new ArrayList<>();

        for (int i = 0; i < suppliesNeeded.size(); i++) {
            List<Widget> supp  = BankInventory.search().withId((int) suppliesNeeded.get(i).get(0)).result();
            int qtyFaltante = (int)suppliesNeeded.get(i).get(1) - supp.size();
            if (qtyFaltante > 0) {
                suppFaltante.add(List.of(suppliesNeeded.get(i).get(0), qtyFaltante , suppliesNeeded.get(i).get(2)));
            }

        }
        return suppFaltante;

    }


    @SneakyThrows
    @Subscribe
    void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("vampiresKat")) {
            reCheckItems = true;
            if (event.getKey().equals("onOff")) {
                encendido = !encendido;
                clientThread.invoke(() -> {
                    if (encendido) {
                        if (checkGearIDs()) {
                            estado = State.AFKING;
                            reset();
                            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Prendido", Color.green), "");
                        } else {
                            estado = State.OFF;
                            encendido = false;
                        }
                    } else {
                        estado = State.OFF;
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Apagado", Color.red), "");
                    }
                });
            } else if (event.getKey().equals("fetchGear")) {
                List<EquipmentItemWidget> gear = Equipment.search().result();
                gearIDs = gear.stream().map(EquipmentItemWidget::getEquipmentItemId).collect(Collectors.toList());
                StringBuilder textId = new StringBuilder();
                for (Integer a : gearIDs) {
                    textId.append(a);
                    textId.append(",");
                }
                if (gearIDs.size() > 0) {
                    textId.deleteCharAt(textId.length() - 1);
                }
                configManager.setConfiguration("vampiresKat","gear",textId.toString());
                clientThread.invoke(() -> {
                    configManager.sendConfig();
                    configManager.load();
                });
            }
        } 
    }

    private boolean checkGearIDs() {
        if (config.gear().length() > 0) {
            gearIDs = Arrays.stream(config.gear().split(",")).map(Integer::parseInt).collect(Collectors.toList());
        } else {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Gear not found", Color.red), "");
        }

        if (gearIDs.size() == 0) {
            return false;
        } else {
            return true;
        }
    }


    @Subscribe
    void onMenuEntryAdded(MenuEntryAdded event) {
        if (client.getSelectedSceneTile() == null) return;
        if (client.isKeyPressed(KeyCode.KC_SHIFT) && event.getOption().contains("Walk here")) {
            client.createMenuEntry(-1).setOption(ColorUtil.wrapWithColorTag("Select spot", new Color(0xFF95FF)))
                    .setType(MenuAction.RUNELITE)
                    .setTarget(event.getTarget())
                    .onClick(x -> {
                        spotFino = client.getSelectedSceneTile().getWorldLocation();
                    });
        }
    }


    private void reset() {
    }

    private boolean isIdle(Player player) {
        return player.getPoseAnimation() == player.getIdlePoseAnimation();
    }

    private void checkEquipment() {

    }

    private void checkInventory() {

    }

}
