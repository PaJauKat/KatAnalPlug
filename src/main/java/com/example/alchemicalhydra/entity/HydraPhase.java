//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.alchemicalhydra.entity;

import java.awt.Color;
import java.awt.image.BufferedImage;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

public enum HydraPhase {
    POISON(3, 8237, 8238, 1644, 0, 825, 1774, new WorldPoint(1371, 10263, 0), Color.GREEN, Color.RED),
    LIGHTNING(3, 8244, 8245, 0, 8241, 550, 1959, new WorldPoint(1371, 10272, 0), Color.CYAN, Color.GREEN),
    FLAME(3, 8251, 8252, 0, 8248, 275, 1800, new WorldPoint(1362, 10272, 0), Color.RED, Color.CYAN),
    ENRAGED(1, 8257, 8258, 1644, 0, 0, 1774, (WorldPoint)null, (Color)null, (Color)null);

    private final int attacksPerSwitch;
    private final int deathAnimation1;
    private final int deathAnimation2;
    private final int specialProjectileId;
    private final int specialAnimationId;
    private final int hpThreshold;
    private final int spriteId;
    private final WorldPoint fountainWorldPoint;
    private final Color phaseColor;
    private final Color fountainColor;
    private BufferedImage specialImage;

    public BufferedImage getSpecialImage(SpriteManager spriteManager) {
        if (this.specialImage == null) {
            BufferedImage tmp = spriteManager.getSprite(this.spriteId, 0);
            this.specialImage = tmp == null ? null : ImageUtil.resizeImage(tmp, 36, 36);
        }

        return this.specialImage;
    }

    public int getAttacksPerSwitch() {
        return this.attacksPerSwitch;
    }

    public int getDeathAnimation1() {
        return this.deathAnimation1;
    }

    public int getDeathAnimation2() {
        return this.deathAnimation2;
    }

    public int getSpecialProjectileId() {
        return this.specialProjectileId;
    }

    public int getSpecialAnimationId() {
        return this.specialAnimationId;
    }

    public int getHpThreshold() {
        return this.hpThreshold;
    }

    public WorldPoint getFountainWorldPoint() {
        return this.fountainWorldPoint;
    }

    public Color getPhaseColor() {
        return this.phaseColor;
    }

    public Color getFountainColor() {
        return this.fountainColor;
    }

    public BufferedImage getSpecialImage() {
        return this.specialImage;
    }

    private HydraPhase(int attacksPerSwitch, int deathAnimation1, int deathAnimation2, int specialProjectileId, int specialAnimationId, int hpThreshold, int spriteId, WorldPoint fountainWorldPoint, Color phaseColor, Color fountainColor) {
        this.attacksPerSwitch = attacksPerSwitch;
        this.deathAnimation1 = deathAnimation1;
        this.deathAnimation2 = deathAnimation2;
        this.specialProjectileId = specialProjectileId;
        this.specialAnimationId = specialAnimationId;
        this.hpThreshold = hpThreshold;
        this.spriteId = spriteId;
        this.fountainWorldPoint = fountainWorldPoint;
        this.phaseColor = phaseColor;
        this.fountainColor = fountainColor;
    }
}
