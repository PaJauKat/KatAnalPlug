//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.hydra;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class HydraAttackCounterOverlay extends Overlay {
    private final HydraPlugin hydraPlugin;
    private final Client client;
    private final HydraConfig config;
    private Map<Integer, Hydra> hydras;
    private boolean isBoldAttackCounterOverlay;

    @Inject
    private HydraAttackCounterOverlay(HydraPlugin hydraPlugin, Client client, HydraConfig config) {
        this.hydraPlugin = hydraPlugin;
        this.config = config;
        this.client = client;
        this.hydras = new HashMap();
        this.isBoldAttackCounterOverlay = false;
        this.setLayer(OverlayLayer.ABOVE_SCENE);
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.MED);
    }

    public Dimension render(Graphics2D graphics) {
        if (!this.hydraPlugin.isPlayerAtHydraRegion()) {
            return null;
        } else {
            Iterator var2 = this.client.getNpcs().iterator();

            while(var2.hasNext()) {
                NPC npc = (NPC)var2.next();
                Hydra hydra = (Hydra)this.hydras.get(npc.getIndex());
                if (hydra != null) {
                    if (this.isBoldAttackCounterOverlay) {
                        graphics.setFont(FontManager.getRunescapeBoldFont());
                    } else {
                        graphics.setFont(FontManager.getRunescapeFont());
                    }

                    this.renderAnimationAttackType(graphics, hydra);
                    this.renderAttackCount(graphics, hydra);
                }
            }

            return null;
        }
    }

    private void renderAnimationAttackType(Graphics2D graphics, Hydra hydra) {
        HydraAnimation hydraAnimation = hydra.getHydraAnimation();
        if (hydraAnimation != null) {
            int heightOffset = 100;
            Point textLocation = hydra.getCanvasTextLocation(graphics, "TEMP!", hydra.getLogicalHeight() + 100);
            if (textLocation != null) {
                boolean attackCountIsMax = hydra.getAttackCount() == 3;
                switch (hydraAnimation) {
                    case RANGE:
                        if (attackCountIsMax) {
                            OverlayUtil.renderTextLocation(graphics, textLocation, HydraAnimation.MAGIC.getText(), HydraAnimation.MAGIC.getColor());
                        } else {
                            OverlayUtil.renderTextLocation(graphics, textLocation, HydraAnimation.RANGE.getText(), HydraAnimation.RANGE.getColor());
                        }
                        break;
                    case MAGIC:
                        if (attackCountIsMax) {
                            OverlayUtil.renderTextLocation(graphics, textLocation, HydraAnimation.RANGE.getText(), HydraAnimation.RANGE.getColor());
                        } else {
                            OverlayUtil.renderTextLocation(graphics, textLocation, HydraAnimation.MAGIC.getText(), HydraAnimation.MAGIC.getColor());
                        }
                }

            }
        }
    }

    private void renderAttackCount(Graphics2D graphics, Hydra hydra) {
        int attackCount = hydra.getAttackCount();
        int heightOffset = 100;
        Point textLocation = hydra.getCanvasTextLocation(graphics, Integer.toString(attackCount), hydra.getLogicalHeight() + 30);
        if (textLocation != null) {
            OverlayUtil.renderTextLocation(graphics, textLocation, Integer.toString(attackCount), Color.WHITE);
        }

    }

    void setHydras(Map<Integer, Hydra> hydras) {
        this.hydras = hydras;
    }

    void setBoldAttackCounterOverlay(boolean isBoldAttackCounterOverlay) {
        this.isBoldAttackCounterOverlay = isBoldAttackCounterOverlay;
    }
}
