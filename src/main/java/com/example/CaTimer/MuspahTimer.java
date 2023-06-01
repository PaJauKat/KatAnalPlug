package com.example.CaTimer;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.Timer;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

class MuspahTimer extends Timer {
    private final CaTimerPlugin plugin;
    public MuspahTimer(Duration duration, BufferedImage image, CaTimerPlugin plugin) {
        super(duration.toMillis(), ChronoUnit.MILLIS, image, plugin);
        setTooltip("Time since monster spawned meow");
        this.plugin=plugin;
    }

    @Override
    public boolean render() {
        return super.render() && (plugin.muspah != null);
    }
}
