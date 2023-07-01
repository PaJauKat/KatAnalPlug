package com.example.Caminador;

import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.EthanApiPlugin;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@PluginDescriptor(
        name = "Caminador",
        enabledByDefault = false,
        tags = {"pajau"}
)
public class TestCaminadorPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private KeyManager keyManager;

    @Inject
    private Caminador pat;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private TestCaminadorOverlay testCaminadorOverlay;

    @Getter
    public WorldPoint tileCaminado;

    @Getter
    public ArrayList<WorldPoint> kat = new ArrayList<>();



    @Override
    protected void startUp() throws Exception {
        overlayManager.add(testCaminadorOverlay);
        keyManager.registerKeyListener(bottonCaminar);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(testCaminadorOverlay);
        keyManager.unregisterKeyListener(bottonCaminar);
    }

    private final KeyListener bottonCaminar = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F7,0)) {
        @Override
        public void hotkeyPressed() {
            clientThread.invoke(() -> {
                if (client.getSelectedSceneTile() != null) {
                    tileCaminado = pat.getNextWp(client.getSelectedSceneTile().getWorldLocation(),2,client);
                    kat = EthanApiPlugin.pathToGoal(tileCaminado,new HashSet<>());
                    log.info("anal: {}",kat);
                }
            });
        }
    };




    private int meow = 0;
    @Subscribe
    void onGameTick(GameTick event) {
        meow++;
        if (meow == 3){
            //Optional<NPC> padre = NPCs.search().withId(2812).first();

            /*Player player = client.getLocalPlayer();
            if (client.getSelectedSceneTile() != null) {
                log.info("kat {}",EthanApiPlugin.canPathToTile(client.getSelectedSceneTile().getWorldLocation()).getDistance());
                log.info("katita {}",EthanApiPlugin.canPathToTile(client.getSelectedSceneTile().getWorldLocation()).isReachable());
                if (NPCs.search().withId(7144).first().isPresent() ) {
                    if (EthanApiPlugin.getHeadIcon(NPCs.search().withId(7144).first().get()) != null)  {
                        log.info("patas {}", Objects.requireNonNull(EthanApiPlugin.getHeadIcon(NPCs.search().withId(7144).first().get())).name());
                    }
                }

            }*/



            /*player.ifPresent(x -> {
               log.info("anal {}",EthanApiPlugin.pathLength(x));
            });*/
            meow=0;
        }



    }
}
