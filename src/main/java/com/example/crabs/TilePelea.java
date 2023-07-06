package com.example.crabs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
//new WorldPoint(1771, 3473, 0)
@Getter
@AllArgsConstructor
public enum TilePelea {
    CRABS_3(List.of(new WorldPoint(1776,3468,0), new WorldPoint(1773,3461,0) ) ),
    CRABS_2(List.of(new WorldPoint(1774,3470,0)) ),
    CRABS_4(List.of(new WorldPoint(1765,3468,0)))
    ;

    private final List<WorldPoint> puntos;

}
