package com.example.GreenDrags;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Gdrags")
public interface GreenDragsConfig extends Config {
    @ConfigItem(
            keyName = "foodName",
            name = "Food Name",
            position = 1,
            description = "Name of food with caps"
    )
    default String foodName(){ return "";}

    @ConfigItem(
            keyName = "foodAmount",
            name = "Cuanta food sacar",
            description = "Cuanta food sacar",
            position = 2
    )
    default int foodAmout(){ return 20;}

}
