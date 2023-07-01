//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.hydra;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class HydraPoisonOverlay extends Overlay {
    private static final Color poisonBorder = new Color(255, 0, 0, 100);
    private static final Color poisonFill = new Color(255, 0, 0, 50);
    private final Client client;
    private Map<LocalPoint, Projectile> poisonProjectiles;

    @Inject
    public HydraPoisonOverlay(Client client) {
        this.client = client;
        this.poisonProjectiles = new HashMap();
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    public Dimension render(Graphics2D graphics) {
        if (!this.poisonProjectiles.isEmpty()) {
            this.drawPoisonArea(graphics, this.poisonProjectiles);
        }

        return null;
    }

    private void drawPoisonArea(Graphics2D graphics, Map<LocalPoint, Projectile> poisonProjectiles) {
        Area poisonTiles = new Area();
        Iterator var4 = poisonProjectiles.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<LocalPoint, Projectile> entry = (Map.Entry)var4.next();
            if (((Projectile)entry.getValue()).getEndCycle() >= this.client.getGameCycle()) {
                LocalPoint point = (LocalPoint)entry.getKey();
                Polygon poly = Perspective.getCanvasTileAreaPoly(this.client, point, 3);
                if (poly != null) {
                    poisonTiles.add(new Area(poly));
                }
            }
        }

        graphics.setPaintMode();
        graphics.setColor(poisonBorder);
        graphics.draw(poisonTiles);
        graphics.setColor(poisonFill);
        graphics.fill(poisonTiles);
    }

    void setPoisonProjectiles(Map<LocalPoint, Projectile> poisonProjectiles) {
        this.poisonProjectiles = poisonProjectiles;
    }
}
