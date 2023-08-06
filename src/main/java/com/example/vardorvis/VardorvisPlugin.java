package com.example.vardorvis;

import com.example.InteractionApi.InteractionHelper;
import com.example.PacketUtils.WidgetInfoExtended;
import com.example.PajauApi.PajauApiPlugin;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Perspective;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.geometry.Geometry;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

@PluginDescriptor(
        name = "<html>[<font color=\"#59D634\">P</font>] Vardorvis</html>",
        description = "En contruccion",
        tags = {"pajau"}
)
@Slf4j
public class VardorvisPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private VardorvisOverlay vardorvisOverlay;

    @Inject
    private VardorvisConfig config;

    @Override
    protected void startUp() throws Exception {
        this.overlayManager.add(vardorvisOverlay);
        calculateLinesToDisplay();
    }

    @Override
    protected void shutDown() throws Exception {
        this.overlayManager.remove(vardorvisOverlay);
    }

    @Provides
    VardorvisConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(VardorvisConfig.class);
    }

    private final int AXES_TICKS = 10;

    private int weaRangedInit = 4;


    int axe_1_9 = 0;
    int axe_2_8 = 0;
    int axe_7_3 = 0;
    int axe_4_6 = 0;
    private int weaRanged = -1;

    void reset() {
        axe_1_9 = 0;
        axe_2_8 = 0;
        axe_7_3 = 0;
        axe_4_6 = 0;
    }




    private final WorldPoint pivoteTileReal = new WorldPoint(1124, 3413,0);

    GeneralPath lines73 = new GeneralPath();

    GeneralPath lines_1_9 = new GeneralPath();
    GeneralPath lines_4_6 = new GeneralPath();
    GeneralPath lines_2_8 = new GeneralPath();


    private final List<WorldPoint> spawns = List.of(
            new WorldPoint(1123, 3422, 0),
            new WorldPoint(1128, 3422, 0),
            new WorldPoint(1133, 3422, 0),

            new WorldPoint(1123, 3417, 0),
            new WorldPoint(1133, 3417, 0),

            new WorldPoint(1123, 3412, 0),
            new WorldPoint(1128, 3412, 0),
            new WorldPoint(1133, 3412, 0)
    );


    void linesWorld2Local(float[] coords) {
        final LocalPoint lp = LocalPoint.fromWorld(client, (int) coords[0],(int) coords[1]);
        coords[0] = lp.getX() - Perspective.LOCAL_TILE_SIZE / 2f;
        coords[1] = lp.getY() - Perspective.LOCAL_TILE_SIZE / 2f;
    }

    private void calculateLinesToDisplay() {

        WorldPoint pivoteTile = new ArrayList<>(WorldPoint.toLocalInstance(client,pivoteTileReal)).get(0);
        log.info("anal");
        Polygon poly7 = new Polygon();
        poly7.addPoint(pivoteTile.getX(), pivoteTile.getY());
        poly7.addPoint(pivoteTile.getX(), pivoteTile.getY() + 3);
        poly7.addPoint(pivoteTile.getX() + 3, pivoteTile.getY() + 3);
        poly7.addPoint(pivoteTile.getX() + 3, pivoteTile.getY());
        Area area7 = new Area(poly7);

        for (int i = 0; i < 8; i++) {
            poly7.translate(1, 1);
            area7.add(new Area(poly7));
        }

        lines73 = new GeneralPath(area7);

        Rectangle sceneRect = new Rectangle(client.getBaseX() + 1, client.getBaseY() + 1, Constants.SCENE_SIZE - 2, Constants.SCENE_SIZE - 2);
        lines73 = Geometry.clipPath(lines73, sceneRect);
        lines73 = Geometry.splitIntoSegments(lines73, 1);
        lines73 = Geometry.transformPath(lines73, this::linesWorld2Local);

        //----------------------------------------------------------------

        Polygon poly1 = new Polygon();
        int x0 = pivoteTile.getX();
        int y0 = pivoteTile.getY() + 8;
        poly1.addPoint(x0, y0);
        poly1.addPoint(x0, y0 + 3);
        poly1.addPoint(x0 + 3, y0 + 3);
        poly1.addPoint(x0 + 3, y0);

        Area area1 = new Area(poly1);

        for (int i = 0; i < 8; i++) {
            poly1.translate(1, -1);
            area1.add(new Area(poly1));
        }

        lines_1_9 = new GeneralPath(area1);

        lines_1_9 = Geometry.clipPath(lines_1_9, sceneRect);
        lines_1_9 = Geometry.splitIntoSegments(lines_1_9, 1);
        lines_1_9 = Geometry.transformPath(lines_1_9, this::linesWorld2Local);

        //----------------------------------------------------------------

        Polygon poly4 = new Polygon();
        x0 = pivoteTile.getX();
        y0 = pivoteTile.getY() + 4;
        poly4.addPoint(x0, y0);
        poly4.addPoint(x0, y0 + 3);
        poly4.addPoint(x0 + 11, y0 + 3);
        poly4.addPoint(x0 + 11, y0);

        lines_4_6 = new GeneralPath(new Area(poly4));

        lines_4_6 = Geometry.clipPath(lines_4_6, sceneRect);
        lines_4_6 = Geometry.splitIntoSegments(lines_4_6, 1);
        lines_4_6 = Geometry.transformPath(lines_4_6, this::linesWorld2Local);

        //----------------------------------------------------------------

        Polygon poly2 = new Polygon();
        x0 = pivoteTile.getX() + 4;
        y0 = pivoteTile.getY();
        poly2.addPoint(x0, y0);
        poly2.addPoint(x0, y0 + 11);
        poly2.addPoint(x0 + 3, y0 + 11);
        poly2.addPoint(x0 + 3, y0);

        lines_2_8 = new GeneralPath(new Area(poly2));

        lines_2_8 = Geometry.clipPath(lines_2_8, sceneRect);
        lines_2_8 = Geometry.splitIntoSegments(lines_2_8, 1);
        lines_2_8 = Geometry.transformPath(lines_2_8, this::linesWorld2Local);


    }

    @Subscribe
    void onGameTick(GameTick event) {
        if (axe_4_6 > 0) {
            axe_4_6--;
        }
        if (axe_2_8 > 0) {
            axe_2_8--;
        }
        if (axe_1_9 > 0) {
            axe_1_9--;
        }
        if (axe_7_3 > 0) {
            axe_7_3--;
        }

        if (weaRanged >= 0) {
            if (weaRanged == weaRangedInit - 1) {
                if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MISSILES) == 0) {
                    InteractionHelper.toggleNormalPrayer(35454998);
                }
            } else if (weaRanged == 0) {
                if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MISSILES) == 1) {
                    InteractionHelper.toggleNormalPrayer(35454999);
                }
            }

            weaRanged--;
        }

    }


    @Subscribe
    void onNpcSpawned(NpcSpawned event) {

        if (event.getNpc().getId() == 12223) {
            calculateLinesToDisplay();
        }

        if (config.rangeSwitch()) {
            if (event.getNpc().getId() == 12226) {
                weaRangedInit = 3 + PajauApiPlugin.nRand.nextInt(2);
                weaRanged = weaRangedInit;
            }
        }

        if (config.axesPath()) {
            if (event.getNpc().getId() == 12225) {
                LocalPoint lp = LocalPoint.fromWorld(client, event.getNpc().getWorldLocation());
                if (lp!= null && lp.isInScene()) {
                    WorldPoint loc = WorldPoint.fromLocalInstance(client, lp);
                    log.info("meow: {}",loc);
                    int m = 1;
                    for (WorldPoint wp : spawns) {
                        if (wp.equals(loc)) {
                            log.info("Spawn en: {}",wp);
                            break;
                        }
                        m++;
                    }
                    log.info("m: {}",m);
                    switch (m) {
                        case 1:
                        case 8:
                            axe_1_9 = AXES_TICKS;
                            break;
                        case 2:
                        case 7:
                            axe_2_8 = AXES_TICKS;
                            break;
                        case 6:
                        case 3:
                            axe_7_3 = AXES_TICKS;
                            break;
                        case 4:
                        case 5:
                            axe_4_6 = AXES_TICKS;
                            break;
                        default:
                            log.info("axe spot not found");
                    }
                }
            }
        }


    }


}
