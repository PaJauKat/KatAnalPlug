//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.hydra;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hydra")
public interface HydraConfig extends Config {
    @ConfigItem(
            position = 1,
            keyName = "attackCounterOverlay",
            name = "Attack Counter Overlay",
            description = "Configures if an attack counter overlay is shown."
    )
    default boolean isAttackCounterOverlay() {
        return true;
    }

    @ConfigItem(
            position = 2,
            keyName = "boldAttackCounterOverlay",
            name = "Bold Attack Counter",
            description = "Configures if the attack counter is <b>bold</b>.<br>Attack Counter Overlay must be enabled."
    )
    default boolean isBoldAttackCounterOverlay() {
        return false;
    }

    @ConfigItem(
            position = 3,
            keyName = "prayerOverlay",
            name = "Prayer Overlay",
            description = "Configures if a prayer overlay is shown.<br>This overlay includes a mini attack counter."
    )
    default boolean isPrayerOverlay() {
        return true;
    }

    @ConfigItem(
            position = 4,
            keyName = "poisonProjectileOverlay",
            name = "Poison Projectile Overlay",
            description = "Configures if a poison projectile overlay is shown."
    )
    default boolean isPoisonOverlay() {
        return true;
    }
}
