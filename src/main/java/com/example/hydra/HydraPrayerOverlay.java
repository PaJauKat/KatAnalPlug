//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.hydra;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

@Singleton
public class HydraPrayerOverlay extends Overlay {
    private static final Color ACTIVATED_BACKGROUND_COLOR = new Color(0, 150, 0, 150);
    private static final Color NOT_ACTIVATED_BACKGROUND_COLOR = new Color(150, 0, 0, 150);
    private final HydraPlugin hydraPlugin;
    private final Client client;
    private final SpriteManager spriteManager;
    private final PanelComponent panelComponent;
    private Map<Integer, Hydra> hydras;
    private BufferedImage bufferedImageRange;
    private BufferedImage bufferedImageMagic;

    @Inject
    private HydraPrayerOverlay(HydraPlugin hydraPlugin, Client client, SpriteManager spriteManager) {
        this.hydraPlugin = hydraPlugin;
        this.client = client;
        this.spriteManager = spriteManager;
        this.panelComponent = new PanelComponent();
        this.hydras = new HashMap();
        this.bufferedImageRange = null;
        this.bufferedImageMagic = null;
        this.setPosition(OverlayPosition.BOTTOM_RIGHT);
        this.setPriority(OverlayPriority.HIGH);
    }

    public Dimension render(Graphics2D graphics) {
        NPC npc = this.hydraPlugin.getInteractingNpc();
        if (npc == null) {
            return null;
        } else {
            Hydra hydra = (Hydra)this.hydras.get(npc.getIndex());
            if (hydra == null) {
                return null;
            } else {
                HydraAnimation hydraAnimation = hydra.getHydraAnimation();
                if (hydraAnimation != null && HydraPlugin.VALID_HYDRA_ANIMATIONS.contains(hydraAnimation)) {
                    if (this.bufferedImageMagic == null) {
                        this.bufferedImageMagic = this.spriteManager.getSprite(127, 0);
                    }

                    if (this.bufferedImageRange == null) {
                        this.bufferedImageRange = this.spriteManager.getSprite(128, 0);
                    }

                    boolean attackCountIsMax = hydra.getAttackCount() == 3;
                    switch (hydraAnimation) {
                        case RANGE:
                            if (attackCountIsMax) {
                                return this.renderPanelMagic(graphics);
                            }

                            return this.renderPanelRange(graphics);
                        case MAGIC:
                            if (attackCountIsMax) {
                                return this.renderPanelRange(graphics);
                            }

                            return this.renderPanelMagic(graphics);
                        default:
                            return null;
                    }
                } else {
                    return null;
                }
            }
        }
    }

    private Dimension renderPanelMagic(Graphics2D graphics) {
        this.panelComponent.getChildren().clear();
        this.panelComponent.getChildren().add(new ImageComponent(this.bufferedImageMagic));
        this.panelComponent.setBackgroundColor(this.client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC) ? ACTIVATED_BACKGROUND_COLOR : NOT_ACTIVATED_BACKGROUND_COLOR);
        return this.panelComponent.render(graphics);
    }

    private Dimension renderPanelRange(Graphics2D graphics) {
        this.panelComponent.getChildren().clear();
        this.panelComponent.getChildren().add(new ImageComponent(this.bufferedImageRange));
        this.panelComponent.setBackgroundColor(this.client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES) ? ACTIVATED_BACKGROUND_COLOR : NOT_ACTIVATED_BACKGROUND_COLOR);
        return this.panelComponent.render(graphics);
    }

    void setHydras(Map<Integer, Hydra> hydras) {
        this.hydras = hydras;
    }
}
