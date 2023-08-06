package com.example.vardorvis;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.geometry.Geometry;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.GeneralPath;

public class VardorvisOverlay extends Overlay {

    @Inject
    private Client client;

    @Inject
    private VardorvisPlugin plugin;

    @Inject
    private VardorvisConfig config;

    @Inject
    VardorvisOverlay(Client ct, VardorvisPlugin pg, VardorvisConfig cg) {
        this.client = ct;
        this.plugin = pg;
        this.config = cg;
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
    }

    int MAX_LOCAL_CANVAS_LENGTH = 20 * Perspective.LOCAL_TILE_SIZE;

    @Override
    public Dimension render(Graphics2D graphics) {

        if (plugin.axe_1_9 > 0) {
            renderPath(graphics,plugin.lines_1_9,config.axesColor());
        }
        if (plugin.axe_2_8 > 0) {
            renderPath(graphics,plugin.lines_2_8,config.axesColor());
        }
        if (plugin.axe_7_3 > 0) {
            renderPath(graphics,plugin.lines73,config.axesColor());
        }
        if (plugin.axe_4_6 > 0) {
            renderPath(graphics,plugin.lines_4_6,config.axesColor());
        }

        return null;
    }

    void renderPath(Graphics2D graphics, GeneralPath path, Color color) {
        LocalPoint playerLocal = client.getLocalPlayer().getLocalLocation();
        Rectangle canvasPath = new Rectangle(
                playerLocal.getX() - MAX_LOCAL_CANVAS_LENGTH,
                playerLocal.getY() - MAX_LOCAL_CANVAS_LENGTH,
                2*MAX_LOCAL_CANVAS_LENGTH,
                2*MAX_LOCAL_CANVAS_LENGTH
        );

        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(1));


        path = Geometry.clipPath(path, canvasPath);
        path = Geometry.filterPath(path, (p1, p2) ->
            Perspective.localToCanvas(client, new LocalPoint((int)p1[0], (int)p1[1]), client.getPlane()) != null &&
            Perspective.localToCanvas(client, new LocalPoint((int)p2[0], (int)p2[1]), client.getPlane()) != null);
        path = Geometry.transformPath(path, p -> {
            Point point = Perspective.localToCanvas(client,new LocalPoint((int) p[0], (int) p[1]),client.getPlane());
            p[0] = point.getX();
            p[1] = point.getY();
        });

        graphics.draw(path);
        graphics.fill(path);


    }
}
