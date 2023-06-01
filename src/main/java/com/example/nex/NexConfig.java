package com.example.nex;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("nexito")
public interface NexConfig extends Config {
    @ConfigItem(
            name = "Heal at X percent",
            keyName = "hpPercentHeal",
            description = ""
    )default int hpPercentHeal(){ return 60; }

    @ConfigItem(
            name = "Food Name",
            keyName = "foodName",
            description = ""
    )default String foodName(){return "Saradomin brew*";}

    @ConfigItem(
            name = "Prayer threshold",
            keyName = "prayThreshold",
            description = ""
    )default int prayThreshold(){return 15;}

    @ConfigItem(
            name = "Prayer restore name",
            keyName = "prayRestorationName",
            description = ""
    )default String prayRestorationName(){return "Prayer potion(4)";};

}
