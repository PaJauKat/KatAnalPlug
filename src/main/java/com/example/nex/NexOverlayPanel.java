package com.example.nex;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class NexOverlayPanel extends OverlayPanel {
    @Inject
    private Client client;

    @Inject
    private NexPlugin plugin;

    @Inject
    NexOverlayPanel(NexPlugin plg, Client clt) {
        this.plugin = plg;
        this.client = clt;

        setPosition(OverlayPosition.BOTTOM_RIGHT);
        setPriority(OverlayPriority.MED);
        this.addMenuEntry(MenuAction.RUNELITE_OVERLAY, "Anal", "Kata", x -> alfa=(alfa+20) %255);
        this.addMenuEntry(MenuAction.RUNELITE_OVERLAY, "Reset", "Color", x -> alfa=20);

    }


    int alfa = 20;
    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.getChildren().add(TitleComponent.builder().text("Nex").color(new Color(205, 0, 0)).build());
        panelComponent.getChildren().add(TitleComponent.builder().text("Kat Plugins").color(new Color(205, 0, 0)).build());
        panelComponent.getChildren().add(LineComponent.builder().left("State: ").right(plugin.getEstado().name()).build());
        panelComponent.setBackgroundColor(new Color(255,0,0,alfa));

        return super.render(graphics);
    }
}
