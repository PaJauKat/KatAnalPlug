//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.alchemicalhydra.entity;

import java.awt.image.BufferedImage;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

@Singleton
public class Hydra {
    private static final int MAX_HP = 1100;
    @Nullable
    private final NPC npc;
    private HydraPhase phase;
    private AttackStyle nextAttack;
    private AttackStyle lastAttack;
    private boolean immunity;
    private int nextSpecial;
    private int attackCount;
    private int nextSwitch;

    public void setNextSpecial() {
        this.nextSpecial += 9;
    }

    public int getNextSpecialRelative() {
        return this.nextSpecial - this.attackCount;
    }

    public void changePhase(HydraPhase hydraPhase) {
        this.phase = hydraPhase;
        this.nextSpecial = 3;
        this.attackCount = 0;
        this.immunity = true;
        if (hydraPhase == HydraPhase.ENRAGED) {
            this.immunity = false;
            this.switchStyles();
            this.nextSwitch = this.phase.getAttacksPerSwitch();
        }

    }

    public void handleProjectile(int projectileId) {
        if (projectileId != this.nextAttack.getProjectileID()) {
            if (projectileId == this.lastAttack.getProjectileID()) {
                return;
            }

            this.switchStyles();
            this.nextSwitch = this.phase.getAttacksPerSwitch() - 1;
        } else {
            --this.nextSwitch;
        }

        this.lastAttack = this.nextAttack;
        ++this.attackCount;
        if (this.nextSwitch <= 0) {
            this.switchStyles();
            this.nextSwitch = this.phase.getAttacksPerSwitch();
        }

    }

    public int getHpUntilPhaseChange() {
        return Math.max(0, this.getHp() - this.phase.getHpThreshold());
    }

    private void switchStyles() {
        this.nextAttack = this.lastAttack == Hydra.AttackStyle.MAGIC ? Hydra.AttackStyle.RANGED : Hydra.AttackStyle.MAGIC;
    }

    private int getHp() {
        int ratio = this.npc.getHealthRatio();
        int health = this.npc.getHealthScale();
        if (ratio >= 0 && health > 0) {
            int exactHealth = 0;
            if (ratio > 0) {
                int minHealth = 1;
                int maxHealth;
                if (health > 1) {
                    if (ratio > 1) {
                        minHealth = (1100 * (ratio - 1) + health - 2) / (health - 1);
                    }

                    maxHealth = (1100 * ratio - 1) / (health - 1);
                    if (maxHealth > 1100) {
                        maxHealth = 1100;
                    }
                } else {
                    maxHealth = 1100;
                }

                exactHealth = (minHealth + maxHealth + 1) / 2;
            }

            return exactHealth;
        } else {
            return -1;
        }
    }

    @Nullable
    public NPC getNpc() {
        return this.npc;
    }

    public HydraPhase getPhase() {
        return this.phase;
    }

    public AttackStyle getNextAttack() {
        return this.nextAttack;
    }

    public AttackStyle getLastAttack() {
        return this.lastAttack;
    }

    public boolean isImmunity() {
        return this.immunity;
    }

    public int getNextSpecial() {
        return this.nextSpecial;
    }

    public int getAttackCount() {
        return this.attackCount;
    }

    public int getNextSwitch() {
        return this.nextSwitch;
    }

    public Hydra(@Nullable NPC npc) {
        this.phase = HydraPhase.POISON;
        this.nextAttack = Hydra.AttackStyle.MAGIC;
        this.lastAttack = Hydra.AttackStyle.MAGIC;
        this.immunity = true;
        this.nextSpecial = 3;
        this.nextSwitch = this.phase.getAttacksPerSwitch();
        this.npc = npc;
    }

    public void setImmunity(boolean immunity) {
        this.immunity = immunity;
    }

    public static enum AttackStyle {
        MAGIC(1662, Prayer.PROTECT_FROM_MAGIC, 127),
        RANGED(1663, Prayer.PROTECT_FROM_MISSILES, 128);

        private final int projectileID;
        private final Prayer prayer;
        private final int spriteID;
        private BufferedImage image;

        public BufferedImage getImage(SpriteManager spriteManager) {
            if (this.image == null) {
                BufferedImage tmp = spriteManager.getSprite(this.spriteID, 0);
                this.image = tmp == null ? null : ImageUtil.resizeImage(tmp, 36, 36);
            }

            return this.image;
        }

        public int getProjectileID() {
            return this.projectileID;
        }

        public Prayer getPrayer() {
            return this.prayer;
        }

        public int getSpriteID() {
            return this.spriteID;
        }

        private AttackStyle(int projectileID, Prayer prayer, int spriteID) {
            this.projectileID = projectileID;
            this.prayer = prayer;
            this.spriteID = spriteID;
        }
    }
}
