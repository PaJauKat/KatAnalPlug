package com.example.blastFurnace;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bfurnace")
public interface BlastFurnaceConfig extends Config {
    @ConfigItem(
            name = "On/Off",
            keyName = "onOff",
            description = "Start/stop button"
    )
    default boolean onOff(){ return false;}

    @ConfigItem(
            name = "Pick bar",
            keyName = "bar",
            description = "Choose a bar"
    )default BarOres barChoosen(){return BarOres.MITHRIL;}

    @ConfigItem(
            name = "Staminas",
            keyName = "staminas",
            description = "Should use Staminas?"
    )default boolean staminas(){return false;}

}
