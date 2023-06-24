package com.example.Caminador;

import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.Keybind;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import java.awt.event.KeyEvent;

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
                }
            });
        }
    };
}
