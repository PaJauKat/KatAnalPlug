package com.example.CaTimer;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@PluginDescriptor(
        name = "<html>[<font color=\"#59D634\">P</font>] Tiempos CA</html>",
        tags = {"pajau","timer","combat achievement"},
        enabledByDefault = false
)
@Slf4j
public class CaTimerPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private MuspahTimerPanel muspahTimerPanel;

    @Inject
    private OverlayManager overlayManager;

    public NPC muspah;

    @Inject
    private ItemManager itemManager;

    public Duration tiempoPelea;
    public Instant tiempoSpawn;
    public int tickSpawn;
    public static int peleaTicks;
    boolean inFight = false;

    @Subscribe
    void onNpcSpawned(NpcSpawned event){
        if(event.getNpc().getName() == null) return;
        if (event.getNpc().getName().contains("Muspah") || event.getNpc().getId()==12077 || event.getNpc().getId()==12078) {
            log.info("Aparecio el muspah");
            muspah = event.getNpc();
            createTimer(Duration.ofSeconds(90));
            tickSpawn=client.getTickCount();
            inFight =true;
        }
    }

    @Override
    protected void startUp() throws Exception {
        this.overlayManager.add(this.muspahTimerPanel);
    }

    @Override
    protected void shutDown() throws Exception {
        this.overlayManager.remove(this.muspahTimerPanel);
    }

    @Subscribe
    void onGameTick(GameTick event) {
        if (inFight && Arrays.stream(client.getMapRegions()).anyMatch(x->x==11330)) {
            //if (!muspah.isDead() || (gotKc==1)) {
            if(inFight){
                peleaTicks = client.getTickCount() - tickSpawn +1;
            }
        }
    }

    @Subscribe
    void onVarbitChanged(VarbitChanged event) {
        if (inFight) {
            if (event.getVarpId() == 3752) {
                inFight =false;
            }
        }
    }

    void removeTimer() {
        infoBoxManager.removeIf(t-> t instanceof MuspahTimer);
    }
    void createTimer(Duration duration) {
        removeTimer();
        BufferedImage iconoMuspah = itemManager.getImage(ItemID.MUPHIN);
        infoBoxManager.addInfoBox(new MuspahTimer(duration,iconoMuspah,this));


    }



}
