//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.alchemicalhydra.overlay;

import com.example.PacketUtils.WidgetInfoExtended;
import com.google.common.base.Strings;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

@Slf4j
public class OverlayUtil {
    private int meow = 0;
    public OverlayUtil() {
    }

    public static Rectangle renderPrayerOverlay(Graphics2D graphics, Client client, Prayer prayer, Color color) {
        Widget wea = null;
        if(Prayer.PROTECT_FROM_MAGIC == prayer){
            wea = client.getWidget(35454997);
        } else if (Prayer.PROTECT_FROM_MISSILES == prayer) {
            wea = client.getWidget(35454998);
        } else if (Prayer.PROTECT_FROM_MELEE == prayer) {
            wea = client.getWidget(35454999);
        }else {
            wea = null;
        }
        //Widget widget = client.getWidget(prayer.getWidgetInfo());
        Widget widget = wea;
        //Widget widget = client.getWidget(WidgetInfoExtended.valueOf("PRAYER_"+prayer.name()).getPackedId());
        if (widget != null && client.getVarcIntValue(VarClientInt.INVENTORY_TAB) == 5) {
            Rectangle bounds = widget.getBounds();
            net.runelite.client.ui.overlay.OverlayUtil.renderPolygon(graphics, rectangleToPolygon(bounds), color);
            return bounds;
        } else {
            return null;
        }
    }

    private static Polygon rectangleToPolygon(Rectangle rect) {
        int[] xpoints = new int[]{rect.x, rect.x + rect.width, rect.x + rect.width, rect.x};
        int[] ypoints = new int[]{rect.y, rect.y, rect.y + rect.height, rect.y + rect.height};
        return new Polygon(xpoints, ypoints, 4);
    }

    public static void renderTextLocation(Graphics2D graphics, String txtString, int fontSize, int fontStyle, Color fontColor, Point canvasPoint, boolean shadows, int yOffset) {
        graphics.setFont(new Font("Arial", fontStyle, fontSize));
        if (canvasPoint != null) {
            Point canvasCenterPoint = new Point(canvasPoint.getX(), canvasPoint.getY() + yOffset);
            Point canvasCenterPoint_shadow = new Point(canvasPoint.getX() + 1, canvasPoint.getY() + 1 + yOffset);
            if (shadows) {
                renderTextLocation(graphics, canvasCenterPoint_shadow, txtString, Color.BLACK);
            }

            renderTextLocation(graphics, canvasCenterPoint, txtString, fontColor);
        }

    }

    public static void renderTextLocation(Graphics2D graphics, Point txtLoc, String text, Color color) {
        if (!Strings.isNullOrEmpty(text)) {
            int x = txtLoc.getX();
            int y = txtLoc.getY();
            graphics.setColor(Color.BLACK);
            graphics.drawString(text, x + 1, y + 1);
            graphics.setColor(color);
            graphics.drawString(text, x, y);
        }
    }
}
