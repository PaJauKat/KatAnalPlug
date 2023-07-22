package com.example.fungus;

import com.example.Caminador.Caminador;
import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.BankInventoryInteraction;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.ObjectPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
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
import javax.swing.text.html.Option;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
        name = "<html>[<font color=\"#FA4444\">P</font>] Mort myre fungus</html>",
        enabledByDefault = false,
        tags = {"pajau"}
)
public class FungusPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private Caminador caminador;

    @Inject
    private FungusConfig config;

    @Inject
    private ClientThread clientThread;

    private State estado = State.RECONOCIENDO_ESTADO;

    private int timeout = 0;
    private boolean enCamino = false;
    int meow = 0;
    private static final int[] verSinhazaRegion = {14642};

    private static final WorldArea  areaSpot = new WorldArea(3666,3254,3,3,0);
    private static final WorldPoint wpCenter = new WorldPoint(3667,3255,0);

    private static final WorldPoint[] logsWP = {
            new WorldPoint(3668, 3255, 0),
            new WorldPoint(3666,3256,0),
            new WorldPoint(3666,3254,0),
            new WorldPoint(3668,3254,0)
    };

    private static final WorldPoint[] camino = {
            new WorldPoint(3651,3208,0),
            new WorldPoint(3653,3228,0),
            new WorldPoint(3660,3245,0),
            new WorldPoint(3667,3255,0)
    };

    private static final WorldPoint bankWP = new WorldPoint(3651,3207,0);
    private int idleTimer = 0;
    private int failSafe = 0;
    private boolean encendido = false;

    private enum State {
        RECONOCIENDO_ESTADO,
        BANKING,
        HACIA_SPOT,
        HACIA_BANK,
        CALLAMPEANDO,
        PRAYER_RESTORATION,
        APAGADO
    }

    void reset() {
        timeout = 0;
        enCamino = false;
        idleTimer = 0;
        failSafe = 0;
        meow = 0;
    }

    @Override
    protected void startUp() throws Exception {
        encendido = false;
    }

    @Provides
    FungusConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(FungusConfig.class);
    }

    @Subscribe
    void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("fungus") ) {
            if (event.getKey().equals("onOff")) {
                encendido = !encendido;
                clientThread.invoke(() -> {
                    if (encendido) {
                        reset();
                        estado = State.RECONOCIENDO_ESTADO;
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE,"", ColorUtil.wrapWithColorTag("Encendido",Color.GREEN), "");
                    } else {
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE,"", ColorUtil.wrapWithColorTag("Apagado", Color.red),"");
                    }
                });
            }
        }
    }

    @Subscribe
    void onGameTick(GameTick event) {
        if (!encendido) return;

        if (timeout > 0) {
            timeout--;
            return;
        }

        Player player = client.getLocalPlayer();
        if (estado == State.RECONOCIENDO_ESTADO) {
            if (Arrays.stream(verSinhazaRegion).anyMatch(x -> x == player.getWorldLocation().getRegionID())) {
                if (client.getWidget(WidgetInfo.BANK_CONTAINER) != null) {
                    estado = State.BANKING;
                } else {
                    if (Inventory.full()) {
                        estado = State.HACIA_BANK;
                    } else if (Inventory.getEmptySlots() > 10) {
                        if (player.getWorldLocation().distanceTo(wpCenter) < player.getWorldLocation().distanceTo(bankWP)) {
                            if (client.getBoostedSkillLevel(Skill.PRAYER) > 45) {
                                estado = State.HACIA_SPOT;
                            } else {
                                estado = State.HACIA_BANK;
                            }
                        } else {
                            estado = State.HACIA_BANK;
                        }
                    } else {
                        estado = State.HACIA_BANK;
                    }
                }
            } else {
                estado = State.PRAYER_RESTORATION;
            }
        }


        else if (estado == State.CALLAMPEANDO) {
            if (Inventory.full() || client.getBoostedSkillLevel(Skill.PRAYER)==0 ) {
                estado = State.HACIA_BANK;
                timeout = 3;
                return;
            }
            log.info("kata {}", Inventory.getEmptySlots());
            List<TileObject> logs = TileObjects.search().filter(x -> x.getWorldLocation().isInArea(areaSpot) && x.getId() == 3509).result();
            if (logs.isEmpty()) {   //hay q Bloomear
                //log.info("wea {}",player.getWorldLocation());
                if (player.getPoseAnimation() == player.getIdlePoseAnimation()) {
                    if (!player.getWorldLocation().equals(wpCenter)) {
                        log.info("moviendo al centro");
                        MousePackets.queueClickPacket();
                        MovementPackets.queueMovement(wpCenter);
                        timeout = 2;
                    } else {
                        Optional<Widget> itemBloom = Inventory.search().withAction("Bloom").first();
                        if (itemBloom.isEmpty()) {
                            Optional<EquipmentItemWidget> aux = Equipment.search().withAction("Bloom").first();
                            if (aux.isPresent()) {
                                itemBloom = Optional.of(aux.get());
                            }
                        }
                        if (itemBloom.isEmpty()) {
                            log.info("No se encontro item de Bloom");
                            return;
                        }

                        itemBloom.ifPresent(x -> {
                            log.info("Bloomeando");
                            //InventoryInteraction.useItem(x, "Bloom");
                            WidgetPackets.queueWidgetAction(x,"Bloom");
                        });
                        timeout = 4;
                    }

                }
            } else {    //hay q recojer
                Optional<TileObject> logPick;
                for (WorldPoint p : logsWP) {
                    if (logs.stream().anyMatch(x -> x.getWorldLocation().equals(p))) {
                        logPick = logs.stream().filter(y -> y.getWorldLocation().equals(p)).collect(Collectors.toList()).stream().findFirst();
                        logPick.ifPresent(z -> TileObjectInteraction.interact(z, "Pick"));
                        log.info("Pickeando");
                        timeout = 5;
                        WorldPoint wpPLayer = player.getWorldLocation();
                        if (player.getWorldLocation().distanceTo(p) == 1) {
                            if (wpPLayer.getX() == p.getX() || wpPLayer.getY() == p.getY()) {
                                timeout = 0;
                            } else {
                                timeout = 1;
                            }
                        } else if (wpPLayer.distanceTo(p) == 2) {
                            timeout = 1;
                        }
                        break;
                    }
                }
            }
        }

        else if (estado == State.HACIA_BANK) {
            WorldPoint wpPlayer = player.getWorldLocation();
            if (enCamino) {
                for (int i = 0; i < camino.length; i++) {
                    if (wpPlayer.isInArea(caminador.getAreaFromWP(camino[camino.length - 1 - i], 2))) {
                        if (i + 1 >= camino.length) {
                            log.info("se paso al estado bankeando");
                            enCamino = false;
                            estado = State.BANKING;
                        } else {
                            if (!camino[camino.length-2-i].isInScene(client)) {
                                return;
                            }
                            if (camino.length - 2 - i == 0) {
                                log.info("moviendo al tile final {}", camino[camino.length - 2 - i]);
                                MousePackets.queueClickPacket();
                                MovementPackets.queueMovement(camino[camino.length - 2 - i]);
                                timeout = 2;
                            } else {
                                log.info("moviendo al tile {}", camino[camino.length - 2 - i]);
                                MousePackets.queueClickPacket();
                                MovementPackets.queueMovement(caminador.getNextWp(camino[camino.length - 2 - i], 2, client));
                                timeout = 2;
                            }

                        }
                        break;
                    }
                }
            } else {
                Optional<WorldPoint> puntoCercano = Arrays.stream(camino).min(Comparator.comparingInt(x -> x.distanceTo(wpPlayer)));
                for (int i = 0; i < camino.length; i++) {
                    if (camino[i].equals(puntoCercano.get())) {
                        enCamino = true;
                        if (i == 0) {
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(caminador.getNextWp(camino[i], 2, client));
                        } else {
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(caminador.getNextWp(camino[i - 1], 2, client));
                        }
                    }
                }
            }
        }


        else if (estado == State.BANKING) {
            if (!Bank.isOpen()) {
                if (player.getIdlePoseAnimation() == player.getPoseAnimation()) {
                    Optional<TileObject> bank = TileObjects.search().atLocation(bankWP).first();
                    if (bank.isEmpty()) {
                        log.info("no se encontro bank");
                    } else {
                        meow = 1;
                        TileObjectInteraction.interact(bank.get(), "Bank");
                        timeout = 1;
                    }
                }
            } else {
                if (meow == 1) {
                    timeout = 3;
                    meow = 0;
                    return;
                }
                Optional<Widget> callampas = Inventory.search().withId(2970).first();
                if (callampas.isPresent()) {
                    BankInventoryInteraction.useItem(2970, "Deposit-All");
                    timeout = 2;
                    log.info("Depositando");
                    if (client.getBoostedSkillLevel(Skill.PRAYER) < 45) {
                        estado = State.PRAYER_RESTORATION;
                    } else {
                        estado = State.HACIA_SPOT;
                    }
                }
            }
        }


        else if (estado == State.PRAYER_RESTORATION) {
            if (client.getBoostedSkillLevel(Skill.PRAYER) < 45) {
                if (Arrays.stream(client.getMapRegions()).anyMatch(x -> Arrays.stream(verSinhazaRegion).anyMatch(y -> x == y))) {
                    Optional<Widget> teleOut = Inventory.search().withId(ItemID.TELEPORT_TO_HOUSE).first();
                    String action = "Break";
                    if (teleOut.isEmpty()) {
                        teleOut = Inventory.search().withId(ItemID.CONSTRUCT_CAPET).first();
                        action = "Tele to POH";
                    }
                    if (teleOut.isPresent()) {
                        InventoryInteraction.useItem(teleOut.get(), action);
                        timeout = 6;
                        log.info("tele a la casa");
                    }
                } else {
                    Optional<TileObject> prayRecharger = TileObjects.search().nameContains("Ornate pool").first();
                    if (prayRecharger.isEmpty()) {
                        prayRecharger = TileObjects.search().nameContains("Altar").first();
                    }
                    if (prayRecharger.isPresent()) {
                        log.info("ID de la wea: {}", prayRecharger.get().getId());
                        //TileObjectInteraction.interact(prayRecharger.get(), "Drink");
                        ObjectPackets.queueObjectAction(1,prayRecharger.get().getId(),prayRecharger.get().getWorldLocation().getX(),prayRecharger.get().getWorldLocation().getY(),false);
                        timeout = 4;
                    }
                }
            } else {
                if (Arrays.stream(client.getMapRegions()).anyMatch(x -> Arrays.stream(verSinhazaRegion).anyMatch(y -> x == y))) {
                    estado = State.HACIA_SPOT;
                    log.info("Cambiando a estado - HACIA SPOT");
                } else {
                    Optional<Widget> drakan = Inventory.search().nameContains("Drakan").first();
                    if (drakan.isPresent()) {
                        log.info("a versinhaza");
                        InventoryInteraction.useItem(drakan.get(), "Ver Sinhaza");
                        timeout = 4;
                    }
                }
            }
        }

        else if (estado == State.HACIA_SPOT) {
            WorldPoint wpPlayer = player.getWorldLocation();
            if (player.getPoseAnimation() == player.getIdlePoseAnimation() && enCamino) {
                idleTimer++;
                failSafe++;
                if (failSafe > 3) {
                    estado = State.APAGADO;
                    failSafe=0;
                    return;
                }
                if (idleTimer >= 8) {
                    enCamino = false;
                    idleTimer = 0;
                }
            }
            if (enCamino) {
                for (int i = 0; i < camino.length; i++) {
                    if (wpPlayer.isInArea(caminador.getAreaFromWP(camino[i], 2))) {
                        if (i + 1 >= camino.length) {
                            log.info("se paso al estado CALLAMPEANDO");
                            enCamino = false;
                            failSafe = 0;
                            estado = State.CALLAMPEANDO;
                        } else {
                            log.info("moviendo al tile {}", camino[i+1]);
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(caminador.getNextWp(camino[i+1], 2, client));
                            timeout = 2;
                        }
                        break;
                    }
                }
            } else {
                Optional<WorldPoint> puntoCercano = Arrays.stream(camino).min(Comparator.comparingInt(x -> x.distanceTo(wpPlayer)));
                for (int i = 0; i < camino.length; i++) {
                    if (camino[i].equals(puntoCercano.get())) {
                        enCamino = true;
                        log.info("valor de i: {}",i);
                        if (i == camino.length-1) {
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(caminador.getNextWp(camino[i], 2, client));
                        } else {
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(caminador.getNextWp(camino[i + 1], 2, client));
                        }
                    }
                }
            }
        }

    }

    int c = 3;








}
