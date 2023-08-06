package com.example.vardorvis;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.Color;

@ConfigGroup("vardorvisPajau")
public interface VardorvisConfig extends Config {
    @ConfigItem(
            name = "Show Axes path",
            keyName = "axesPath",
            description = ""
    )default boolean axesPath(){ return true;}

    @Alpha
    @ConfigItem(
            name = "Axes path color",
            keyName = "axesColor",
            description = ""
    )default Color axesColor(){return new Color(255,12,67,50);}

    @ConfigItem(
            name = "Range Switch",
            keyName = "rangeSwitch",
            description = ""
    )default boolean rangeSwitch(){return true;}
}
