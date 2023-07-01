//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.hydra;

import java.awt.Color;
import java.util.Objects;

public enum HydraAnimation {
    RANGE(8261, "RANGE", new Color(0, 255, 0)),
    MAGIC(8262, "MAGIC", new Color(52, 152, 219)),
    POISON(8263, "POISON", new Color(255, 0, 0));

    private final int id;
    private final String text;
    private final Color color;

    public static HydraAnimation fromId(int id) {
        HydraAnimation[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            HydraAnimation hydraAnimation = var1[var3];
            if (Objects.equals(hydraAnimation.id, id)) {
                return hydraAnimation;
            }
        }

        throw new IllegalArgumentException();
    }

    private HydraAnimation(int id, String text, Color color) {
        this.id = id;
        this.text = text;
        this.color = color;
    }

    int getId() {
        return this.id;
    }

    String getText() {
        return this.text;
    }

    Color getColor() {
        return this.color;
    }
}
