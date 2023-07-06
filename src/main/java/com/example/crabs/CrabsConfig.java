package com.example.crabs;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("crabs")
public interface CrabsConfig extends Config {

    @ConfigItem(
            name = "On/Off",
            keyName = "onOff",
            description = "Start/stop button"
    )
    default boolean onOff() {
        return false;
    }

    enum nCrabs {
        CRAB_1("1 Crab"),
        CRAB_2("2 Crabs"),
        CRAB_3("3 Crabs"),
        CRAB_4("4 Crabs")
        ;

        private final String texto;

        nCrabs(String texto) {
            this.texto = texto;
        }

        @Override
        public String toString() {
            return this.texto;
        }
    }

    @ConfigItem(
            name = "Spot",
            keyName = "spot",
            description = "Choose spot"
    )default TilePelea spot(){return TilePelea.CRABS_3;}

    @ConfigItem(
            name = "Evade crashers",
            keyName = "evadeCrashers",
            description = "It will hop or change spot if crasher"
    )default boolean evadeCrashers(){return false;}



}
