package com.example.spec;

import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.Keybind;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import java.awt.event.KeyEvent;


@Slf4j
@PluginDescriptor(
        name = "Special anal",
        tags = {"pajau"}
)
public class SpecPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private KeyManager keyManager;

    @Inject
    private ClientThread clientThread;


    @Override
    protected void startUp() throws Exception {
        this.keyManager.registerKeyListener(botonSpec);
    }

    @Override
    protected void shutDown() throws Exception {
        this.keyManager.unregisterKeyListener(botonSpec);
    }


    private KeyListener botonSpec = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F8, 0)) {

        @Override
        public void hotkeyPressed() {
            clientThread.invoke(() -> {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetActionPacket(1, 10485795, -1, -1);
                log.info("speckiando");
            });
        }
    };


}
