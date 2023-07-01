//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.alchemicalhydra.overlay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.example.alchemicalhydra.AlchemicalHydraConfig;
import com.example.alchemicalhydra.AlchemicalHydraPlugin;
import com.example.alchemicalhydra.entity.Hydra;
import com.example.alchemicalhydra.entity.HydraPhase;
import net.runelite.api.Client;
import net.runelite.api.IndexDataBase;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.api.SpritePixels;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.InfoBoxComponent;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.util.ImageUtil;

@Singleton
public class AttackOverlay extends Overlay {
    public static final int IMAGE_SIZE = 36;
    private static final String INFO_BOX_TEXT_PADDING = "        ";
    private static final Dimension INFO_BOX_DIMENSION = new Dimension(40, 40);
    private static final PanelComponent panelComponent = new PanelComponent();
    private static final InfoBoxComponent stunComponent = new InfoBoxComponent();
    private static final InfoBoxComponent phaseSpecialComponent = new InfoBoxComponent();
    private static final InfoBoxComponent prayerComponent = new InfoBoxComponent();
    private static final int STUN_TICK_DURATION = 7;
    private final Client client;
    private final AlchemicalHydraPlugin plugin;
    private final AlchemicalHydraConfig config;
    private final SpriteManager spriteManager;
    private int stunTicks;
    private Hydra hydra;

    @Inject
    AttackOverlay(Client client, AlchemicalHydraPlugin plugin, AlchemicalHydraConfig config, SpriteManager spriteManager) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.spriteManager = spriteManager;
        stunComponent.setBackgroundColor(config.dangerColor());
        stunComponent.setImage(this.createStunImage());
        this.setPriority(OverlayPriority.HIGH);
        this.setPosition(OverlayPosition.BOTTOM_RIGHT);
        this.setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    public Dimension render(Graphics2D graphics2D) {
        this.hydra = this.plugin.getHydra();
        if (this.hydra == null) {
            return null;
        } else {
            this.clearPanelComponent();
            this.updateStunComponent();
            this.updatePhaseSpecialComponent();
            if (this.config.hidePrayerOnSpecial() && this.isSpecialAttack()) {
                return panelComponent.render(graphics2D);
            } else {
                this.updatePrayerComponent();
                this.renderPrayerWidget(graphics2D);
                return panelComponent.render(graphics2D);
            }
        }
    }

    public void decrementStunTicks() {
        if (this.stunTicks > 0) {
            --this.stunTicks;
        }

    }

    public void setStunTicks() {
        this.stunTicks = 7;
    }

    private void clearPanelComponent() {
        List<LayoutableRenderableEntity> children = panelComponent.getChildren();
        if (!children.isEmpty()) {
            children.clear();
        }

    }

    private void updateStunComponent() {
        if (this.stunTicks > 0) {
            stunComponent.setText("        " + this.stunTicks);
            panelComponent.getChildren().add(stunComponent);
        }
    }

    private void updatePhaseSpecialComponent() {
        int nextSpec = this.hydra.getNextSpecialRelative();
        if (nextSpec <= 3 && nextSpec >= 0) {
            if (nextSpec == 0) {
                phaseSpecialComponent.setBackgroundColor(this.config.dangerColor());
            } else if (nextSpec == 1) {
                phaseSpecialComponent.setBackgroundColor(this.config.warningColor());
            } else {
                phaseSpecialComponent.setBackgroundColor(ComponentConstants.STANDARD_BACKGROUND_COLOR);
            }

            phaseSpecialComponent.setImage(this.hydra.getPhase().getSpecialImage(this.spriteManager));
            phaseSpecialComponent.setText("        " + nextSpec);
            panelComponent.getChildren().add(phaseSpecialComponent);
        }
    }

    private void updatePrayerComponent() {
        Prayer nextPrayer = this.hydra.getNextAttack().getPrayer();
        int nextSwitch = this.hydra.getNextSwitch();
        if (nextSwitch == 1) {
            prayerComponent.setBackgroundColor(this.client.isPrayerActive(nextPrayer) ? this.config.warningColor() : this.config.dangerColor());
        } else {
            prayerComponent.setBackgroundColor(this.client.isPrayerActive(nextPrayer) ? this.config.safeColor() : this.config.dangerColor());
        }

        prayerComponent.setImage(this.hydra.getNextAttack().getImage(this.spriteManager));
        prayerComponent.setText("        " + nextSwitch);
        panelComponent.getChildren().add(prayerComponent);
    }

    private void renderPrayerWidget(Graphics2D graphics2D) {
        Prayer prayer = this.hydra.getNextAttack().getPrayer();
        OverlayUtil.renderPrayerOverlay(graphics2D, this.client, prayer, prayer == Prayer.PROTECT_FROM_MAGIC ? Color.CYAN : Color.GREEN);
    }

    private boolean isSpecialAttack() {
        HydraPhase phase = this.hydra.getPhase();
        switch (phase) {
            case FLAME:
                NPC npc = this.hydra.getNpc();
                return this.hydra.getNextSpecialRelative() == 0 || npc != null && npc.getInteracting() == null;
            case POISON:
            case LIGHTNING:
            case ENRAGED:
                return this.hydra.getNextSpecialRelative() == 0;
            default:
                return false;
        }
    }

    private BufferedImage createStunImage() {
        SpritePixels root = this.getSprite(1788);
        SpritePixels mark = this.getSprite(937);
        if (mark != null && root != null) {
            SpritePixels sprite = ImageUtil.getImageSpritePixels(root.toBufferedImage(),this.client);
            return sprite.toBufferedImage();
        } else {
            return null;
        }
    }

    private SpritePixels getSprite(int spriteId) {
        IndexDataBase spriteDatabase = this.client.getIndexSprites();
        if (spriteDatabase == null) {
            return null;
        } else {
            SpritePixels[] sprites = this.client.getSprites(spriteDatabase, spriteId, 0);
            return sprites == null ? null : sprites[0];
        }
    }

    static {
        panelComponent.setOrientation(ComponentOrientation.VERTICAL);
        panelComponent.setBorder(new Rectangle(0, 0, 0, 0));
        panelComponent.setPreferredSize(new Dimension(40, 0));
        stunComponent.setPreferredSize(INFO_BOX_DIMENSION);
        phaseSpecialComponent.setPreferredSize(INFO_BOX_DIMENSION);
        prayerComponent.setPreferredSize(INFO_BOX_DIMENSION);
    }
}
