package com.example.venenatis;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
        name = "<html>[<font color=\"#59D634\">P</font>] Venenatis</html>",
        description = "Counts venenatis movements",
        tags = {"pajau"},
        enabledByDefault = false
)
public class VenenatisPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private VenenatisOverlay venenatisOverlay;

    @Inject
    private OverlayManager overlayManager;

    private WorldPoint posPasada;

    @Getter
    private NPC venenatis = null;

    @Getter
    private int moveCounter = -1;

    private int offset = 1;

    private int offsetInit = 1;

    @Override
    protected void startUp() throws Exception {
        this.overlayManager.add(venenatisOverlay);
        offsetInit=1;
    }

    @Override
    protected void shutDown() throws Exception {
        this.overlayManager.remove(venenatisOverlay);
    }

    @Subscribe
    void onNpcSpawned(NpcSpawned event) {
        if(event.getNpc().getName()==null) return;
        if (event.getNpc().getName().contains("Spiderling")) { //sacar el Id mejor porque las chiquitar tienen el nombre
            log.info("Spawnearon spiderling");
            moveCounter = 1;
        } else if (event.getNpc().getName().contains("Venenatis")) {
            log.info("Spawneo venenatis");
            venenatis = event.getNpc();
        }
    }

    @Subscribe
    void onNpcDespawned(NpcDespawned event) {
        if(venenatis == null) return;
        if (event.getNpc().getId() == venenatis.getId()) {
            log.info("murio la penenatis");
            venenatis = null;
        }

    }

    private int aux = 0;
    @Subscribe
    void onGameTick(GameTick event) {
        if (venenatis == null) return;
        log.info("dentro");
        log.info("moveCounter {}",moveCounter);
        WorldPoint posActual = venenatis.getWorldLocation();
        log.info("katarina: {}",posActual);
        log.info("gatarina: {}",posPasada);
        if (posPasada == null || !posPasada.equals(posActual) ) {
            log.info("woof");
            offset = 2;
            posPasada = posActual;
            if (aux == 0) {
                log.info("meow");
                aux = 1;
                moveCounter++;
            }
        } else if (aux == 1) {
            offset--;
            if (offset == 0) {
                aux=0;
            }
        }


    }
}
