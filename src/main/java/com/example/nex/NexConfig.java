package com.example.nex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.util.List;

@ConfigGroup("nexAnal")
public interface NexConfig extends Config {

    @ConfigSection(
            name = "Melee",
            description = "Melee gear",
            position = 80,
            closedByDefault = true
    )
    String meleeGear = "Melee";

    @ConfigSection(
            name = "Ranged",
            description = "Ranged gear",
            position = 90,
            closedByDefault = true
    )
    String rangedGear = "Ranged";

    @ConfigSection(
            name = "Spec",
            description = "Spec options",
            position = 100,
            closedByDefault = true
    )
    String specGear = "Spec";

    @ConfigSection(
            name = "KCing",
            description = "Option when Kcing",
            position = 110,
            closedByDefault = true
    )
    String kcSection = "KCing";

    @ConfigSection(
            name = "Potions",
            description = "Potions options",
            position = 70,
            closedByDefault = true
    )
    String potionSection = "Potions";

    @ConfigSection(
            name = "Bank",
            description = "Bank options",
            position = 60,
            closedByDefault = true
    )
    String bankSection = "Bank";

    @ConfigSection(
            name = "Bank Pre-Pot",
            description = "Prepot Options",
            position = 65,
            closedByDefault = true
    )String prePotSection = "Bank Pre-Pot";

    //---------------------------------------------------------------------
    @ConfigItem(
            name = "On/Off",
            keyName = "onOff",
            description = "Start/stop button",
            position = -100
    )
    default boolean onOff() {
        return false;
    }

    @ConfigItem(
            name = "Wait for Master",
            keyName = "waitForMaster",
            description = "It will enter if master is at portal"
    )default boolean waitForMaster(){return true;}

    @ConfigItem(
            name = "Master name",
            keyName = "masterName",
            description = ""
    )default String masterName(){return "Putita";}
    //BANK SECTION---------------------------------------------------------
    @ConfigItem(
            name = "Bank anal",
            keyName = "bankAnal",
            description = "",
            section = bankSection
    )default boolean bankAnal(){ return false;}

    @ConfigItem(
            name = "Restore amount",
            keyName = "restoreAmount",
            description = "",
            section = bankSection
    )default int restoreAmount(){return 5;}

    @ConfigItem(
            name = "Saradomin brew amount",
            keyName = "saraAmount",
            description = "",
            section = bankSection
    )default int saraAmount(){return 13;}

    @ConfigItem(
            name = "Ranging amount",
            keyName = "rangAmount",
            description = "",
            section = bankSection
    )default int rangAmount(){return 1;}

    @ConfigItem(
            name = "Combat amount",
            keyName = "combatAmount",
            description = "",
            section = bankSection
    )default int combatAmount(){return 2;}

    //Pre-Pot section--------------------------------------

    @ConfigItem(
            name = "Imbued Heart",
            keyName = "heart",
            description = "",
            section = prePotSection
    )default boolean heart(){return true;}
    @ConfigItem(
            name = "S combat",
            keyName = "combatPre",
            description = "",
            section = prePotSection
    )default boolean combatPre(){return true;}
    @ConfigItem(
            name = "Ranging pot",
            keyName = "rangPre",
            description = "",
            section = prePotSection
    )default boolean rangPre(){return true;}
    @ConfigItem(
            name = "Angler",
            keyName = "anglerPre",
            description = "",
            section = prePotSection
    )default boolean anglerPre(){return true;}
    @ConfigItem(
            name = "Anti-Poison",
            keyName = "antiPre",
            description = "",
            section = prePotSection
    )default boolean antiPre(){return true;}

    @ConfigItem(
            name = "Menaphite",
            keyName = "menaphitePre",
            description = "",
            section = prePotSection
    )default boolean menaphitePre(){return true;}



    //POTION SECTION-------------------------------------------------------



    @ConfigItem(
            name = "Heal at X percent",
            keyName = "hpTriggerHeal",
            description = "",
            section = potionSection
    )
    default int hpTriggerHeal() {
        return 40;
    }

    @ConfigItem(
            name = "Heal stop healing",
            keyName = "hpStopHealing",
            description = "",
            section = potionSection
    )
    default int hpStopHealing() {
        return 99;
    }

    @ConfigItem(
            name = "Food Name",
            keyName = "foodName",
            description = "",
            section = potionSection
    )
    default String foodName() {
        return "Saradomin brew*";
    }

    @ConfigItem(
            name = "Prayer threshold",
            keyName = "prayThreshold",
            description = "",
            section = potionSection
    )
    default int prayThreshold() {
        return 15;
    }

    @ConfigItem(
            name = "Prayer restore name",
            keyName = "prayRestorationName",
            description = "",
            section = potionSection
    )
    default String prayRestorationName() {
        return "Super restore*";
    }

    @ConfigItem(
            name = "Level to reboost",
            keyName = "lvlReboost",
            description = "",
            section = potionSection
    )
    default int lvlReboost() {
        return 112;
    }


    //Ranged SECTION-------------------------------------------------------

    @ConfigItem(
            name = "Fetch gear ranged",
            keyName = "fetchGearRanged",
            description = "",
            section = rangedGear,
            position = 2
    )
    default boolean fetchGearRanged() {
        return false;
    }

    @ConfigItem(
            name = "Ranged gear",
            keyName = "rangedGear",
            description = "",
            section = rangedGear,
            position = 1
    )
    default String rangedGear() {
        return "";
    }


