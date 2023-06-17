package com.example.venenatis;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.*;
import org.pushingpixels.substance.internal.fonts.Fonts;

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

            Point punto = Perspective.getCanvasTextLocation(client,graphics,plugin.getVenenatis().getLocalLocation(), String.valueOf(plugin.getMoveCounter()),0);
            OverlayUtil.renderTextLocation(graphics,punto,String.valueOf(plugin.getMoveCounter()),Color.WHITE);
        }
        return null;
    }
}
