package com.example.worldFinder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Objects;

@Slf4j
@Getter
@PluginDescriptor(
        name = "World Finder",
        tags = {"pajau"}
)
public class WorldFinderPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private WorldService worldService;

    @Inject
    private KeyManager keyManager;

    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private WorldFinderOverlay worldFinderOverlay;

    private boolean encendido = false;

    private WorldPoint tileInicial = null;
    private WorldPoint tileFinal = null;

    private int timeout = 0;

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(worldFinderOverlay);
        keyManager.registerKeyListener(botonEncendido);
        encendido = false;
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(worldFinderOverlay);
        keyManager.unregisterKeyListener(botonEncendido);
        encendido = false;
    }

    private int mundito = 477;

    private final KeyListener botonEncendido = new HotkeyListener(() -> new Keybind(KeyEvent.VK_M, 0)) {

        @Override
        public void hotkeyPressed() {
            clientThread.invoke(() -> {
                encendido = !encendido;
                if (encendido) {
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Encendido", Color.GREEN), "");
                } else {
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Apagado", Color.RED), "");
                }
            });
        }
    };

    @Subscribe
    void onGameTick(GameTick event) {
        if (!encendido) return;

        if (tileInicial != null && tileFinal != null) {
            int areaX = Math.min(tileInicial.getX(),tileFinal.getX());
            int areaY = Math.min(tileInicial.getY(),tileFinal.getY());
            WorldArea area = new WorldArea(areaX,areaY,Math.abs(tileFinal.getX()-tileInicial.getX())+1, Math.abs(tileFinal.getY()-tileInicial.getY())+1, client.getPlane());

            List<Player> jugadores = client.getPlayers();
            if (jugadores.stream().anyMatch(x -> x.getWorldLocation().isInArea(area)) ) {

            }
        }

        if (timeout > 0) {
            timeout--;
            return;
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

    @Subscribe
    void onMenuEntryAdded(MenuEntryAdded event) {

        final boolean shiftPressed = client.isKeyPressed(KeyCode.KC_SHIFT);
        if (shiftPressed && event.getOption().contains("Walk here")) {
            if (tileInicial != null && tileFinal != null) {
                client.createMenuEntry(-2).setOption(ColorUtil.wrapWithColorTag("Clear tiles",Color.PINK))
                        .setType(MenuAction.RUNELITE)
                        .onClick(x -> {
                            tileFinal = null;
                            tileInicial = null;
                        });
                return;
            }
            final Tile selectedTile = client.getSelectedSceneTile();
            if (selectedTile==null) return;

            client.createMenuEntry(-1).setOption(tileInicial==null? "Tile Inicial":"Tile Final")
                    .setType(MenuAction.RUNELITE)
                    .setTarget(event.getTarget())
                    .onClick(x ->{
                        if (tileInicial == null) {
                            tileInicial = client.getSelectedSceneTile().getWorldLocation();
                        } else {
                            tileFinal = client.getSelectedSceneTile().getWorldLocation();
                        }
                    });
        }
    }

}
