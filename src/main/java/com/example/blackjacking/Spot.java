package com.example.blackjacking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

@AllArgsConstructor
@Getter
public enum Spot {
    BANDIT_1(
            "Bandit",
            new WorldArea(3363,3000,2,3,0),
            new WorldPoint(3364,2999,0),
            new WorldPoint[]{
                    new WorldPoint(3364,2999,0),
                    new WorldPoint(3362, 2987, 0),
                    new WorldPoint(3362,2973,0),
                    new WorldPoint(3359,2959,0)
            }
            ),
    THUG_1("Menaphite Thug",
            new WorldArea(3340, 2953, 5, 4, 0),
            new WorldPoint(3345,2955,0),
            new WorldPoint[]{
                    new WorldPoint(3345,2955,0),
                    new WorldPoint(3359,2959,0)
            });
    private final String name;
    private final WorldArea place;
    private final WorldPoint door;
    private final WorldPoint[] path2bank;


}
