package com.example.fungus;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("fungus")
public interface FungusConfig extends Config {
    @ConfigItem(
            name = "On/Off",
            keyName = "onOff",
            description = "Start button"
    )default boolean onOff() {return false;}
}
