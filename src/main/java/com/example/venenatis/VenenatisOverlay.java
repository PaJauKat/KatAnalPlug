package com.example.venenatis;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;

public class VenenatisOverlay extends Overlay {
    @Inject
    private Client client;

    @Inject
    private VenenatisPlugin plugin;

    @Inject
    VenenatisOverlay(Client ct,VenenatisPlugin plg) {
        this.plugin = plg;
        this.client = ct;
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);
        setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (plugin.getVenenatis() != null) {
            if (plugin.getVenenatis().isDead()) return null;
            graphics.setFont(new Font("Arial",Font.BOLD,24));
            String texto;
            Color colorcito;
            if (plugin.getMoveCounter() == 3) {
                texto = "Web";
                colorcito = Color.orange;
            } else {
                texto = String.valueOf(plugin.getMoveCounter());
                colorcito = Color.WHITE;
            }

            Point punto = Perspective.getCanvasTextLocation(client,graphics,plugin.getVenenatis().getLocalLocation(), texto,0);
            OverlayUtil.renderTextLocation(graphics,punto,String.valueOf(plugin.getMoveCounter()),colorcito);
        }
        return null;
    }
}
