package com.example.fungus;

import com.example.Caminador.Caminador;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
        name = "Fungus anal",
        enabledByDefault = false,
        tags = {"pajau"}
)
public class FungusPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private Caminador caminador;

    private State estado = State.CALLAMPEANDO;

    private int timeout = 0;

    private static final WorldArea  areaSpot = new WorldArea(3666,3254,3,3,0);
    private static final WorldPoint wpCenter = new WorldPoint(3667,3255,0);

    private static final WorldPoint[] logsWP = {
            new WorldPoint(3668, 3255, 0),
            new WorldPoint(3666,3256,0),
            new WorldPoint(3666,3254,0),
            new WorldPoint(3668,3254,0)
    };

    private static final WorldPoint[] camino = {
            new WorldPoint(3653,3228,0),
            new WorldPoint(3660,3245,0),
            new WorldPoint(3667,3255,0)
    };

    private enum State {
        BANKING,
        HACIA_SPOT,
        HACIA_BANK,
        CALLAMPEANDO
    }

    @Override
    protected void startUp() throws Exception {
        estado = State.CALLAMPEANDO;
    }

    @Subscribe
    void onGameTick(GameTick event) {
        if (timeout > 0) {
            timeout--;
            return;
        }

        Player player = client.getLocalPlayer();

        if (estado == State.CALLAMPEANDO) {
            if(Inventory.full()){
                estado = State.HACIA_BANK;
                timeout = 3;
            }

            List<TileObject> logs = TileObjects.search().filter(x -> x.getWorldLocation().isInArea(areaSpot) && x.getId() == 3509).result();
            if (logs.isEmpty()) {
                log.info("wea {}",player.getWorldLocation());
                if (!player.getWorldLocation().equals(wpCenter) ) {
                    log.info("moviendo al centro");
                    MousePackets.queueClickPacket();
                    MovementPackets.queueMovement(wpCenter);
                    timeout = 2;
                    return;
                }
                Optional<Widget> itemBloom = Inventory.search().withAction("Bloom").first();
                if (itemBloom.isEmpty()) {
                    log.info("No se encontro item de Bloom");
                    return;
                }

                itemBloom.ifPresent(x -> {
                    log.info("Bloomeando");
                    InventoryInteraction.useItem(x, "Bloom");
                });

                timeout = 4;
            } else {
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
                            timeout=1;
                        }
                        break;
                    }
                }



            }
        }

        else if (estado == State.HACIA_BANK) {
            WorldPoint wpPlayer = player.getWorldLocation();
            for (int i = 0; i < camino.length; i++) {
                if (wpPlayer.isInArea(caminador.getAreaFromWP(camino[camino.length-1-i],2)) ) {
                    if (i + 1 >= camino.length) {
                        log.info("se paso al estado bankeando");
                        estado = State.BANKING;
                    } else {
                        log.info("moviendo al tile {}",camino[camino.length-1-i]);
                        MovementPackets.queueMovement(caminador.getNextWp(camino[camino.length-1-i],2,client));
                        timeout = 2;
                    }
                    break;
                }

            }
        }

    }

    int c = 3;





}
