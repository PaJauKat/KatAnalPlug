package com.example.nex;

import com.example.PajauApi.PajauApiPlugin;
import com.sun.jna.platform.win32.WinBase;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;

public class NexOverlay extends Overlay {

    @Inject
    private NexPlugin nexPlugin;
    @Inject
    private Client client;

    private int counter = 0;

    @Inject
    NexOverlay(NexPlugin plugin,Client clt) {
        this.nexPlugin = plugin;
        this.client = clt;
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (nexPlugin.getEscapeTile() != null) {
            LocalPoint lp = LocalPoint.fromWorld(client, nexPlugin.getEscapeTile());
            if (lp == null || !lp.isInScene()) {
                return null;
            }
            Polygon poly = Perspective.getCanvasTilePoly(client, lp);
            OverlayUtil.renderPolygon(graphics,poly,Color.ORANGE);

            graphics.setFont(new Font("Arial",Font.BOLD,24));
            Point puntoTex = Perspective.getCanvasTextLocation(client, graphics, LocalPoint.fromWorld(client, nexPlugin.getEscapeTile()),"Anal",0);
            OverlayUtil.renderTextLocation(graphics,puntoTex,"Anal",Color.ORANGE);

        }

        if (PajauApiPlugin.tilesBuscados.size()>0) {
            counter++;
            if (counter >= 300) {
                counter = 0;
                PajauApiPlugin.tilesBuscados.clear();
                return null;
            }
            for (WorldPoint p : PajauApiPlugin.tilesBuscados) {
                LocalPoint lp = LocalPoint.fromWorld(client, p);
                if (lp == null) {
                    return null;
                }
                Polygon poly = Perspective.getCanvasTilePoly(client, lp);
                OverlayUtil.renderPolygon(graphics,poly,Color.red);
            }


        }

        /*if (nexPlugin.getTileBuscados().size() > 0) {
            for (WorldPoint p : nexPlugin.getTileBuscados()) {
                LocalPoint lp = LocalPoint.fromWorld(client, p);
                if (lp == null) {
                    return null;
                }
                Polygon poly = Perspective.getCanvasTilePoly(client, lp);
                OverlayUtil.renderPolygon(graphics,poly,Color.red);
            }
        }*/

        return null;
    }
}
