package com.example.nex;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.InteractionApi.InteractionHelper;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.Angle;
import net.runelite.api.coords.Direction;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
        name = "Nex Tulong",
        tags = {"pajau"},
        enabledByDefault = false
)
public class NexPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private KeyManager keyManager;

    @Inject
    private NexConfig config;

    Random N = new Random();
    private boolean encendido = false;
    private State estado;
    private int timeout = 0;
    private NPC nex;


    //todo ver bien los tiles de la cruz
    WorldArea areaEast1 = new WorldArea(2927, 5202, 4, 3, 0);
    WorldArea areaEast2 = new WorldArea(2931, 5202, 4, 3, 0);

    WorldArea areaNorth1 = new WorldArea(2924, 5205, 3, 4, 0);
    WorldArea areaNorth2 = new WorldArea(2924, 5209, 3, 4, 0);

    WorldArea areaWest1 = new WorldArea(2920, 5202, 4, 3, 0);
    WorldArea areaWest2 = new WorldArea(2916, 5202, 4, 3, 0);

    WorldArea areaSouth1 = new WorldArea(2924, 5198, 3, 4, 0);
    WorldArea areaSouth2 = new WorldArea(2924, 5194, 3, 4, 0);
    private NPC fumus;
    private int potTimeout=0;
    final int R = 3;

    final int mcTries=20;

    private GameObject weaNegra = null;

    private boolean flagWeaNegra = false;

    final WorldPoint[] ptsPrueba = {
            new WorldPoint(2935,5205,0),
            new WorldPoint(2923, 5213, 0),
            new WorldPoint(2927, 5213, 0),
    };

    final WorldPoint[] tilesUmbra = {
            new WorldPoint(2935, 5205, 0),
            new WorldPoint(2937, 5205,  0),
            new WorldPoint(2927, 5213,  0),
            new WorldPoint(2927, 5215,  0)
    };

    final WorldArea umbraAttackArea = new WorldArea(2927, 5205, 11, 11, 0);

    private final KeyListener botonEncendido = new HotkeyListener(()->new Keybind(KeyEvent.VK_F8,0)) {
        @Override
        public void keyPressed(KeyEvent e) {
            clientThread.invoke(() -> {
                encendido = !encendido;
                if (encendido) {
                    estado = State.ENTRAR;
                } else {
                    estado = State.APAGADO;
                }
            });
        }
    };


    @Override
    protected void startUp() throws Exception {
        keyManager.registerKeyListener(botonEncendido);
    }

    @Override
    protected void shutDown() throws Exception {
        keyManager.unregisterKeyListener(botonEncendido);
    }

    @Provides
    NexConfig getConfig(ConfigManager configManager) {
        return (NexConfig) configManager.getConfig(NexConfig.class);
    }

    private boolean lowHP(int percent) {
        return client.getBoostedSkillLevel(Skill.HITPOINTS) <= (percent/100) * client.getRealSkillLevel(Skill.HITPOINTS);
    }

    public boolean isIdle(Player gamer) {
        return gamer.getIdlePoseAnimation() == gamer.getPoseAnimation();
    }

    public boolean isIdle(NPC mono) {
        return mono.getIdlePoseAnimation() == mono.getPoseAnimation();
    }





    @Subscribe
    void onGameTick(GameTick event) {
        if(!encendido) return;
        if (timeout>0) {
            timeout--;
            return;
        }

        if (estado.isEnPelea()) {
            if (potTimeout > 0) {
                potTimeout--;
            } else if (lowHP(config.hpPercentHeal())) {
                Optional<Widget> foodUsed = Inventory.search().matchesWildCardNoCase(config.foodName()).first();
                if (foodUsed.isPresent()) {
                    potTimeout = 4;
                    if (foodUsed.get().getName().contains("(")) {
                        InventoryInteraction.useItem(foodUsed.get(), "Drink");
                    } else {
                        InventoryInteraction.useItem(foodUsed.get(), "Eat");
                    }
                }
            } else if (client.getBoostedSkillLevel(Skill.PRAYER) < config.prayThreshold()) {
                Optional<Widget> prayPot = Inventory.search().matchesWildCardNoCase(config.prayRestorationName()).first();
                if (prayPot.isPresent()) {
                    potTimeout = 4;
                    InventoryInteraction.useItem(prayPot.get(),"Drink");
                }
            }
        }

        Player player = client.getLocalPlayer();
        if (estado == State.FASE_1) {

            if(nex.isDead() || nex ==null){
                Optional<NPC> nix = NPCs.search().withId(11278).first();
                nix.ifPresent(x -> nex=x );
            }
            if(nex == null){
                return;
            }
            if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MAGIC) != 1) {
                InteractionHelper.togglePrayer();
            }

            if (!isIdle(player) && !player.isInteracting()) {//si esta corriendo y no esta interactuando
                return;
            }

            if (nex.getAnimation() == 9178) {    //todo ver si la animacion es diferente para cada punta de Cruz
                int angulo = nex.getCurrentOrientation();
                Angle ang = new Angle(nex.getCurrentOrientation());

                if(ang.getNearestDirection()== Direction.EAST){
                    if(player.getWorldLocation().isInArea(areaEast1)){
                        Collection<WorldPoint> pene = WorldPoint.toLocalInstance(client, new WorldPoint(2975, 5205, 0));
                        WorldPoint t = new ArrayList<>(pene).get(0);
                        WorldPoint[] m = pene.toArray(WorldPoint[]::new);
                        MovementPackets.queueMovement(m[0]);
                    } else if (player.getWorldLocation().isInArea(areaEast2)) {
                        MovementPackets.queueMovement(2935,5203,false);
                        MovementPackets.queueMovement(WorldPoint.toLocalInstance(client,new WorldPoint(2935,5203,0)).toArray(WorldPoint[]::new)[0]);
                    }
                } else if (ang.getNearestDirection() == Direction.NORTH) {
                    if(player.getWorldLocation().isInArea(areaNorth1)){
                        MovementPackets.queueMovement(2923,5203,false);
                    } else if (player.getWorldLocation().isInArea(areaNorth2)) {
                        MovementPackets.queueMovement(2925,5213,false);
                    }
                } else if (ang.getNearestDirection() == Direction.SOUTH) {
                    if(player.getWorldLocation().isInArea(areaSouth1)){
                        MovementPackets.queueMovement(2923,5203,false);
                    } else if (player.getWorldLocation().isInArea(areaSouth2)) {
                        MovementPackets.queueMovement(2925,5213,false);
                    }
                } else if (ang.getNearestDirection() == Direction.WEST) {
                    if(player.getWorldLocation().isInArea(areaWest1)){
                        MovementPackets.queueMovement(2923,5203,false);
                    } else if (player.getWorldLocation().isInArea(areaWest2)) {
                        MovementPackets.queueMovement(2915,5203,false);
                    }
                }
            } else if (player.getInteracting() == null) {
                //esta nex atacable
                NPCInteraction.interact(nex, "Attack");
            }
            
        } else if (estado == State.FUMUS) {

            if (fumus.isDead()) {
                estado = State.FASE_2;
            } else if (!Objects.equals(WorldPoint.fromLocalInstance(client, player.getLocalLocation()), new WorldPoint(2923, 5213, 0))
                    && isIdle(player)) {
                moverHacia(client, new WorldPoint(2923, 5213, 0));
            } else {
                NPCs.search().nameContains("Fumus").first().ifPresent(x -> {
                    fumus = x;
                    NPCInteraction.interact(x, "Attack");
                });
            }
        } else if (estado == State.FASE_2) {
            if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MISSILES) == 0) {
                InteractionHelper.toggleNormalPrayers(List.of(35454998,35455009));  //rigour y protect ranged
            }
            WorldPoint nexLoc = nex.getWorldLocation().dx(1).dy(1);
            CollisionData col = Objects.requireNonNull(client.getCollisionMaps())[client.getPlane()];
            WorldPoint escapeTile = null;

            if(flagWeaNegra && isIdle(player)){
                WorldPoint wpGamer = player.getWorldLocation();
                MousePackets.queueClickPacket();
                if (isWalkable(client.getCollisionMaps()[client.getPlane()], wpGamer.dx(1).dy(0))) {
                    MovementPackets.queueMovement(wpGamer.dx(1).dy(1));
                } else if (isWalkable(client.getCollisionMaps()[client.getPlane()], wpGamer.dx(0).dy(1))) {
                    MovementPackets.queueMovement(wpGamer.dx(0).dy(1));
                } else if (isWalkable(client.getCollisionMaps()[client.getPlane()], wpGamer.dx(-1).dy(0))) {
                    MovementPackets.queueMovement(wpGamer.dx(-1).dy(0));
                } else if (isWalkable(client.getCollisionMaps()[client.getPlane()], wpGamer.dx(0).dy(-1))) {
                    MovementPackets.queueMovement(wpGamer.dx(0).dy(-1));
                }
            }
            
            if (player.getWorldLocation().distanceTo(nexLoc) <= 5) {
                //todo correr de nex p2
                if ( isIdle(nex)) {
                    int dx = nexLoc.getX()-player.getWorldLocation().getX();
                    int dy = nexLoc.getY()-player.getWorldLocation().getY();

                    if (dx >= 0 && Math.abs(dx) >= Math.abs(dy)) {
                        for (int i = 0; i < mcTries; i++) {
                            int ax = nexLoc.getX() - 7 - N.nextInt(5);
                            int ay = player.getWorldLocation().getY()-R +N.nextInt(2*R+1);

                            if(isWalkable(col,ax - client.getBaseX(),ay - client.getBaseY())){
                                escapeTile = new WorldPoint(ax,ay,client.getPlane());
                                break;
                            }
                        }
                    } else if (dx <= 0 && Math.abs(dx)>=Math.abs(dy)) {
                        for (int i = 0; i < mcTries; i++) {
                            int ax = nexLoc.getX()+7+N.nextInt(5);
                            int ay = player.getWorldLocation().getY() - R + N.nextInt(2*R+1);

                            if (isWalkable(col, ax - client.getBaseX(), ay - client.getBaseY())) {
                                escapeTile = new WorldPoint(ax,ay,client.getPlane());
                                break;
                            }
                        }
                    } else if (dy > 0 && Math.abs(dy) > Math.abs(dx)) {
                        for (int i = 0; i < mcTries; i++) {
                            int ax = player.getWorldLocation().getX()-R+N.nextInt(2*R+1);
                            int ay = nexLoc.getY()-7-N.nextInt(2*R+1);

                            if (isWalkable(col, ax - client.getBaseX(), ay - client.getBaseY())) {
                                escapeTile = new WorldPoint(ax,ay,client.getPlane());
                                break;
                            }
                        }
                    } else if (dy < 0 && Math.abs(dy) > Math.abs(dx)) {
                        for (int i = 0; i < mcTries; i++) {
                            int ax = player.getWorldLocation().getX()-R+N.nextInt(2*R+1);
                            int ay = nexLoc.getY()+7+N.nextInt(2*R+1);

                            if (isWalkable(col, ax - client.getBaseX(), ay - client.getBaseY())) {
                                escapeTile = new WorldPoint(ax,ay,client.getPlane());
                                break;
                            }
                        }
                    }
                    
                    if (escapeTile == null) {
                        WorldArea areaNex = new WorldArea(nex.getWorldLocation().getX()-10,nex.getWorldLocation().getY()-10,
                                22,22,client.getPlane());
                        WorldArea areaMonkas = new WorldArea(nex.getWorldLocation().getX()-5,nex.getWorldLocation().getY()-5,
                                12,12,client.getPlane());
                        for (WorldPoint wp :
                                ptsPrueba) {
                            if(wp.isInArea(areaNex) && !wp.isInArea(areaMonkas)){
                                escapeTile = wp;
                                break;
                            }
                        }
                        if(escapeTile == null){
                            for (WorldPoint wp :
                                    ptsPrueba) {
                                if(wp.isInArea(areaNex)){
                                    escapeTile = wp;
                                    break;
                                }
                            }
                        }
                    }

                }
                if (escapeTile != null) {
                    MovementPackets.queueMovement(escapeTile);
                } else {
                    log.info("no se encontro un puto tile");
                }

            } else if (isIdle(player) && !player.isInteracting()) {
                NPCInteraction.interact(nex, "Attack");
            } 

        } else if (estado == State.UMBRA) {
            if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MISSILES) == 0) {
                InteractionHelper.toggleNormalPrayers(List.of(35454998,35455009));  //rigour y protect ranged
            }
            WorldPoint nexLoc = nex.getWorldLocation().dx(1).dy(1);
            CollisionData col = Objects.requireNonNull(client.getCollisionMaps())[client.getPlane()];
            WorldPoint escapeTile = null;

            WorldArea areaNex = new WorldArea(nex.getWorldLocation().getX()-10,nex.getWorldLocation().getY()-10,
                    22,22,client.getPlane());
            WorldArea areaMonkas = new WorldArea(nex.getWorldLocation().getX()-5,nex.getWorldLocation().getY()-5,
                    12,12,client.getPlane());

            if(player.getWorldLocation().isInArea(umbraAttackArea) && !player.getWorldLocation().isInArea(areaMonkas)){
                if(!player.isInteracting()){
                    NPCs.search().nameContains("Umbra").first().ifPresent(x->NPCInteraction.interact(x,"Attack"));
                }

            } else if (player.getWorldLocation().isInArea(areaMonkas)) {
                List<Pair<Integer,WorldPoint>> tilesSafe = new ArrayList<Pair<Integer,WorldPoint>>();
                Arrays.stream(tilesUmbra).forEach(x -> {
                    if(!x.isInArea(areaMonkas)){
                        tilesSafe.add(Pair.of(x.distanceTo(player.getWorldLocation()),x ));
                    }
                });

                Optional<Pair<Integer, WorldPoint>> distMin = tilesSafe.stream().min((n1, n2)->Integer.compare(n1.getLeft(),n2.getLeft()));

                distMin.ifPresent(x->{
                    MousePackets.queueClickPacket();
                    MovementPackets.queueMovement(x.getRight());
                });
            }



        }

    }

    @Subscribe
    void onChatmessage(ChatMessage event) {
        if (event.getMessage().contains("Fumus, don't fail me!")) {
            estado = State.FUMUS;
        } else if (event.getMessage().contains("Umbra, don't fail me!")) {
            estado = State.UMBRA;
        } else if (event.getMessage().contains("Cruor, don't fail me!")) {
            estado = State.CRUOR;
        } else if (event.getMessage().contains("Glacies, don't fail me!")) {
            estado = State.GLACIES;
        }
    }

    @Subscribe
    void onGameObjectSpawned(GameObjectSpawned event) {
        if (event.getGameObject().getId() == 42942) {
            weaNegra = event.getGameObject();
            flagWeaNegra = true;
        }
    }

    private void moverHacia(Client client, WorldPoint wp) {
        MovementPackets.queueMovement(WorldPoint.toLocalInstance(client,wp).toArray(WorldPoint[]::new)[0]);
    }

    public boolean isWalkable(CollisionData colData,int x,int y){   //x y son coordenadas de Scene
        return (colData.getFlags()[x][y] & (CollisionDataFlag.BLOCK_MOVEMENT_OBJECT + CollisionDataFlag.BLOCK_MOVEMENT_FLOOR +
                CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION)) == 0;
    }

    public boolean isWalkable(CollisionData colData,WorldPoint wp){   //x y son coordenadas de Scene
        int x = wp.getX() - client.getBaseX();
        int y = wp.getY() - client.getBaseY();
        return (colData.getFlags()[x][y] & (CollisionDataFlag.BLOCK_MOVEMENT_OBJECT + CollisionDataFlag.BLOCK_MOVEMENT_FLOOR +
                CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION)) == 0;
    }

    private int num2ang(int num) {
        int ang = 1536-num;
        if(num<512){
            return 12;
        }
        return 24;
    }
}
