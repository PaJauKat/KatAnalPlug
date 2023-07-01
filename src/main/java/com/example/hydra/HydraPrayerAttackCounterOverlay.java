//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.hydra;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

@Singleton
public class HydraPrayerAttackCounterOverlay extends Overlay {
    private final HydraPlugin hydraPlugin;
    private final PanelComponent panelComponent;
    private Map<Integer, Hydra> hydras;

    @Inject
    private HydraPrayerAttackCounterOverlay(HydraPlugin hydraPlugin) {
        this.hydraPlugin = hydraPlugin;
        this.panelComponent = new PanelComponent();
        this.panelComponent.setPreferredSize(new Dimension(14, 0));
        this.hydras = new HashMap();
        this.setPosition(OverlayPosition.BOTTOM_RIGHT);
        this.setPriority(OverlayPriority.MED);
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
                String attackCount = String.valueOf(hydra.getAttackCount());
                this.panelComponent.getChildren().clear();
                this.panelComponent.getChildren().add(LineComponent.builder().right(attackCount).build());
                return this.panelComponent.render(graphics);
            }
        }
    }

    void setHydras(Map<Integer, Hydra> hydras) {
        this.hydras = hydras;
    }
}