    @Getter
    @AllArgsConstructor
    enum RangeWep {
        TEN("10", 10, List.of(new WorldPoint(2927, 5214, 0), new WorldPoint(2936, 5205, 0))),
        EIGHT("8", 8, List.of(new WorldPoint(2929, 5214, 0), new WorldPoint(2936, 5207, 0))),
        SEVEN("7", 7, List.of(new WorldPoint(2930, 5214, 0), new WorldPoint(2936, 5208, 0))),
        ;

        private final String inString;
        private final int distant;
        private final List<WorldPoint> tilesUmbra;

        @Override
        public String toString() {
            return String.valueOf(distant);
        }
    }

    @ConfigItem(
            name = "Range distant",
            keyName = "rangeDistant",
            description = "Ranged weapong range",
            section = rangedGear,
            position = 3
    )
    default RangeWep rangeDistant() {
        return RangeWep.TEN;
    }


    //Melee SECTION-------------------------------------------------------
    @ConfigItem(
            name = "Fetch gear melee",
            keyName = "fetchGearMelee",
            description = "",
            section = meleeGear,
            position = 2
    )
    default boolean fetchGearMelee() {
        return false;
    }

    @ConfigItem(
            name = "Melee Gear",
            keyName = "meleeGear",
            description = "",
            section = meleeGear,
            position = 1
    )
    default String meleeGear() {
        return "";
    }

    //Spec SECTION-------------------------------------------------------
    @ConfigItem(
            name = "Spec weapon ID",
            keyName = "specID",
            description = "",
            section = specGear,
            position = 6
    )
    default int specID() {
        return 0;
    }

    @ConfigItem(
            name = "Spec percent",
            keyName = "specPercent",
            description = "",
            section = specGear,
            position = 3
    )
    default int specPercent() {
        return 75;
    }

    @ConfigItem(
            name = "Spec Gear",
            keyName = "specGear",
            description = "",
            section = specGear,
            position = 1
    )
    default String specGear() {
        return "";
    }

    @ConfigItem(
            name = "Fetch Spec Gear",
            keyName = "fetchSpecGear",
            description = "",
            section = specGear,
            position = 2
    )
    default boolean fetchSpecGear() {
        return false;
    }

    @Getter
    @AllArgsConstructor
    enum Style {
        RANGED(Skill.RANGED, Prayer.RIGOUR),
        MELEE(Skill.STRENGTH, Prayer.PIETY);
        private final Skill skill;
        private final Prayer prayer;
    }

    @ConfigItem(
            name = "Spec Style",
            keyName = "specStyle",
            description = "",
            section = specGear,
            position = 4
    )
    default Style specStyle() {
        return Style.RANGED;
    }

    @ConfigItem(
            name = "Spec P2",
            keyName = "specP2",
            description = "",
            section = specGear,
            position = 5
    )
    default boolean specP2() {
        return true;
    }

    @Getter
    @AllArgsConstructor
    enum Players {
        A(1, new WorldPoint(2923, 5203, 0), new WorldPoint(2921, 5213, 0), new WorldPoint(2935, 5199, 0), new WorldPoint(2921, 5193, 0)),
        B(2, new WorldPoint(2925, 5205, 0), new WorldPoint(2921, 5215, 0), new WorldPoint(2937, 5199, 0), new WorldPoint(2921, 5191, 0)),
        C(3, new WorldPoint(2927, 5203, 0), new WorldPoint(2923, 5215, 0), new WorldPoint(2937, 5201, 0), new WorldPoint(2923, 5191, 0)),
        D(4, new WorldPoint(2925, 5201, 0), new WorldPoint(2923, 5213, 0), new WorldPoint(2935, 5201, 0), new WorldPoint(2923, 5193, 0));

        private final int position;
        private final WorldPoint P1Tile;
        private final WorldPoint FTile;
        private final WorldPoint CTile;
        private final WorldPoint GTile;
    }

    @ConfigItem(
            name = "Player",
            keyName = "playerKey",
            description = ""
    )
    default Players playerKey() {
        return Players.A;
    }


    @ConfigItem(
            name = "Zaros Item id",
            keyName = "zarosItemId",
            description = ""
    )
    default int zarosItemId() {
        return 26235;
    }

    //Kc SECTION-------------------------------------------------------
    @ConfigItem(
            name = "Fetch KC Gear",
            keyName = "fetchKcGear",
            description = "",
            section = kcSection,
            position = 2
    )
    default boolean fetchKcGear() {
        return false;
    }

    @ConfigItem(
            name = "KC Gear",
            keyName = "kcGear",
            description = "",
            section = kcSection,
            position = 1
    )
    default String kcGear() {
        return "";
    }

    @ConfigItem(
            name = "Lvl reBoost",
            keyName = "kcLvlReBoost",
            description = "Lvl to reBoost",
            section = kcSection
    )default int kcLvlReBoost(){return 106;}

    @ConfigItem(
            name = "Style for KCing",
            keyName = "kcStyle",
            description = "",
            section = kcSection
    )default Style kcStyle(){return Style.RANGED;}


    enum modo {
        NEX,
        KC;
    }

    @ConfigItem(
            name = "Mode",
            keyName = "mode",
            description = "",
            position = -10
    )default modo mode(){return modo.NEX;}



}
