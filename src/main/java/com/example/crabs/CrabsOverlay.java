package com.example.crabs;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;

public class CrabsOverlay extends Overlay {

    @Inject
    private Client client;

    @Inject
    private CrabsPlugin plugin;

    @Inject
    private CrabsConfig config;

    @Inject
    CrabsOverlay(Client clt, CrabsPlugin plg, CrabsConfig cfig) {
        this.plugin=plg;
        this.client=clt;
        this.config = cfig;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.MED);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }




    @Override
    public Dimension render(Graphics2D graphics) {
        for (int i = 0; i < TilePelea.values().length; i++) {
            List<WorldPoint> tiles = TilePelea.values()[i].getPuntos();
            for (int j = 0; j < tiles.size(); j++) {
                if (tiles.get(j).isInScene(client) && tiles.get(j).distanceTo(client.getLocalPlayer().getWorldLocation()) <= plugin.MAX_DISTANT) {
                    LocalPoint lp = LocalPoint.fromWorld(client, tiles.get(j));
                    if (lp == null || !lp.isInScene()) {
                        continue;
                    }
                    Polygon poly = Perspective.getCanvasTilePoly(client, lp);
                    OverlayUtil.renderPolygon(graphics, poly, Color.lightGray);
                    graphics.setFont(new Font("Arial",Font.BOLD,20));
                    Point punto = Perspective.getCanvasTextLocation(client, graphics, lp, TilePelea.values()[i].toString(), 0);
                    OverlayUtil.renderTextLocation(graphics, punto, TilePelea.values()[i].toString(), Color.lightGray);
                }
            }

        }



        return null;
    }
}
