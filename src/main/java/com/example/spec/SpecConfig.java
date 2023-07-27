package com.example.spec;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("specPlug")
public interface SpecConfig extends Config {
    @ConfigItem(
            name = "Key spec",
            keyName = "keySpec",
            description = ""
    )default Keybind keySpec(){return Keybind.NOT_SET;}
}
