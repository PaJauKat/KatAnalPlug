package com.example.blackjacking;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.WidgetPackets;
import com.example.PajauApi.PajauApiPlugin;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.WorldService;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

@Slf4j
@PluginDescriptor(
        name = "<html>[<font color=\"#FA4444\">P</font>] Blackjack</html>",
        description = "KO and pickpocket Thugs/Bandits. Use jug of wine",
        tags = {"pajau"}
)
public class Blackjacking extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private KeyManager keyManager;

    @Inject
    private BlackjackingConfig config;

    @Inject
    private WorldService worldService;

    @Provides
    BlackjackingConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(BlackjackingConfig.class);
    }

    private int timeout = 0;
    private boolean prendido = false;
    private int counter = 0;
    private final WorldArea thugSpot1 = new WorldArea(3340, 2953, 5, 4, 0);

    private final int DOOR_CLOSED_ID = 1533;
    private final int DOOR_OPEN_ID = 1534;

    private State estado;
    private List<NPC> victima = new ArrayList<>();
    private final String KO_MESSAGE = "You smack the bandit over the head and render them unconscious.";

    private int knocked = 0;
    private List<Widget> remain = new ArrayList<>();

    private int dropIndex = 0;

    private enum State {ROBANDO, TO_SHOP, TO_SPOT ,RESTOCK, STARTING, APAGADO}

    private final KeyListener botonEncendido = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F10, 0)) {
        @Override
        public void hotkeyPressed() {
            clientThread.invoke(() -> {
                prendido = !prendido;
                counter = 0;
                if (prendido) {
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("ON", Color.GREEN), "");
                } else
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("OFF", Color.RED), "");
            });
        }
    };

    @Override
    protected void startUp() throws Exception {
        keyManager.registerKeyListener(botonEncendido);
    }

    @Override
    protected void shutDown() throws Exception {
        keyManager.unregisterKeyListener(botonEncendido);
    }

    @Subscribe
    void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("blackjackANAL") ) {
            if (event.getKey().equals("onOff") ) {
                prendido=!prendido;
                clientThread.invoke(() -> {
                    if (!prendido) {
                        estado = State.APAGADO;
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Apagado", Color.red), "");
                    } else {
                        reset();
                        estado = State.ROBANDO;
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Prendido", Color.green), "");
                    }
                });
            }
        }
    }

    @Subscribe
    void onChatMessage(ChatMessage event) {
        if (event.getMessage().equals(KO_MESSAGE)) {
            log.info("meow");
            knocked = 4;
            counter=2;
        }
    }

    void reset() {
        mundoCasa = -1;
        timeout=0;
        dropIndex = 0;
        victima = new ArrayList<>();
    }



    @Subscribe
    void onGameTick(GameTick event) {
        if (!prendido) return;


        if (timeout > 0) {
            timeout--;
            return;
        }
        if (knocked > 0) {
            knocked--;
        }




        if (mundoCasa == -1) {
            mundoCasa=client.getWorld();
        }



        if (estado == State.ROBANDO) {
            if (Inventory.search().withId(config.foodID()).empty()) {
                if (Inventory.getItemAmount(config.idRemain()) > 0) {
                    if (remain.isEmpty()) {
                        remain = Inventory.search().withId(config.idRemain()).result();
                    }
                    int woof = PajauApiPlugin.nRand.nextInt(6) + 2 + dropIndex;
                    for (int i = dropIndex; i < woof; i++) {
                        if (i < remain.size()) {
                            dropIndex = i+1;
                            InventoryInteraction.useItem(remain.get(i), "Drop");
                        } else {
                            break;
                        }
                    }
                } else {
                    remain = new ArrayList<>();
                    dropIndex = 0;
                    log.info("ROBANDO -> TO_SHOP");
                    estado = State.TO_SHOP;
                }

            }else{
                victima = NPCs.search().withName(config.spot().getName()).withinWorldArea(config.spot().getPlace()).result();
                if (victima.size()==1 && victima.get(0).getWorldLocation().isInArea(config.spot().getPlace())) {
                    if (counter > 0) {
                        if (victima.get(0).getAnimation() == 838 || knocked > 0) {
                            if (counter == 2 && client.getBoostedSkillLevel(Skill.HITPOINTS) < 50) {
                                log.info("Comiendo");
                                InventoryInteraction.useItem(config.foodID(), "Drink");
                            } else {
                                log.info("robando");
                                NPCInteraction.interact(victima.get(0), "Pickpocket");
                            }
                            counter--;
                        } else {
                            log.info("KO denuevo");
                            NPCInteraction.interact(victima.get(0), "Knock-Out");
                            counter = 2;
                        }
                    } else {
                        log.info("KO");
                        NPCInteraction.interact(victima.get(0), "Knock-Out");
                        counter = 2;
                    }
                    timeout = 1;
                } else {
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE,"","another victim is in area or there is no victim","");
                    timeout = 10;
                }
            }

        }



        else if (estado == State.TO_SHOP) {
            List<TileObject> door = TileObjects.search().atLocation(config.spot().getDoor()).result();
            if (client.getLocalPlayer().getWorldLocation().isInArea(config.spot().getPlace())) {

                for (TileObject o : door) {
                    if (o.getId() == 1533) {
                        TileObjectInteraction.interact(o, "Open");
                        log.info("abriendo la puerta");
                        timeout = 1;
                        return;
                    }
                }
                log.info("Moviendo a fuera de la puerta");
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(config.spot().getPath2bank()[0]);
            } else if (client.getLocalPlayer().getWorldLocation().equals(config.spot().getDoor()) && door.stream().anyMatch(x->x.getId()==DOOR_OPEN_ID)) {
                TileObjects.search().atLocation(config.spot().getDoor()).withId(DOOR_OPEN_ID).first().ifPresent(x -> {
                    TileObjectInteraction.interact(x,"Close");
                });
            } else {
                if (!PajauApiPlugin.caminando(client, config.spot().getPath2bank(), 2, true)) {
                    estado = State.RESTOCK;
                    log.info("A restokear");
                }
            }
        }


        if (estado == State.RESTOCK) {
            if (client.getWidget(300, 16) == null) {
                if (Inventory.getItemAmount(ItemID.JUG_OF_WINE)>0 && Inventory.full()) {
                    if (client.getWorld() != mundoCasa) {
                        PajauApiPlugin.hop(mundoCasa,worldService,client);
                        timeout = 2;
                    }else {
                        log.info("Hacia el spot");
                        estado = State.TO_SPOT;
                        victima = new ArrayList<>();
                        hopear = false;
                    }
                }else {
                    if (hopear) {
                        if (mundoActual != client.getWorld()) {
                            hopear = false;
                        } else {
                            client.openWorldHopper();
                            PajauApiPlugin.hop(false,worldService,client);
                            timeout = 2;
                        }
                    }else {
                        if (client.getLocalPlayer().getPoseAnimation()== client.getLocalPlayer().getIdlePoseAnimation()) {
                            Optional<NPC> barman = NPCs.search().withId(11874).first();
                            barman.ifPresent(npc -> {
                                NPCInteraction.interact(npc, "Trade");
                                log.info("Tradeando");
                            });
                        }
                    }
                }
            } else {
                if (Inventory.full()) {
                    log.info("punto partida hacia el spot");
                    MousePackets.queueClickPacket();
                    MovementPackets.queueMovement(new WorldPoint(3359,2959,0));
                    timeout = 3;
                } else {
                    Widget wine = client.getWidget(300,16).getChild(3);
                    if (wine.getItemQuantity() > 0) {
                        WidgetPackets.queueWidgetAction(wine, "Buy 50");
                        timeout = 1;
                    } else {
                        log.info("iniciando hopeo");
                        mundoActual = client.getWorld();
                        if (mundoCasa < 0) {
                            mundoCasa=client.getWorld();
                        }
                        hopear = true;
                        MousePackets.queueClickPacket();
                        //WidgetPackets.queueResumePause(19660801, -1);
                        //WidgetPackets.queueWidgetActionPacket(c,19660801,-1,-1);
                        //log.info("Closing shop c={}",c);
                        //c=(c+1) % 10;
                        EthanApiPlugin.invoke(-1,-1,26,-1,-1,"","",-1,-1); //cerrar shop
                    }
                }
            }
        }

        else if (estado == State.TO_SPOT) {
            if (client.getLocalPlayer().getWorldLocation().isInArea(config.spot().getPlace())) {
                Optional<TileObject> puerta = TileObjects.search().atLocation(config.spot().getDoor()).withId(DOOR_OPEN_ID).first();
                if (puerta.isPresent()) {
                    TileObjectInteraction.interact(puerta.get(), "Close");
                } else {
                    estado = State.ROBANDO;
                    log.info("TO_SPOT -> ROBANDO");
                }
            } else if (client.getLocalPlayer().getWorldLocation().equals(config.spot().getDoor())) {
                List<TileObject> door = TileObjects.search().atLocation(config.spot().getDoor()).result();
                for (TileObject o : door) {
                    if (o.getId() == DOOR_CLOSED_ID) {
                        TileObjectInteraction.interact(o, "Open");
                        log.info("abriendo la puerta");
                        timeout = 1;
                        return;
                    }
                }
                List<WorldPoint> ptsInPlace = config.spot().getPlace().toWorldPointList();


                WorldPoint tile2Enter = null;
                if (config.spot().getDoor().dx(-1).isInArea(config.spot().getPlace())) {
                    tile2Enter = config.spot().getDoor().dx(-1);
                } else if (config.spot().getDoor().dy(1).isInArea(config.spot().getPlace())) {
                    tile2Enter = config.spot().getDoor().dy(1);
                } else if (config.spot().getDoor().dx(1).isInArea(config.spot().getPlace())) {
                    tile2Enter = config.spot().getDoor().dx(1);
                } else if (config.spot().getDoor().dy(-1).isInArea(config.spot().getPlace())) {
                    tile2Enter = config.spot().getDoor().dy(-1);
                }
                if (tile2Enter == null) {
                    log.info("Tile to enter not found");
                    return;
                }


                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(tile2Enter);
                timeout = 1;
            } else if (!PajauApiPlugin.caminando(client, reverse(config.spot().getPath2bank()), 2, true)) {
                /*List<TileObject> door = TileObjects.search().atLocation(config.spot().getDoor()).result();
                for (TileObject o : door) {
                    if (o.getId() == 1533) {
                        TileObjectInteraction.interact(o, "Open");
                        log.info("abriendo la puerta");
                        timeout = 1;
                        return;
                    }
                }*/
            }
        }
    }

    private boolean hopear = false;
    private int mundoCasa = -1;
    private int mundoActual = -1;
    private int c = 1;

    private boolean doorOpen(WorldPoint worldPoint) {
        List<TileObject> objects = TileObjects.search().atLocation(config.spot().getDoor()).result();
        for (TileObject obj: objects  ) {
            if (obj.getId() == DOOR_CLOSED_ID) {
                return true;
            }
        }
        return false;
    }

    WorldPoint[] reverse(WorldPoint[] a)
    {
        int n = a.length;
        WorldPoint[] b = new WorldPoint[n];
        int j = n;
        for (WorldPoint worldPoint : a) {
            b[j - 1] = worldPoint;
            j = j - 1;
        }

        return b;
    }


}
