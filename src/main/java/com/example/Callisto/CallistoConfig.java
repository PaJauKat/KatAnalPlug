package com.example.Callisto;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("CallistoAnal")
public interface CallistoConfig extends Config {
    @ConfigItem(
            name = "MoveBarrage delay(tick)",
            keyName = "barrageTimeout",
            description = ""
    )
    default int barrageTimeout() {
        return 1;
    }

    @ConfigItem(
            name = "GrrBarrage delay(tick)",
            keyName = "grrBarrageTimeout",
            description = ""
    )
    default int grrBarrageTimeout() {
        return 3;
    }

    @ConfigItem(
            name = "Rigour",
            keyName = "actRigour",
            description = ""
    )default boolean actRigour() {
        return true;
    }

}
