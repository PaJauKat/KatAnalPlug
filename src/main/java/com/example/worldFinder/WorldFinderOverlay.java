package com.example.worldFinder;

import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;

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
            LocalPoint tileLP = LocalPoint.fromWorld(plugin.getClient(), plugin.getTileInicial());
            if ( tileLP == null)
            {
                return null;
            }

            Polygon tileInit = Perspective.getCanvasTilePoly(plugin.getClient(), tileLP);
            OverlayUtil.renderPolygon(graphics, tileInit, Color.blue);
        }
        if (plugin.getTileFinal() != null) {
            LocalPoint finalLP = LocalPoint.fromWorld(plugin.getClient(), plugin.getTileFinal());
            if (finalLP == null)
            {
                return null;
            }

            Polygon tileFin = Perspective.getCanvasTilePoly(plugin.getClient(), finalLP);
            OverlayUtil.renderPolygon(graphics, tileFin, Color.blue);

            int dx = plugin.getTileFinal().getX() - plugin.getTileInicial().getX();
            int dy = plugin.getTileFinal().getY() - plugin.getTileInicial().getY();

            LocalPoint inicialLP = LocalPoint.fromWorld(plugin.getClient(), plugin.getTileInicial());
            if (inicialLP == null) {
                return null;
            }

            int centroX = ( inicialLP.getX() + finalLP.getX() ) / 2;
            int centroY = ( inicialLP.getY() + finalLP.getY() ) / 2;
            LocalPoint centro = new LocalPoint(centroX, centroY);
            Polygon areaFinder = Perspective.getCanvasTileAreaPoly(plugin.getClient(), centro,abs(dx)+1 ,abs(dy)+1, plugin.getClient().getPlane(),0);
            OverlayUtil.renderPolygon(graphics,areaFinder,Color.magenta);
        }
        return null;
    }
}
