package com.example.Caminador;

import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.CollisionDataFlag;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Caminador {
    private Random nRand = new Random();

    public List<WorldPoint> getPath(WorldPoint[] puntos, int radio, Client client){
        List<WorldPoint> path = new ArrayList<>();
        for (WorldPoint pt:puntos) {
            int newX = pt.getX() - radio + nRand.nextInt(2*radio);
            int newY = pt.getY() - radio + nRand.nextInt(2*radio);


        }
        return null;
    }

    public WorldPoint getNextWp(WorldPoint ptInicial,int radio,Client client){
        int ancho = 2*radio+1;
        WorldArea area = new WorldArea(ptInicial.getX() - radio, ptInicial.getY() - radio,
                ancho,ancho, ptInicial.getPlane());
        for (int i = 0; i < ancho*ancho; i++) {
            int tilePruebaX = area.getX() + nRand.nextInt(ancho);
            int tilePruebaY = area.getY() + nRand.nextInt(ancho);
            WorldPoint tilePrueba=new WorldPoint(tilePruebaX,tilePruebaY, area.getPlane());
            if( area.contains(tilePrueba) ){
                LocalPoint localTile = LocalPoint.fromWorld(client,tilePrueba);
                CollisionData[] flagTiles = client.getCollisionMaps();
                assert flagTiles != null;
                CollisionData flagTile = flagTiles[client.getPlane()];
                assert localTile != null;
                if ((flagTile.getFlags()[localTile.getSceneX()][localTile.getSceneY()] & (CollisionDataFlag.BLOCK_MOVEMENT_OBJECT +
                        CollisionDataFlag.BLOCK_MOVEMENT_FLOOR + CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION)) == 0 ) { //check flags
                    return new WorldPoint(tilePruebaX,tilePruebaY, client.getPlane());
                }
            }
        }
        return null;
    }

    public WorldArea getAreaFromWP(WorldPoint wp, int radio) {
        WorldArea area = new WorldArea(wp.getX()-radio,wp.getY()-radio,2*radio +1,2*radio+1,wp.getPlane());
        return area;
    }

}
