package com.example.CaTimer;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.inject.Inject;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MuspahTimerPanel extends Overlay {

    @Inject
    private Client client;

    @Inject
    private CaTimerPlugin caTimerPlugin;

    @Getter
    protected PanelComponent panelcito= new PanelComponent();

    @Inject
    public MuspahTimerPanel(CaTimerPlugin plugin){
        this.caTimerPlugin = plugin;
        setPosition(OverlayPosition.BOTTOM_LEFT);
        LineComponent linea = LineComponent.builder().left("Katarina").right("anal").build();
        this.panelcito.getChildren().add(linea);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (caTimerPlugin.inFight || Arrays.stream(client.getMapRegions()).anyMatch(mapId -> mapId == 11330)) {
            //graphics.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,15));
            LineComponent meow = LineComponent.builder().left("Katarina").right(String.valueOf(formatTime(CaTimerPlugin.peleaTicks))).build();
            this.panelcito.getChildren().set(0, meow);

        } else {
            return null;
        }
        return panelcito.render(graphics);
    }

    public String formatTime(int ticks) {
        int millis = ticks * 600;
        return String.format("%2d:%02d:%s",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60,
                String.valueOf(millis%1000).substring(0,1));
    }
}
