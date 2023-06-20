package com.example.worldFinder;

import net.runelite.api.Client;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("WorldFinder")
public interface WorldFinderConfig extends Config {
    @ConfigItem(
            name = "Key ON/OFF",
            keyName = "prendido",
            description = "Key for ON/OFF"
    ) default Keybind prendido(){ return Keybind.NOT_SET; }
}
