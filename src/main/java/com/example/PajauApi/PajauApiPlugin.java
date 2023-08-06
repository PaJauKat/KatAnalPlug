package com.example.PajauApi;

import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.fungus.FungusPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.CollisionDataFlag;
import net.runelite.api.GameState;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;

import java.util.*;
import java.util.function.Predicate;

@Slf4j
public class PajauApiPlugin extends Plugin {

    static Client client = RuneLite.getInjector().getInstance(Client.class);

    public static final Random nRand = new Random();

    public static final int BANK_CLOSE_BUTTON = 786434;

    public static List<WorldPoint> tilesBuscados = new ArrayList<>();


    public static WorldPoint TilesAvalibleRadial(Client clt, int radio, WorldPoint ptCentral,Predicate<? super WorldPoint> predicado){

        int flagCondition = CollisionDataFlag.BLOCK_MOVEMENT_OBJECT + CollisionDataFlag.BLOCK_MOVEMENT_FLOOR +
                CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION;

        int[][] banderas = Objects.requireNonNull(clt.getCollisionMaps())[clt.getPlane()].getFlags();

        int x0 = ptCentral.getX() - clt.getBaseX();
        int y0 = ptCentral.getY() - clt.getBaseY();


        for (int i = 0; i <= 2*radio; i++) {
            tilesBuscados.add(ptCentral.dx(radio).dy(i-radio));
            if ((banderas[x0 + radio][y0 - radio + i] & flagCondition) == 0) {
                if ( predicado.test(ptCentral.dx(radio).dy(i-radio))) {
                    return ptCentral.dx(radio).dy(i-radio);
                }
            }
        }

        for (int i = 0; i <= 2 * radio; i++) {
            tilesBuscados.add(ptCentral.dx(radio-i).dy(radio));
            if ((banderas[x0 + radio - i][y0 + radio] & flagCondition) == 0) {
                if ( predicado.test(ptCentral.dx(radio-i).dy(radio))) {
                    return ptCentral.dx(radio-i).dy(radio);
                }
            }
        }

        for (int i = 0; i <= 2 * radio; i++) {
            tilesBuscados.add(ptCentral.dx(-radio).dy(radio-i));
            if ((banderas[x0 - radio][y0 + radio - i] & flagCondition) == 0) {
                if ( predicado.test(ptCentral.dx(-radio).dy(radio-i))) {
                    return ptCentral.dx(-radio).dy(radio-i);
                }
            }
        }

        for (int i = 0; i <= 2 * radio; i++) {
            tilesBuscados.add(ptCentral.dx(i-radio).dy(-radio));
            if ((banderas[x0 - radio + i][y0 - radio] & flagCondition) == 0) {
                if ( predicado.test(ptCentral.dx(i-radio).dy(-radio))) {
                    return ptCentral.dx(i-radio).dy(-radio);
                }
            }
        }

        return null;

    }


    public static WorldPoint getNextWp(WorldPoint ptInicial, int radio, Client client){
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

    public void walkTo() {

    }

    public static WorldArea centerWP2area(WorldPoint centerTile, int radio) {
        return new WorldArea(centerTile.dx(-radio).dy(-radio), 2 * radio + 1, 2 * radio + 1);
    }

    public static boolean caminando(Client client,WorldPoint[] camino,int radio,boolean lastTilePerfect) {
        WorldPoint wpPlayer = client.getLocalPlayer().getWorldLocation();
        for (int i = 0; i < camino.length; i++) {
            if (wpPlayer.isInArea(centerWP2area(camino[i], radio))) {
                if (i + 1 >= camino.length) {
                    //enCamino = false;
                    // failSafe = 0;
                    //estado = FungusPlugin.State.CALLAMPEANDO;
                    return false;
                } else {
                    WorldPoint nextTile = camino[i+1];
                    if (camino[i+1].isInScene(client)) {
                        MousePackets.queueClickPacket();
                        MovementPackets.queueMovement(i+1==camino.length-1 && lastTilePerfect? camino[i+1] : getNextWp(camino[i+1], radio, client));
                        //timeout = 2;
                    }

                }
                break;
            }
        }
        return true;
    }

    public boolean caminando(Client client,WorldPoint[] camino,int radio) {
        return caminando(client,camino,radio,true);
    }


    public static void hop(boolean previous, WorldService worldService, Client client) {

        WorldResult worldResult = worldService.getWorlds();
        if (worldResult == null || client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        World w = worldResult.findWorld(client.getWorld());

        EnumSet<WorldType> tipos = w.getTypes().clone();
        tipos.remove(WorldType.BOUNTY);
        tipos.remove(WorldType.LAST_MAN_STANDING);
        tipos.remove(WorldType.SKILL_TOTAL);

        List<World> munditos = worldResult.getWorlds();
        int worldIndex = munditos.indexOf(w);
        World wTest;

        do {

            if (previous) {
                worldIndex--;
                if (worldIndex < 0) {
                    worldIndex = munditos.size() - 1;
                }
            } else {
                worldIndex++;
                if (worldIndex >= munditos.size()) {
                    worldIndex = 0;
                }
            }

            wTest = munditos.get(worldIndex);
            EnumSet<WorldType> types = wTest.getTypes().clone();
            types.remove(WorldType.BOUNTY);
            types.remove(WorldType.LAST_MAN_STANDING);

            if (types.contains(WorldType.SKILL_TOTAL)) {
                try {
                    int totalReq = Integer.parseInt(wTest.getActivity().substring(0, wTest.getActivity().indexOf(" ")));
                    if (client.getTotalLevel() > totalReq) {
                        types.remove(WorldType.SKILL_TOTAL);
                    }
                } catch (NumberFormatException ex) {
                    log.warn("Failed to parse total level requirement for target world", ex);
                }
            }


            // Avoid switching to near-max population worlds, as it will refuse to allow the hop if the world is full
            if (wTest.getPlayers() >= 1800)
            {
                continue;
            }

            if (wTest.getPlayers() < 0)
            {
                // offline world
                continue;
            }

            if (types.equals(tipos)) {
                break;
            }


        } while (w != wTest);

        if (w == wTest) {
            log.info("No se encontro mundo");
        } else {
            hop(wTest.getId(),worldService,client);
            log.info("hopeando a {}",wTest.getId());
        }


    }


    public static void hop(int w,WorldService worldService, Client client) {
        assert client.isClientThread();
        World world = Objects.requireNonNull(worldService.getWorlds()).findWorld(w);
        if (world == null) {
            log.info("no se encontro el mundo");
            return;
        }

        log.info("katarina");

        final net.runelite.api.World rsWorld = client.createWorld();
        rsWorld.setActivity(world.getActivity());
        rsWorld.setAddress(world.getAddress());
        rsWorld.setId(world.getId());
        rsWorld.setPlayerCount(world.getPlayers());
        rsWorld.setLocation(world.getLocation());
        rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));

        client.hopToWorld(rsWorld);
    }










}
