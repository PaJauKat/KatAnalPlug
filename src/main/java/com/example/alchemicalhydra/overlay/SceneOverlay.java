//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.alchemicalhydra.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.example.alchemicalhydra.AlchemicalHydraConfig;
import com.example.alchemicalhydra.AlchemicalHydraPlugin;
import com.example.alchemicalhydra.entity.Hydra;
import com.example.alchemicalhydra.entity.HydraPhase;
import net.runelite.api.Client;
import net.runelite.api.Deque;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

@Singleton
public class SceneOverlay extends Overlay {
    private static final int LIGHTNING_ID = 1666;
    private static final Area POISON_AREA = new Area();
    private static final int POISON_AOE_AREA_SIZE = 3;
    private static final int HYDRA_HULL_OUTLINE_STROKE_SIZE = 1;
    private final Client client;
    private final AlchemicalHydraPlugin plugin;
    private final AlchemicalHydraConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private Hydra hydra;

    @Inject
    public SceneOverlay(Client client, AlchemicalHydraPlugin plugin, AlchemicalHydraConfig config, ModelOutlineRenderer modelOutlineRenderer) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.setPriority(OverlayPriority.HIGH);
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    public Dimension render(Graphics2D graphics2D) {
        this.hydra = this.plugin.getHydra();
        if (this.hydra == null) {
            return null;
        } else {
            this.renderHpUntilPhaseChange(graphics2D);
            this.renderHydraImmunityOutline();
            this.renderPoisonProjectileAreaTiles(graphics2D);
            this.renderLightning(graphics2D);
            this.renderFountainOutline(graphics2D);
            this.renderFountainTicks(graphics2D);
            return null;
        }
    }

