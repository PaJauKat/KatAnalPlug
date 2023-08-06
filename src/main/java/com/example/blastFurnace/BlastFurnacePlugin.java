package com.example.blastFurnace;

import com.example.EthanApiPlugin.Collections.*;
import com.example.InteractionApi.*;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "<html>[<font color=\"#FA4444\">P</font>] Blast furnace</html>",
        tags = {"pajau"},
        enabledByDefault = false
)
@Slf4j
public class BlastFurnacePlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private BlastFurnaceConfig config;
    private boolean coalBagDeposit = false;
    private boolean waiting = false;
    private boolean toDispenser = false;
    private boolean glovesClicked = false;
    private boolean foremanPaid = false;

    @Provides
    BlastFurnaceConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(BlastFurnaceConfig.class);
    }

    private enum Estados{
        MAKING_BARS,
        RECHARGING_COFFER,
        PAYING_FOREMAN,
        APAGADO,
        STARTING
    }

    private boolean enAccion = false;
    private int timeout = 0;
    private Estados estado = Estados.STARTING;
    private GameObject conveyorBelt;
    private GameObject barDispenser;
    private int nDeposit = 0;
    private boolean recojer = false;
    private final WorldPoint BANK_POSITION = new WorldPoint(1948,4956,0);

    void reset(){
        recojer = false;
        coalAmount = -1;
        nDeposit = 0;
        toDispenser = false;
        foremanPaid = false;
    }

    private final List<Integer> ORES = Arrays.asList(
            ItemID.COAL,
            ItemID.IRON_ORE,
            ItemID.SILVER_ORE,
            ItemID.GOLD_ORE,
            ItemID.MITHRIL_ORE,
            ItemID.ADAMANTITE_ORE,
            ItemID.RUNITE_ORE
    );

    private final List<Integer> BARS_IDS = Arrays.asList(
            ItemID.IRON_BAR,
            ItemID.STEEL_BAR,
            ItemID.MITHRIL_BAR,
            ItemID.ADAMANTITE_BAR,
            ItemID.RUNITE_BAR,
            ItemID.GOLD_BAR,
            ItemID.SILVER_BAR,
            ItemID.BRONZE_BAR
    );

    private final List<Integer> BF_BARS_IDS = List.of(
            Varbits.BLAST_FURNACE_ADAMANTITE_BAR,
            Varbits.BLAST_FURNACE_STEEL_BAR,
            Varbits.BLAST_FURNACE_GOLD_BAR,
            Varbits.BLAST_FURNACE_IRON_BAR,
            Varbits.BLAST_FURNACE_MITHRIL_BAR,
            Varbits.BLAST_FURNACE_RUNITE_BAR,
            Varbits.BLAST_FURNACE_SILVER_BAR
    );

    private final List<Integer> STAMINAS_IDS = List.of(
            ItemID.STAMINA_POTION1,
            ItemID.STAMINA_POTION2,
            ItemID.STAMINA_POTION3,
            ItemID.STAMINA_POTION4
    );

    private final List<Integer> itemsRelevant = List.of(
            ItemID.STAMINA_POTION1,
            ItemID.STAMINA_POTION2,
            ItemID.STAMINA_POTION3,
            ItemID.STAMINA_POTION4,

            ItemID.IRON_BAR,
            ItemID.STEEL_BAR,
            ItemID.MITHRIL_BAR,
            ItemID.ADAMANTITE_BAR,
            ItemID.RUNITE_BAR,
            ItemID.GOLD_BAR,
            ItemID.SILVER_BAR,
            ItemID.BRONZE_BAR,

            ItemID.COAL,
            ItemID.IRON_ORE,
            ItemID.SILVER_ORE,
            ItemID.GOLD_ORE,
            ItemID.MITHRIL_ORE,
            ItemID.ADAMANTITE_ORE,
            ItemID.RUNITE_ORE,

            ItemID.GOLDSMITH_GAUNTLETS,
            ItemID.ICE_GLOVES,
            ItemID.COAL_BAG_12019,
            ItemID.COAL_BAG_25627,
            ItemID.OPEN_COAL_BAG
    );

    private final String DEPOSIT_ORE_TEXT = "All your ore goes onto the conveyor belt";
    private final String COAL_BAG_TEXT = "The coal bag contains";
    private final String EMPTY_COAL_BAG = "The coal bag is empty";
    private int coalAmount = -1;
    private final WorldPoint TILE_CONVEYOR = new WorldPoint(1942,4967,0);
    private final List<Integer> COAL_BAG_IDS = List.of(12019,24480);
    private final int EMPTY_SLOT_ID = 6512;

    private final List<Integer> itemsIndepositables = List.of(ItemID.ICE_GLOVES,ItemID.GOLDSMITH_GAUNTLETS,ItemID.COAL_BAG_25627, ItemID.OPEN_COAL_BAG, ItemID.COAL_BAG_12019);



    //0 - poner weas
    //1 - cargando
    //2 - se puede recojer con gloves
    //3 - se puede recojer sin gloves




    @Override
    protected void startUp() throws Exception {
        reset();
        estado = Estados.APAGADO;
        enAccion = false;
    }

    private void withdrawStamina() {
        List<Widget> stam = Bank.search().idInList(STAMINAS_IDS).result();
        stam.sort(Comparator.comparingInt(Widget::getItemId));
        Collections.reverse(stam);
        if (!stam.isEmpty()) {
            MousePackets.queueClickPacket();
            BankInteraction.useItem(stam.get(0), "Withdraw-1");
        }
    }

    private boolean isMoving() {
        return client.getLocalPlayer().getPoseAnimation()!=client.getLocalPlayer().getIdlePoseAnimation();
    }

    @Subscribe
    void onGameTick(GameTick event) {
        if (!enAccion) {
            return;
        }
        if (timeout > 0) {
            timeout--;
            return;
        }
        Player player = client.getLocalPlayer();


        if (estado == Estados.MAKING_BARS) {
            if (client.getVarbitValue(Varbits.BLAST_FURNACE_COFFER) < 1000) {
                estado = Estados.RECHARGING_COFFER;
                return;
            }

            if (client.getWidget(WidgetInfo.LEVEL_UP) != null) {
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(barDispenser.getWorldLocation().dy(-1));
                timeout = 1;
            }


            if (!isMoving()) {
                if (BF_BARS_IDS.stream().anyMatch(id -> client.getVarbitValue(id) > 0)) {
                    int barInDispenser = 0; //cantidad de barras en el dispenser
                    log.info("meow");
                    for (int b : BF_BARS_IDS) {
                        if (client.getVarbitValue(b) > 0) {
                            barInDispenser = client.getVarbitValue(b);
                            break;
                        }
                    }

                    Widget takeShit = client.getWidget(270, 0);//Chat meseage
                    if (takeShit != null) {
                        Widget allButton = client.getWidget(270, 12);   //all button
                        if (allButton != null) {
                            if (allButton.getActions() != null) {   //all button not selected
                                WidgetPackets.queueWidgetAction(allButton, "All");
                            } else {
                                Widget takeButton = client.getWidget(270, 14);
                                if (takeButton != null) {
                                    log.info("tomando barritas");
                                    MousePackets.queueClickPacket();
                                    WidgetPackets.queueResumePause(17694734, barInDispenser);

                                    Optional<TileObject> bank = TileObjects.search().atLocation(BANK_POSITION).first();
                                    bank.ifPresent(tileObject -> TileObjectInteraction.interact(tileObject, "Use"));
                                    toDispenser = false;
                                    timeout = 2;
                                }
                            }
                        }
                    } else {
                        if (!glovesClicked) {
                            Optional<Widget> iceGloves = Inventory.search().withId(ItemID.ICE_GLOVES).first();
                            if (iceGloves.isPresent()) {
                                glovesClicked = true;
                                InventoryInteraction.useItem(iceGloves.get(), "Wear");
                                return;
                            }
                        }
                        glovesClicked = false;

                        log.info("taking bars {}", barDispenser.getId());
                        TileObjectInteraction.interact(barDispenser, "Take");
                        timeout = 1;
                    }
                }


                else if (Bank.isOpen()) {
                    Optional<Widget> bar2deposit = BankInventory.search().idInList(BARS_IDS).first();
                    if (bar2deposit.isPresent()) {
                        BankInventoryInteraction.useItem(bar2deposit.get(), "Deposit-All");
                    }
                    List<Widget> itemsNaqVer = BankInventory.search().filter(x->!itemsRelevant.contains(x.getItemId())).filterUnique().result();
                    itemsNaqVer.forEach(x->BankInventoryInteraction.useItem(x,"Deposit-All"));


                    if (config.staminas()) {
                        Optional<Widget> stam = BankInventory.search().idInList(STAMINAS_IDS).first();
                        if (client.getEnergy() < 40 * 100 && client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0) {
                            if (BankInventory.search().idInList(STAMINAS_IDS).result().isEmpty()) {
                                if (Inventory.full()) {
                                    Optional<Widget> item2makeSpace = BankInventory.search().filter(x->!itemsIndepositables.contains(x.getItemId())).first();
                                    item2makeSpace.ifPresent(x->BankInventoryInteraction.useItem(x,"Deposit-All"));
                                    timeout = 1;
                                }else {
                                    withdrawStamina();
                                    timeout = 1;
                                }
                                return;
                            } else {
                                if (stam.isPresent()) {
                                    BankInventoryInteraction.useItem(stam.get(), "Drink");
                                    return;
                                }
                            }
                        } else if (client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) > 0) {
                            stam.ifPresent(x->BankInventoryInteraction.useItem(x,"Deposit-1"));
                        }
                    }

                    Optional<Widget> bolsa = BankInventory.search().withSet(new HashSet<>(COAL_BAG_IDS)).first();
                    if (bolsa.isPresent() && bolsa.get().getActions() != null) {
                        if (Arrays.stream(bolsa.get().getActions()).anyMatch(x ->x != null && x.contains("Fill"))) {
                            BankInventoryInteraction.useItem(bolsa.get(), "Fill");
                            coalAmount = 0;
                        } else if (Arrays.stream(bolsa.get().getActions()).anyMatch(x ->x != null && x.contains("Empty"))) {
                            coalAmount = 27;
                            nDeposit = 1;
                        }
                    }

                    if (client.getVarbitValue(Varbits.BLAST_FURNACE_COAL) < 80 && config.barChoosen().getSecondary() == ItemID.COAL) {
                        //sacar coal
                        Optional<Widget> coal = Bank.search().withId(ItemID.COAL).first();
                        if (coal.isPresent()) {
                            BankInventoryInteraction.useItem(coal.get(), "Withdraw-All");
                        } else {
                            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Coal not Found", Color.ORANGE), "");
                            enAccion = false;
                            return;
                        }//sino sacar primarios
                    } else {
                        Optional<Widget> primario = Bank.search().withId(config.barChoosen().getPrimary()).first();
                        if (primario.isPresent()) {
                            BankInventoryInteraction.useItem(primario.get(), "Withdraw-All");
                            toDispenser = true;
                        } else {
                            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Primary not Found", Color.ORANGE), "");
                            enAccion = false;
                            return;
                        }
                    }

                    TileObjectInteraction.interact(conveyorBelt, "Put-ore-on");
                    timeout=2;




                } else {
                    //List<Widget> oreInInventory = Inventory.search().idInList(Arrays.stream(BarOres.values()).map(BarOres::getPrimary).collect(Collectors.toList())).result();
                    List<Widget> oreInInventory = Inventory.search().idInList(ORES).result();
                    if (oreInInventory.size() > 0) {

                        if (Inventory.search().idInList(Arrays.stream(BarOres.values()).map(BarOres::getPrimary).collect(Collectors.toList())).empty()) {
                            toDispenser = false;
                        } else {
                            toDispenser = true;
                        }
                        TileObjectInteraction.interact(conveyorBelt, "Put-ore-on");
                        timeout = 2;

                    } else if (client.getLocalPlayer().getWorldLocation().equals(TILE_CONVEYOR)) {


                        Optional<Widget> coalBag = Inventory.search().idInList(COAL_BAG_IDS).first();
                        if (coalBag.isPresent() && coalAmount > 0) {
                            if (!waiting) {
                                timeout = 1;
                                waiting = true;
                                return;
                            }
                            waiting = false;
                            log.info("limpiando coal bag");
                            InventoryInteraction.useItem(coalBag.get(), "Empty");
                            TileObjectInteraction.interact(conveyorBelt, "Put-ore-on");
                            coalAmount = 0;
                            timeout = 1;
                        } else {
                            if (toDispenser) {
                                Optional<Widget> goldGloves = Inventory.search().withId(ItemID.GOLDSMITH_GAUNTLETS).first();
                                goldGloves.ifPresent(x->InventoryInteraction.useItem(x,"Wear"));
                                MousePackets.queueClickPacket();
                                MovementPackets.queueMovement(barDispenser.getWorldLocation().dy(-1));
                            } else {
                                Optional<TileObject> bank = TileObjects.search().atLocation(BANK_POSITION).first();
                                bank.ifPresent(tileObject -> TileObjectInteraction.interact(tileObject, "Use"));
                            }
                        }
                    } else if (toDispenser) {
                        if (!player.getWorldLocation().equals(barDispenser.getWorldLocation().dy(-1))) {
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(barDispenser.getWorldLocation().dy(-1));
                        }
                    } else {
                        openBank();
                    }

                }
            }
        } else if (estado == Estados.PAYING_FOREMAN) {
            if (!isMoving()) {
                if (foremanPaid) {
                    if (client.getVarbitValue(Varbits.BLAST_FURNACE_COFFER) < 5000) {
                        estado = Estados.RECHARGING_COFFER;
                        return;
                    }

                    if (Bank.isOpen()) {
                        //depositar coins
                        Optional<Widget> coins2Depostit = BankInventory.search().withId(ItemID.COINS_995).first();
                        coins2Depostit.ifPresent(x -> BankInventoryInteraction.useItem(x, "Deposit-All"));
                        estado = Estados.MAKING_BARS;
                    } else {
                        openBank();
                    }
                }else{
                    if (client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS)!=null) {
                        MousePackets.queueClickPacket();
                        //WidgetPackets.queueWidgetAction(client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS).getChild(1),"Continue"); //action Continue

                        WidgetPackets.queueResumePause(client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS).getId(),1);
                        //WidgetPackets.queueWidgetActionPacket();
                        timeout = 1;
                        foremanPaid = true;
                        return;
                    }


                    Optional<NPC> foreman = NPCs.search().withId(NpcID.BLAST_FURNACE_FOREMAN).first();
                    Optional<Widget> coins = Inventory.search().withId(ItemID.COINS_995).first();
                    if (coins.isPresent()) {
                        if (coins.get().getItemQuantity() < 2500) {
                            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Not enough money, you poor guy", Color.red), "");
                            estado = Estados.APAGADO;
                        }
                        if (foreman.isPresent()) {
                            NPCInteraction.interact(foreman.get(), "Pay");
                        }
                    } else {
                        if (Bank.isOpen()) {
                            if (Inventory.full()) {
                                Optional<Widget> item2Deposit = BankInventory.search().filter(x -> !itemsIndepositables.contains(x.getItemId())).first();
                                if (item2Deposit.isPresent()) {
                                    BankInventoryInteraction.useItem(item2Deposit.get(), "Deposit-All");
                                }
                            } else {
                                Optional<Widget> coinsInBank = Bank.search().withId(ItemID.COINS_995).first();
                                if (coinsInBank.isPresent()) {
                                    BankInteraction.useItem(coinsInBank.get(), "Withdraw-All");
                                } else {
                                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Coins not found", Color.red), "");
                                }
                            }
                        } else {
                            openBank();
                        }

                    }
                }



            }


        } else if (estado == Estados.RECHARGING_COFFER) {
            if (client.getVarbitValue(Varbits.BLAST_FURNACE_COFFER) < 5000) {

                Optional<Widget> coinsInInv = Inventory.search().withId(ItemID.COINS_995).first();
                if (coinsInInv.isPresent()) {

                    if (client.getWidget(WidgetInfo.CHATBOX_TITLE) != null
                            && client.getWidget(WidgetInfo.CHATBOX_TITLE).getText() != null
                            && client.getWidget(WidgetInfo.CHATBOX_TITLE).getText().contains("Deposit how much?"))
                    {
                        log.info("Depositando cash");
                        //WidgetPackets.queueResumePause(client.getWidget(WidgetInfo.CHATBOX_FULL_INPUT).getId(), 10000);
                        client.setVarcStrValue(VarClientStr.INPUT_TEXT,"100000");
                        client.runScript(681);

                        timeout = 1;
                        return;
                    }


                    if (client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS) != null) {//widget of money coffer
                        WidgetPackets.queueResumePause(client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS).getId(), 1);
                        //deposit cash
                    } else {
                        //open widget od coffer
                        Optional<TileObject> coffer = TileObjects.search().withId(29330).first();
                        if (coffer.isPresent()) {
                            TileObjectInteraction.interact(coffer.get(), "Use");
                        } else {
                            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Coffer not found", Color.red), "");
                        }
                    }
                } else {
                    if (Bank.isOpen()) {
                        Optional<Widget> coinsInBank = Bank.search().withId(ItemID.COINS_995).first();
                        if (coinsInBank.isPresent()) {
                            BankInteraction.useItem(coinsInBank.get(), "Withdraw-All");
                        } else {
                            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Coins not found", Color.red), "");
                        }
                    } else {
                        openBank();
                    }
                }


            } else {
                estado = Estados.MAKING_BARS;
            }

        }


    }

    void openBank() {
        Optional<TileObject> bankChest = TileObjects.search().withId(26707).first();
        if (bankChest.isPresent()) {
            TileObjectInteraction.interact(bankChest.get(), "Use");
        } else {
            log.info("Bank chest not found");
        }
    }

    @Subscribe
    void onChatMessage(ChatMessage event) {
        if (event.getMessage().contains(COAL_BAG_TEXT)) {
            log.info("tiene 27");
            coalAmount = 27;
        }else if (event.getMessage().contains(EMPTY_COAL_BAG)){
            log.info("tiene 0");
            coalAmount = 0;
        } else if (event.getMessage().contains("You must ask the foreman's permission before using the blast furnace.")) {
            estado = Estados.PAYING_FOREMAN;
            toDispenser = false;
        }
    }

    @Subscribe
    void onGameObjectSpawned(GameObjectSpawned event) {
        switch (event.getGameObject().getId()) {
            case ObjectID.CONVEYOR_BELT:
                conveyorBelt = event.getGameObject();
                break;
            case NullObjectID.NULL_9092:
                barDispenser = event.getGameObject();
                break;
        }
    }

    @Subscribe
    void onGameObjectDespawned(GameObjectDespawned event) {
        switch (event.getGameObject().getId()) {
            case ObjectID.CONVEYOR_BELT:
                conveyorBelt = null;
                break;
            case NullObjectID.NULL_9092:
                barDispenser = null;
                break;
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOADING)
        {
            conveyorBelt = null;
            barDispenser = null;
        }
    }

    @Subscribe
    void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("bfurnace") ) {
            if (event.getKey().equals("onOff") ) {
                enAccion=!enAccion;
                clientThread.invoke(() -> {
                    if (!enAccion) {
                        estado= Estados.APAGADO;
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Apagado", Color.red), "");
                    } else {
                        estado= Estados.MAKING_BARS;
                        reset();
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Prendido", Color.green), "");
                    }
                });
            }
        }
    }


}
