package com.example.vampires;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.util.ArrayList;
import java.util.List;

@ConfigGroup("vampiresKat")
public interface VampiresConfig extends Config {

    @ConfigSection(
            name = "Bank",
            description = "",
            position = 1,
            closedByDefault = true
    )String bankSection = "Bank";

    @ConfigSection(
            name = "Potions",
            description = "",
            position = 1,
            closedByDefault = true
    )String potionSection = "Potions";

    @ConfigItem(
            name = "On/Off",
            keyName = "onOff",
            description = ""
    )default boolean onOff(){return false;}

    @ConfigItem(
            name = "Min Value loot",
            keyName = "minValLoot",
            description = ""
    )default int minValLoot(){return 2000;}

    @ConfigItem(
            name = "Recharge pray at",
            keyName = "rechargePP",
            description = ""
    )default int rechargePP(){return 10;}

    @ConfigItem(
            name = "Piety",
            keyName = "piety",
            description = ""
    )default boolean piety() {return false;}

    @ConfigItem(
            name = "Alch",
            keyName = "alch",
            description = ""
    )default boolean alch(){return false;}

    @ConfigItem(
            name = "Gear",
            keyName = "gear",
            description = "",
            section = bankSection
    )default String gear(){return "";}

    @ConfigItem(
            name = "Fetch gear",
            keyName = "fetchGear",
            description = "",
            section = bankSection
    )default boolean fetchGear(){return false;}

    @Getter
    @AllArgsConstructor
    enum StrBoost{
        SUPER_STRENGTH("Super strength*",List.of(ItemID.SUPER_STRENGTH4, ItemID.SUPER_STRENGTH3)),
        DRAGON_BATTLEAXE("Dragon battleaxe",List.of(ItemID.DRAGON_BATTLEAXE)),
        SUPER_COMBAT("Super combat potion*",List.of(ItemID.SUPER_COMBAT_POTION4)),
        STRENGTH_POTION("Strength potion*",List.of(ItemID.SUPER_STRENGTH4)),
        NONE("",new ArrayList<>());

        private final String name;
        private final List<Integer> id;
    }

    @Getter
    @AllArgsConstructor
    enum AtkBoost{
        SUPER_ATTACK("Super attack*",List.of(ItemID.SUPER_ATTACK4)),
        SUPER_COMBAT("Super combat potion",List.of(ItemID.SUPER_COMBAT_POTION4)),
        ATTACK_POTION("Attack potion*",List.of(ItemID.ATTACK_POTION4)),
        NONE("",new ArrayList<>());

        private final String name;
        private final List<Integer> id;
    }


    @ConfigItem(
            name = "Str Boost",
            keyName = "strBoost",
            description = "",
            section = potionSection
    )default StrBoost strBoost(){return StrBoost.NONE;}

    @ConfigItem(
            name = "Str pot amount",
            keyName = "strPotAmount",
            description = "",
            section = potionSection
    )default int strPotAmount(){return 2;}

    @ConfigItem(
            name = "Attack Boost",
            keyName = "atkBoost",
            description = "",
            section = potionSection
    )default AtkBoost atkBoost(){return AtkBoost.NONE;}

    @ConfigItem(
            name = "Atk pot amount",
            keyName = "atkPotAmount",
            description = "",
            section = potionSection
    )default int atkPotAmount(){return 2;}

    @ConfigItem(
            name = "Lvl to reBoost",
            keyName = "lvl2reboost",
            description = "",
            section = potionSection
    )default int lvl2reboost(){return 106;}


}
