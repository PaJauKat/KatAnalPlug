package com.example.Robador;

import com.example.EthanApiPlugin.Collections.BankInventory;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.InteractionApi.*;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

@PluginDescriptor(
        name = "<html>[<font color=\"#FA4444\">P</font>] Ardy knights</html>",
        enabledByDefault = false,
        tags = {"pajau","thieving","robador"}
)
@Slf4j
public class RobadorPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private RobadorConfig config;

    private static final Random num = new Random(92371267);
    private long t_click;
    private long lastClick = 0;
    private boolean encendido = false;
    private int nAbrir = 20;
    private boolean flagBankear = false;
    private int timeout = 0;
    private boolean caminoAlBank = false;
    private boolean identEstado = false;
    private final WorldPoint[] ptsCasa = {new WorldPoint(2671, 3316, 0), new WorldPoint(2672, 3316, 0),
            new WorldPoint(2670, 3317, 0),new WorldPoint(2671, 3317, 0),
            new WorldPoint(2672,3317,0), new WorldPoint(2669,3317,0)};

    private final WorldArea[] areaCasa = {new WorldArea(2671,3315,4,5,0)
            ,new WorldArea(2668,3317,3,3,0), new WorldArea(2670,3316,1,1,0)};



    @Provides
    RobadorConfig getConfig(ConfigManager configManager){
        return (RobadorConfig) configManager.getConfig(RobadorConfig.class);
    }


    @Override
    protected void startUp() throws Exception {
        encendido = true;
        identEstado=true;
        lastClick = 0;
        log.info("Encendido");
    }

    @Override
    protected void shutDown() throws Exception {
        encendido = false;
        lastClick = 0;
        log.info("Apagado");
    }

    @Subscribe
    void onClientTick(ClientTick event) {
        if(identEstado) return;
        if (!encendido) return;
        if (flagBankear) return;
        long tiempoActual = System.currentTimeMillis();
        //Random n = new Random(System.currentTimeMillis());
        //t_click = (long) Math.floor(Math.random() * (2000 - 400 + 1)) + 400;
        if (lastClick + t_click < tiempoActual) {
            t_click = (long) (800 + num.nextInt(800));
            lastClick = tiempoActual;
            if (client.getBoostedSkillLevel(Skill.HITPOINTS) > config.HpComer()) {
                log.info("Robando T= {}", t_click);
                Optional<Widget> coinPouch = Inventory.search().nameContains("Coin pouch").first();
                if (coinPouch.isPresent()) {
                    if (coinPouch.get().getItemQuantity() >= nAbrir) {
                        nAbrir = num.nextInt(8) + 20;
                        InventoryInteraction.useItem(22531, "Open-all");
                        return;
                    }
                }

                NPCInteraction.interact(x -> {
                    if (x.getName() != null) {
                        if (x.getName().contains("Knight of Ardougne")) {
                            return x.getWorldLocation().isInArea(new WorldArea(2671, 3315, 2, 1, 0));
                        }
                    }
                    return false;
                }, "Pickpocket");
            } else {
                if (Inventory.search().withAction("Eat").empty()) {
                    flagBankear = true;
                    caminoAlBank = true;
                }
                InventoryInteraction.useItem(x -> {
                    if (x.getActions() != null) {
                        return Arrays.stream(x.getActions()).anyMatch(y -> (y != null) ? y.contains("Eat") : false);
                    }
                    return false;
                },"Eat");
            }
        }
    }

    @Subscribe
    void onGameTick(GameTick event) {
        if (timeout > 0) {
            timeout--;
            return;
        }
        if (identEstado) {
            if(client.getLocalPlayer()!=null){
                if (estoyEn(areaCasa,client) ) {
                    flagBankear = false;
                    caminoAlBank = false;
                } else if (!estoyEn(areaCasa, client)) {
                    if (Inventory.search().withId(config.foodId()).empty()) {
                        flagBankear = true;
                        caminoAlBank = true;
                    } else if (!Inventory.search().withId(config.foodId()).empty()) {
                        flagBankear = true;
                        caminoAlBank = false;
                    }
                }
            }
            identEstado=false;
        }

        if (flagBankear) {
            log.info("meow");
            if (client.getWidget(786442) == null && caminoAlBank) {
                log.info("abierta: {}",TileObjects.search().atLocation(new WorldPoint(2669, 3316, 0)).empty());

                if(estoyEn(areaCasa,client) && !TileObjects.search().atLocation(new WorldPoint(2669, 3316, 0)).empty()){ //en casa con puerta cerrada
                    log.info("Abriendo Puerta");
                    Optional<TileObject> puerta= TileObjects.search().atLocation(new WorldPoint(2669, 3316, 0)).first();
                    puerta.ifPresent(x->TileObjectInteraction.interact(x,"Open"));
                } else if (estoyEn(areaCasa,client) && TileObjects.search().atLocation(new WorldPoint(2669, 3316, 0)).empty()) {
                    log.info("Camino al Bank");
                    Optional<TileObject> bank = TileObjects.search().atLocation(2656,3283,0).first();
                    bank.ifPresent(x->TileObjectInteraction.interact(x, "Bank"));

                } else if (!estoyEn(areaCasa,client) ) {
                    if(isIdle(client.getLocalPlayer())){
                        log.info("Reanudando Camino al Bank");
                        Optional<TileObject> bank = TileObjects.search().atLocation(2656,3283,0).first();
                        bank.ifPresent(x->TileObjectInteraction.interact(x, "Bank"));
                    }
                }
            } else if (client.getWidget(786442) != null && caminoAlBank) {
                log.info("bank abierto, hay food: {}",BankInventory.search().withId(config.foodId()).empty());
                if (BankInventory.search().withId(config.foodId()).empty()) {
                    log.info("Sacando food");
                    BankInteraction.useItem(x -> x.getItemId()== config.foodId(), "Withdraw-all");
                } else {
                    log.info("Curando en el bank");
                    if (client.getBoostedSkillLevel(Skill.HITPOINTS) < (client.getRealSkillLevel(Skill.HITPOINTS) - 10)) {
                        BankInventoryInteraction.useItem(x -> x.getItemId()==config.foodId(), "Eat");
                    } else {
                        log.info("Curado");
                        caminoAlBank = false;
                    }

                }
            } else {
                log.info("devuelta al spot fino");
                WorldArea[] areaPuertaOut = new WorldArea[]{new WorldArea(2667,3314,3,3,0)};
                if (isIdle(client.getLocalPlayer()) && !estoyEn(areaPuertaOut,client) ){
                    MousePackets.queueClickPacket();
                    MovementPackets.queueMovement(2669, 3315, false);
                } else if (isIdle(client.getLocalPlayer()) && estoyEn(areaPuertaOut,client)) {
                    if(TileObjects.search().atLocation(new WorldPoint(2669, 3316, 0)).empty()){
                        MousePackets.queueClickPacket();
                        MovementPackets.queueMovement(2672,3316,false);
                        flagBankear = false;
                    }else {
                        Optional<TileObject> puerta = TileObjects.search().atLocation(new WorldPoint(2669, 3316, 0)).first();
                        puerta.ifPresent(x->TileObjectInteraction.interact(x,"Open"));
                    }
                }
            }
            timeout = num.nextInt(4) + 1;
            log.info("timeout {}",timeout);
        }
    }

    public boolean isIdle (Player gamer){
        return gamer.getIdlePoseAnimation() == gamer.getPoseAnimation();
    }

    public boolean estoyEn(WorldPoint tile){
        return client.getLocalPlayer().getWorldLocation().equals(tile);
    }

    public boolean estoyEn(WorldArea[] areas, Client clt){
        WorldPoint playerWp = clt.getLocalPlayer().getWorldLocation();
        for (WorldArea area : areas) {
            if(playerWp.isInArea(area)){
                return true;
            }
        }
        return false;
    }
}
