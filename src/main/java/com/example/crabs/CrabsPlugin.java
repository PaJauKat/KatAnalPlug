package com.example.crabs;

import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.InteractionApi.NPCInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.WorldService;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.npcunaggroarea.NpcAggroAreaPlugin;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.GeneralPath;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
        name = "<html>[<font color=\"#FA4444\">P</font>] Crabs</html>",
        tags = {"pajau"},
        enabledByDefault = false
)
@PluginDependency(NpcAggroAreaPlugin.class)
public class CrabsPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private KeyManager keyManager;

    @Inject
    private NpcAggroAreaPlugin npcAggroAreaPlugin;

    @Inject
    private CrabsConfig config;

    @Inject
    private WorldService worldService;

    private GeneralPath AreaSafe;
    private boolean reseteando=false;
    private int timeout=-1;
    private int contador=0;
    private int llave;
    private final Color pint=Color.magenta;
    private Estados estado = Estados.STARTING;

    enum Estados{
        STARTING,
        EN_COMBATE,
        RESETTING,
        ACTIVAR_AGGRO_PLUG,
        SEARCHING_WORLD,
        APAGADO
    }

    @Provides
    CrabsConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(CrabsConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        keyManager.registerKeyListener(caminador);
        reseteando=false;
        enAccion=false;
        tilePelea=null;
        choosen=null;
    }

    @Override
    protected void shutDown() throws Exception {
        reseteando=false;
        enAccion=false;
        tilePelea=null;
        choosen=null;
    }

    private static WorldPoint choosen = null;
    public static WorldPoint tilePelea = null;
    private boolean enAccion = false;
    private final KeyListener caminador = new HotkeyListener( ()->new Keybind(KeyEvent.VK_F6,0)){
        @Override
        public void hotkeyPressed() {
            clientThread.invoke( () -> {

                enAccion=!enAccion;
                if (!enAccion) {
                    tilePelea = null;
                    estado=Estados.APAGADO;
                    log.info("Se apago la wea");
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Apagado", Color.red), "");
                } else {
                    tilePelea=client.getLocalPlayer().getWorldLocation();
                    estado=Estados.STARTING;
                    log.info("Se prendio la wea");
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Prendido", Color.GREEN), "");
                }
            });
        }
    };

    @Subscribe
    void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("crabs") ) {
            if (event.getKey().equals("onOff") ) {
                enAccion=!enAccion;
                clientThread.invoke(() -> {
                    if (!enAccion) {
                        tilePelea = null;
                        estado=Estados.APAGADO;
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Apagado", Color.red), "");
                    } else {
                        tilePelea=client.getLocalPlayer().getWorldLocation();
                        estado=Estados.STARTING;
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Prendido", Color.green), "");
                    }
                });
            }
        }
    }

    public boolean InsideSafe(){
        return AreaSafe.contains(client.getLocalPlayer().getLocalLocation().getX(),client.getLocalPlayer().getLocalLocation().getY());
    }

    public boolean InsideSafe(WorldPoint pt){
        return AreaSafe.contains(Objects.requireNonNull(LocalPoint.fromWorld(client, pt)).getX(),
                Objects.requireNonNull(LocalPoint.fromWorld(client, pt)).getY());
    }

    private int mundoActual = 0;
    private int hopFailSafe = 0;
    private int hopTimeout = 0;
    private final int TICKS_FOR_RETRY_HOP = 12;
    private final int RETRIES_FOR_HOP = 3;
    private int timeNoFighting = 0;
    private final int MAX_TIME_NO_FIGHTING = 100;
    private int resetTileTries = 0;
    private final int MAX_RESETTILE_TRIES = 3;

    private final int[] SAND_CRABS_REGION_IDS = {6710,6966,7222,7478,7479,6965,7221};

    @Subscribe
    void onGameTick(GameTick event){
        if(!enAccion) return;
        if (timeout>0) {
            timeout--;
            return;
        }
        if (npcAggroAreaPlugin.getEndTime() == null) {
            log.info("npc agresion timer no activado");
            timeout = 10;
            client.addChatMessage(ChatMessageType.GAMEMESSAGE,"","Npc agresion timer no activado","");
            return;
        }

        AreaSafe = npcAggroAreaPlugin.getLinesToDisplay()[client.getPlane()];
        if (AreaSafe == null) {
            return;
        }


        CollisionData[] collisionData = client.getCollisionMaps();
        if (collisionData == null) {
            return;
        }
        CollisionData collActual = collisionData[client.getPlane()];
        Player jugador = client.getLocalPlayer();
        WorldArea playerArea = client.getLocalPlayer().getWorldArea();
        WorldPoint playerPoint = client.getLocalPlayer().getWorldLocation();
        int plano = client.getPlane();
        int ScenePlayerX = client.getLocalPlayer().getLocalLocation().getSceneX();
        int ScenePlayerY = client.getLocalPlayer().getLocalLocation().getSceneY();

        int baseX = client.getBaseX();
        int baseY = client.getBaseY();
        log.info("estado: {}",estado);

        if (estado == Estados.STARTING) {
            if ( Arrays.stream(SAND_CRABS_REGION_IDS).anyMatch(x -> x==playerPoint.getRegionID() ) ) {
                estado = Estados.SEARCHING_WORLD;
            }
        }

        else if (estado == Estados.SEARCHING_WORLD) {

            if (mundoActual == 0) {
                tilePelea = null;
                for (WorldPoint wp : config.spot().getPuntos()) {
                    if (client.getPlayers().stream().noneMatch( x -> x.getWorldLocation().isInArea(toWorldArea(wp, 2)))  ) {
                        tilePelea = wp;
                        break;
                    }
                }
                if (tilePelea == null) {
                    mundoActual = client.getWorld();
                    log.info("hopeando");
                    client.openWorldHopper();
                    hop(false);
                } else {
                    estado = Estados.EN_COMBATE;
                }
            } else {
                if (mundoActual != client.getWorld()) {
                    mundoActual = 0;
                } else {
                    hopTimeout++;
                    if (hopTimeout > TICKS_FOR_RETRY_HOP) {
                        hopFailSafe++;
                        if (hopFailSafe > RETRIES_FOR_HOP) {
                            estado = Estados.APAGADO;
                            log.info("No se pudo hopear");
                        } else {
                            log.info("hopeando denuevo");
                            hop(false);
                        }
                    }
                }
            }
        }

        else if (estado == Estados.EN_COMBATE) {

            if (npcAggroAreaPlugin.getEndTime().isBefore(Instant.now())) {
                estado = Estados.RESETTING;
            } else {
                if (!playerPoint.equals(tilePelea)) {
                    if (jugador.getPoseAnimation() == jugador.getIdlePoseAnimation()) {
                        MousePackets.queueClickPacket();
                        MovementPackets.queueMovement(tilePelea);
                    }
                } else {
                    if (jugador.isInteracting()) {
                        timeNoFighting = 0;
                    } else {
                        timeNoFighting++;
                        if (timeNoFighting > MAX_TIME_NO_FIGHTING) {
                            List<NPC> NPCsCercanos = NPCs.search().withinWorldArea(toWorldArea(playerPoint, 3)).result();
                            if (!NPCsCercanos.isEmpty()) {
                                NPCsCercanos = NPCsCercanos.stream().filter(x -> !x.isInteracting()).collect(Collectors.toList());
                                NPCInteraction.interact(NPCsCercanos.get(0));
                                timeout = 5;
                                timeNoFighting = MAX_TIME_NO_FIGHTING/2;
                            } else {
                                log.info("No hay NPCs cerca atakables");
                            }
                        }
                    }
                }
            }
        } else if (estado == Estados.RESETTING) {
            if (npcAggroAreaPlugin.getEndTime().isBefore(Instant.now())) {
                List<NPC> npcs;
                if (client.getFollower() != null) {
                    npcs = client.getNpcs().stream().filter(x -> x.getId() != client.getFollower().getId()).collect(Collectors.toList());
                }else {
                    npcs = client.getNpcs();
                }

                if (npcs.stream().noneMatch(x -> x.getInteracting() != null
                        && x.getInteracting().getName() != null
                        && x.getInteracting().getName().equalsIgnoreCase(jugador.getName())) ) { //si no hay npc targeteando al player
                    log.info("no hay targeteandome");
                    if (jugador.getPoseAnimation() == jugador.getIdlePoseAnimation()) {
                        log.info("meow");
                        choosen = null;
                        for (int i = 1; i < 22; i++) {
                            if (isWalkable(collActual, ScenePlayerX + i, ScenePlayerY + i) && !InsideSafe(playerPoint.dx(i).dy(i))) {
                                choosen = new WorldPoint(baseX + ScenePlayerX + i, baseY + ScenePlayerY + i, client.getPlane());
                                break;
                            } else if (isWalkable(collActual, ScenePlayerX - i, ScenePlayerY + i) && !InsideSafe(playerPoint.dx(-i).dy(i))) {
                                choosen = new WorldPoint(baseX + ScenePlayerX - i, baseY + ScenePlayerY + i, client.getPlane());
                                break;
                            } else if (isWalkable(collActual, ScenePlayerX - i, ScenePlayerY - i) && !InsideSafe(playerPoint.dx(-i).dy(-i))) {
                                choosen = new WorldPoint(baseX + ScenePlayerX - i, baseY + ScenePlayerY - i, client.getPlane());
                                break;
                            } else if (isWalkable(collActual, ScenePlayerX + i, ScenePlayerY - i) && !InsideSafe(playerPoint.dx(i).dy(-i))) {
                                choosen = new WorldPoint(baseX + ScenePlayerX + i, baseY + ScenePlayerY - i, client.getPlane());
                                break;
                            }
                        }
                        if (choosen == null) {
                            resetTileTries++;
                            if (resetTileTries > MAX_RESETTILE_TRIES) {
                                log.info("No se encontro un resetTile");
                                estado = Estados.APAGADO;
                                enAccion = false;
                            }
                        } else {
                            log.info("Moviendo hacia un Tile reseteador");
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(choosen);
                            log.info("Tile escogido: {}", choosen);
                        }
                        timeout = 1;
                    }

                }
            } else {
                log.info("Woof");
                if (!playerPoint.equals(tilePelea)) {
                    if (jugador.getPoseAnimation() == jugador.getIdlePoseAnimation()) {
                        MousePackets.queueClickPacket();
                        MovementPackets.queueMovement(tilePelea);
                    }
                } else {
                    estado = Estados.EN_COMBATE;
                }


            }
        }
    }

    private void revisarTiles() {
    }

    public boolean InCombat(Player yo){
        return yo.isInteracting() || yo.getAnimation()!=-1 || client.getNpcs().stream().anyMatch(mono-> {
            if (mono.getInteracting()!=null) {
                return mono.getInteracting().equals(yo);
            }
            return false;
        });
    }

    public boolean isWalkable(CollisionData colData,int x,int y){
        return (colData.getFlags()[x][y] & (CollisionDataFlag.BLOCK_MOVEMENT_OBJECT + CollisionDataFlag.BLOCK_MOVEMENT_FLOOR +
                CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION)) == 0;
    }

    @Subscribe
    void onGameStateChanged(GameStateChanged event){
        if(event.getGameState() == GameState.LOGGED_IN){
            AreaSafe = npcAggroAreaPlugin.getLinesToDisplay()[client.getPlane()];
        }
    }

    private WorldArea toWorldArea(WorldPoint worldPoint,int radio) {
        return new WorldArea(worldPoint.dx(-radio).dy(-radio),2*radio+1,2*radio+1   );
    }

    public void hop(boolean previous) {

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
            hop(wTest.getId());
            log.info("hopeando a {}",wTest.getId());
        }


    }
    private void hop(int w) {
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
