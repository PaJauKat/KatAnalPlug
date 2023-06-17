package com.example.worldFinder;

import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.util.Objects;

import static java.lang.Math.abs;

public class WorldFinderOverlay extends Overlay {

    @Inject
    private WorldFinderPlugin plugin;

    @Inject
    WorldFinderOverlay() {
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.MED);
    }


    @Override
    public Dimension render(Graphics2D graphics) {
        if (plugin.getTileInicial() != null) {
            Polygon tileInit = Perspective.getCanvasTilePoly(plugin.getClient(), Objects.requireNonNull(LocalPoint.fromWorld(plugin.getClient(), plugin.getTileInicial())));
            OverlayUtil.renderPolygon(graphics, tileInit, Color.blue);
        }
        if (plugin.getTileFinal() != null) {
            Polygon tileFin = Perspective.getCanvasTilePoly(plugin.getClient(), Objects.requireNonNull(LocalPoint.fromWorld(plugin.getClient(), plugin.getTileFinal())));
            OverlayUtil.renderPolygon(graphics, tileFin, Color.blue);

            int dx = plugin.getTileFinal().getX() - plugin.getTileInicial().getX();
            int dy = plugin.getTileFinal().getY() - plugin.getTileInicial().getY();

            int centroX = ( Objects.requireNonNull(LocalPoint.fromWorld(plugin.getClient(), plugin.getTileInicial())).getX() + Objects.requireNonNull(LocalPoint.fromWorld(plugin.getClient(), plugin.getTileFinal())).getX() ) / 2;
            int centroY = ( Objects.requireNonNull(LocalPoint.fromWorld(plugin.getClient(), plugin.getTileInicial())).getY() + Objects.requireNonNull(LocalPoint.fromWorld(plugin.getClient(), plugin.getTileFinal())).getY() ) / 2;
            LocalPoint centro = new LocalPoint(centroX, centroY);
            Polygon areaFinder = Perspective.getCanvasTileAreaPoly(plugin.getClient(), centro,abs(dx)+1 ,abs(dy)+1, plugin.getClient().getPlane(),0);
            OverlayUtil.renderPolygon(graphics,areaFinder,Color.magenta);
        }
        return null;
    }
}
