package com.example.specBar;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("specbar")
public interface SpecBarConfig extends Config
{
    @ConfigItem(
            position = 2,
            keyName = "specbarid",
            name = "Spec bar widget id",
            description = "Configures the id for the specbar widget since it can change after update"
    )
    default int specbarid()
    {
        return 35;
    }

    @ConfigItem(
            position = 3,
            keyName = "alwaysOn",
            name = "Always On",
            description = "Enable this if you want the spec bar to always show, not just when it needs to"
    )
    default boolean alwaysOn()
    {
        return false;
    }
}