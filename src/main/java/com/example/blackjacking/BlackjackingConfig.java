package com.example.blackjacking;

import net.runelite.api.ItemID;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import javax.swing.*;

@ConfigGroup("blackjackANAL")
public interface BlackjackingConfig extends Config {

    @ConfigItem(
            name = "On/Off",
            keyName = "onOff",
            description = "Start/Stop plugin"
    )
    default boolean onOff() {
        return false;
    }

    @ConfigItem(
            name = "Spot",
            keyName = "spot",
            description = "Choose an Empty spot"
    )
    default Spot spot() {
        return Spot.THUG_1;
    }

    @ConfigItem(
            name = "Food ID",
            keyName = "foodID",
            description = ""
    )
    default int foodID() {
        return 1993;
    }

    /*@ConfigItem(
            name = "Min wine",
            keyName = "minWine",
            description = "Min amount of wine to buy from barman"
    )
    default int minWine() {
        return 20;
    }*/

    @ConfigItem(
            name = "ID Remain",
            keyName = "idRemain",
            description = "ID of food remain like jugs from jug of wine"
    )default int idRemain(){ return ItemID.JUG;}

}
