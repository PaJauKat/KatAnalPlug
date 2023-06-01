package com.example.Robador;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Robador")
public interface RobadorConfig extends Config {

    @ConfigItem(
            keyName = "foodId",
            name = "Food ID",
            position = 1,
            description = "Food ID"
    )
    default int foodId(){ return 379;}

    @ConfigItem(
            keyName = "HpComer",
            name = "HP a la cual comer",
            description = "",
            position = 2
    )
    default int HpComer(){return 19;}



}
