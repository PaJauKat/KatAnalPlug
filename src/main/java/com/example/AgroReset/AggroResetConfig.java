package com.example.AgroReset;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("agroReset")
public interface AggroResetConfig extends Config {
    @ConfigItem(
            name = "On/Off",
            keyName = "onOff",
            description = "Start/stop button"
    )
    default boolean onOff(){ return false;}
}
