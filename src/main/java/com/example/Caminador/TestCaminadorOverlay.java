package com.example.Caminador;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.util.Iterator;
import java.util.Objects;

public class TestCaminadorOverlay extends Overlay {
    private final Client client;

    private final TestCaminadorPlugin plugin;

    @Inject
    TestCaminadorOverlay(TestCaminadorPlugin plugin, Client client1){
        this.client=client1;
        this.plugin=plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if(client.getSelectedSceneTile() != null){
            Polygon poly = Perspective.getCanvasTilePoly(client,client.getSelectedSceneTile().getLocalLocation());
            OverlayUtil.renderPolygon(graphics,poly,Color.magenta);
            if (plugin.tileCaminado != null) {
                LocalPoint lp = LocalPoint.fromWorld(client,plugin.getTileCaminado());
                if (lp != null) {
                    Polygon tilecito = Perspective.getCanvasTilePoly(client,lp);
                    OverlayUtil.renderPolygon(graphics,tilecito,Color.GREEN);
                }

            }
        }

        if (!plugin.kat.isEmpty()) {

            for (WorldPoint worldPoint : plugin.kat) {
                LocalPoint lp = LocalPoint.fromWorld(client, worldPoint);
                if (lp != null) {
                    Polygon polygon = Perspective.getCanvasTilePoly(client, lp);
                    OverlayUtil.renderPolygon(graphics, polygon, Color.ORANGE);
                }
            }

            /*plugin.kat.forEach(x -> {
                LocalPoint lp = LocalPoint.fromWorld(client, x);
                if (lp != null) {
                    Polygon polygon = Perspective.getCanvasTilePoly(client, lp );
                    OverlayUtil.renderPolygon(graphics,polygon,Color.ORANGE);
                }

            });*/

        }

        return null;
    }
}
