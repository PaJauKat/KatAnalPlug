//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.alchemicalhydra.overlay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;

import com.example.alchemicalhydra.AlchemicalHydraConfig;
import com.example.alchemicalhydra.AlchemicalHydraPlugin;
import com.example.alchemicalhydra.entity.Hydra;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

@Slf4j
public class PrayerOverlay extends Overlay {
    private final Client client;
    private final AlchemicalHydraPlugin plugin;
    private final AlchemicalHydraConfig config;
    private Hydra hydra;

    private int meow = 0;

    @Inject
    private PrayerOverlay(Client client, AlchemicalHydraPlugin plugin, AlchemicalHydraConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.hydra = plugin.getHydra();
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.HIGH);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public Dimension render(Graphics2D graphics2D) {
        this.hydra = this.plugin.getHydra();
        if (this.hydra == null) {
            return null;
        } else {
            this.renderPrayerWidget(graphics2D);
            return null;
        }
    }

    private void renderPrayerWidget(Graphics2D graphics2D) {
        Prayer prayer = this.hydra.getNextAttack().getPrayer();
        meow++;
        if (meow == 50) {
            meow = 0;
            log.info("pene: {}",prayer);
            log.info("pene2: {}",prayer.name());
        }
        OverlayUtil.renderPrayerOverlay(graphics2D, this.client, prayer, prayer == Prayer.PROTECT_FROM_MAGIC ? Color.CYAN : Color.GREEN);
    }
}
