package com.example.worldFinder;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
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
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
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

    @Inject
    private WorldFinderConfig config;

    @Provides
    WorldFinderConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(WorldFinderConfig.class);
    }

    private boolean encendido = false;

    private WorldPoint tileInicial = null;
    private WorldPoint tileFinal = null;

    private int timeout = 0;

    private int mundoOrigen = 0;
    private int intentos = 0;

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

    private final KeyListener botonEncendido = new HotkeyListener( () -> config.prendido() ) {

        @Override
        public void hotkeyPressed() {
            clientThread.invoke(() -> {
                if (client.getGameState() != GameState.LOGGED_IN) {
                    return;
                }

                encendido = !encendido;
                if (encendido) {
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Encendido", Color.GREEN), "");
                } else {
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Apagado", Color.RED), "");
                }
                intentos = 0;
            });
        }
    };


    @Subscribe
    void onGameTick(GameTick event) {
        if (!encendido) return;

        if (intentos > 3) {
            encendido = false;
            intentos = 0;
            log.info("failsafe apagando");
            return;
        }

        if (intentos > 0) {
            if (client.getWorld() != mundoOrigen) {
                intentos = 0;
                log.info("Se Hopeo");
                timeout = 3;
            }
        }

        if (timeout > 0) {
            timeout--;
            return;
        }

        if (tileInicial != null && tileFinal != null) {
            int areaX = Math.min(tileInicial.getX(),tileFinal.getX());
            int areaY = Math.min(tileInicial.getY(),tileFinal.getY());
            WorldArea area = new WorldArea(areaX,areaY,Math.abs(tileFinal.getX()-tileInicial.getX())+1, Math.abs(tileFinal.getY()-tileInicial.getY())+1, client.getPlane());

            List<Player> jugadores = client.getPlayers();
            if (jugadores.stream().anyMatch(x -> x.getWorldLocation().isInArea(area))) {
                if (intentos == 0) {
                    log.info("guardando info");
                    mundoOrigen = client.getWorld();
                }
                hop(false);
                timeout = 15;
                intentos++;
            } else {
                log.info("Se encontro mundo!");
                encendido = false;
            }
        }

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