    private void renderPoisonProjectileAreaTiles(Graphics2D graphics2D) {
        Map<LocalPoint, Projectile> poisonProjectiles = this.plugin.getPoisonProjectiles();
        if (this.config.poisonOutline() && !poisonProjectiles.isEmpty()) {
            POISON_AREA.reset();
            Iterator var3 = poisonProjectiles.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry<LocalPoint, Projectile> entry = (Map.Entry)var3.next();
                if (((Projectile)entry.getValue()).getEndCycle() >= this.client.getGameCycle()) {
                    LocalPoint localPoint = (LocalPoint)entry.getKey();
                    Polygon polygon = Perspective.getCanvasTileAreaPoly(this.client, localPoint, 3);
                    if (polygon != null) {
                        POISON_AREA.add(new Area(polygon));
                    }
                }
            }

            drawOutlineAndFill(graphics2D, this.config.poisonOutlineColor(), this.config.poisonFillColor(), (float)this.config.poisonStroke(), POISON_AREA);
        }
    }

    private void renderLightning(Graphics2D graphics2D) {
        Deque<GraphicsObject> graphicsObjects = this.client.getGraphicsObjects();
        if (this.config.lightningOutline() && this.hydra.getPhase() == HydraPhase.LIGHTNING) {
            Iterator var3 = graphicsObjects.iterator();

            while(var3.hasNext()) {
                GraphicsObject graphicsObject = (GraphicsObject)var3.next();
                if (graphicsObject.getId() == 1666) {
                    LocalPoint localPoint = graphicsObject.getLocation();
                    if (localPoint == null) {
                        return;
                    }

                    Polygon polygon = Perspective.getCanvasTilePoly(this.client, localPoint);
                    if (polygon == null) {
                        return;
                    }

                    drawOutlineAndFill(graphics2D, this.config.lightningOutlineColor(), this.config.lightningFillColor(), (float)this.config.lightningStroke(), polygon);
                }
            }

        }
    }

    private void renderHydraImmunityOutline() {
        NPC npc = this.hydra.getNpc();
        if (this.config.hydraImmunityOutline() && this.hydra.isImmunity() && npc != null && !npc.isDead()) {
            WorldPoint fountainWorldPoint = this.hydra.getPhase().getFountainWorldPoint();
            if (fountainWorldPoint != null) {
                Collection<WorldPoint> fountainWorldPoints = WorldPoint.toLocalInstance(this.client, fountainWorldPoint);
                if (fountainWorldPoints.size() == 1) {
                    WorldPoint worldPoint = null;

                    WorldPoint wp;
                    for(Iterator var5 = fountainWorldPoints.iterator(); var5.hasNext(); worldPoint = wp) {
                        wp = (WorldPoint)var5.next();
                    }

                    LocalPoint localPoint = LocalPoint.fromWorld(this.client, worldPoint);
                    if (localPoint != null) {
                        Polygon polygon = Perspective.getCanvasTileAreaPoly(this.client, localPoint, 3);
                        if (polygon != null) {
                            int stroke = 1;
                            if (npc.getWorldArea().intersectsWith(new WorldArea(worldPoint, 1, 1))) {
                                ++stroke;
                            }

                            this.modelOutlineRenderer.drawOutline(npc, stroke, this.hydra.getPhase().getPhaseColor(), 0);
                            return;
                        }
                    }
                }
            }

            this.modelOutlineRenderer.drawOutline(npc, 1, this.hydra.getPhase().getPhaseColor(), 0);
        }
    }

    private void renderFountainOutline(Graphics2D graphics2D) {
        NPC npc = this.hydra.getNpc();
        WorldPoint fountainWorldPoint = this.hydra.getPhase().getFountainWorldPoint();
        if (this.config.fountainOutline() && this.hydra.isImmunity() && fountainWorldPoint != null && npc != null && !npc.isDead()) {
            Collection<WorldPoint> fountainWorldPoints = WorldPoint.toLocalInstance(this.client, fountainWorldPoint);
            if (fountainWorldPoints.size() == 1) {
                WorldPoint worldPoint = null;

                WorldPoint wp;
                for(Iterator var6 = fountainWorldPoints.iterator(); var6.hasNext(); worldPoint = wp) {
                    wp = (WorldPoint)var6.next();
                }

                LocalPoint localPoint = LocalPoint.fromWorld(this.client, worldPoint);
                if (localPoint != null) {
                    Polygon polygon = Perspective.getCanvasTileAreaPoly(this.client, localPoint, 3);
                    if (polygon != null) {
                        Color color = this.hydra.getPhase().getFountainColor();
                        if (!npc.getWorldArea().intersectsWith(new WorldArea(worldPoint, 1, 1))) {
                            color = color.darker();
                        }

                        drawOutlineAndFill(graphics2D, color, new Color(color.getRed(), color.getGreen(), color.getBlue(), 30), 1.0F, polygon);
                    }
                }
            }
        }
    }

    private void renderFountainTicks(Graphics2D graphics2D) {
        if (this.config.fountainTicks()) {
            Collection<WorldPoint> fountainWorldPoints = WorldPoint.toLocalInstance(this.client, HydraPhase.POISON.getFountainWorldPoint());
            fountainWorldPoints.addAll(WorldPoint.toLocalInstance(this.client, HydraPhase.LIGHTNING.getFountainWorldPoint()));
            fountainWorldPoints.addAll(WorldPoint.toLocalInstance(this.client, HydraPhase.FLAME.getFountainWorldPoint()));
            if (!fountainWorldPoints.isEmpty()) {
                Iterator var4 = fountainWorldPoints.iterator();

                while(var4.hasNext()) {
                    WorldPoint wp = (WorldPoint)var4.next();
                    LocalPoint localPoint = LocalPoint.fromWorld(this.client, wp);
                    if (localPoint == null) {
                        return;
                    }

                    String text = String.valueOf(this.plugin.getFountainTicks());
                    Point timeLoc = Perspective.getCanvasTextLocation(this.client, graphics2D, localPoint, text, graphics2D.getFontMetrics().getHeight());
                    OverlayUtil.renderTextLocation(graphics2D, text, this.config.fountainTicksFontSize(), this.config.fountainTicksFontStyle().getFont(), this.config.fountainTicksFontColor(), timeLoc, this.config.fountainTicksFontShadow(), this.config.fountainTicksFontZOffset() * -1);
                }

            }
        }
    }

    private void renderHpUntilPhaseChange(Graphics2D graphics2D) {
        NPC npc = this.hydra.getNpc();
        if (this.config.showHpUntilPhaseChange() && npc != null && !npc.isDead()) {
            int hpUntilPhaseChange = this.hydra.getHpUntilPhaseChange();
            if (hpUntilPhaseChange != 0) {
                String text = String.valueOf(hpUntilPhaseChange);
                Point point = npc.getCanvasTextLocation(graphics2D, text, 0);
                if (point != null) {
                    OverlayUtil.renderTextLocation(graphics2D, text, this.config.fontSize(), this.config.fontStyle().getFont(), this.config.fontColor(), point, this.config.fontShadow(), this.config.fontZOffset() * -1);
                }
            }
        }
    }

    private static void drawOutlineAndFill(Graphics2D graphics2D, Color outlineColor, Color fillColor, float strokeWidth, Shape shape) {
        Color originalColor = graphics2D.getColor();
        Stroke originalStroke = graphics2D.getStroke();
        graphics2D.setStroke(new BasicStroke(strokeWidth));
        graphics2D.setColor(outlineColor);
        graphics2D.draw(shape);
        graphics2D.setColor(fillColor);
        graphics2D.fill(shape);
        graphics2D.setColor(originalColor);
        graphics2D.setStroke(originalStroke);
    }
}
