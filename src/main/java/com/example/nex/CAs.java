package com.example.nex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.Varbits;

@AllArgsConstructor
@Getter
public enum CAs {
    HARD(35, Varbits.COMBAT_ACHIEVEMENT_TIER_HARD),
    ELITE(30,Varbits.COMBAT_ACHIEVEMENT_TIER_ELITE),
    MASTER(25,Varbits.COMBAT_ACHIEVEMENT_TIER_MASTER),
    GRAND_MASTER(15,Varbits.COMBAT_ACHIEVEMENT_TIER_GRANDMASTER);

    private final int kcNeeded;
    private final int varbit;

}
