//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.hydra;

import java.awt.Graphics2D;
import net.runelite.api.NPC;
import net.runelite.api.Point;

public class Hydra {
    static final int MAX_ATTACK_COUNT = 3;
    private final NPC npc;
    private int attackCount;
    private HydraAnimation hydraAnimation;

    public Hydra(NPC npc) {
        this.npc = npc;
        this.attackCount = 3;
        this.hydraAnimation = null;
    }

    void updateAttackCount() {
        this.attackCount = this.attackCount == 1 ? 3 : --this.attackCount;
    }

    void resetAttackCount() {
        this.attackCount = 3;
    }

    Point getCanvasTextLocation(Graphics2D graphics, String text, int zOffset) {
        return this.npc.getCanvasTextLocation(graphics, text, zOffset);
    }

    int getLogicalHeight() {
        return this.npc.getLogicalHeight();
    }

    int getAttackCount() {
        return this.attackCount;
    }

    HydraAnimation getHydraAnimation() {
        return this.hydraAnimation;
    }

    void setHydraAnimation(HydraAnimation hydraAnimation) {
        this.hydraAnimation = hydraAnimation;
    }
}
