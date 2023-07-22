package com.example.cooker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("cooker")
public interface CookerConfig extends Config {

    @ConfigItem(
            name = "Id raw",
            keyName = "id",
            description = "Id of raw version"
    )default int id(){return 363;}

    public enum bank{
        NPC,
        GAME_OBJECT
    }
    @ConfigItem(
            name = "Bank type",
            keyName = "bankType",
            description = ""
    )default bank bankType(){return bank.NPC;}

    @ConfigItem(
            name = "Bank Id",
            keyName = "bankID",
            description = ""
    )default int bankID(){return 3194;}

    @ConfigItem(
            name = "Bank object Option",
            keyName = "bankOp",
            description = "Which option on the object is bank"
    )default int bankOp(){return 3;}

    @ConfigItem(
            name = "Fire or Range Id",
            keyName = "fireID",
            description = "ID of Object used for cooking"
    )default int fireID() {return 43475;}

    @ConfigItem(
            name = "On/Off",
            keyName = "onOff",
            description = "Start/Stop plugin"
    )default boolean onOff(){return false;}
}
