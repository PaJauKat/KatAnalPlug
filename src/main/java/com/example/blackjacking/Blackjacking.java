package com.example.blackjacking;

import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.sun.jna.platform.mac.SystemB;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Optional;

@Slf4j
@PluginDescriptor(
        name = "Blackjacking Thugs",
        description = "KO and pickpocket Thugs. Use jug of wine",
        tags = {"pajau"}
)
public class Blackjacking extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private KeyManager keyManager;

    private int timeout = 0;
    private boolean prendido = false;
    private int counter = 0;
    private WorldArea thugSpot1 = new WorldArea(3340, 2953, 5, 4, 0);

    private State estado;

    private enum State {ROBANDO, RESTOCK, STARTING}

    private final KeyListener botonEncendido = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F10, 0)) {
        @Override
        public void hotkeyPressed() {
            clientThread.invoke(() -> {
                prendido = !prendido;
                counter = 0;
                if (prendido) {
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("ON", Color.GREEN), "");
                } else
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("OFF", Color.RED), "");
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



    @Subscribe
    void onGameTick(GameTick event) {
        if (!prendido) return;


        if (timeout > 0) {
            timeout--;
            return;
        }



        Optional<NPC> thug = NPCs.search().nameContains("Thug").withinWorldArea(thugSpot1).first();

        if (estado == State.ROBANDO) {
            if (thug.isPresent()) {
                if (counter > 0) {
                    if (thug.get().getAnimation() == 838) {
                        if (counter == 2 && client.getBoostedSkillLevel(Skill.HITPOINTS) < 50) {
                            log.info("Comiendo");
                            InventoryInteraction.useItem("Jug of wine", "Drink");
                        } else {
                            log.info("robando");
                            NPCInteraction.interact(thug.get(), "Pickpocket");
                        }
                        counter--;
                    } else if (thug.get().getInteracting() != null) {
                        log.info("KO");
                        NPCInteraction.interact(thug.get(), "Knock-Out");
                        counter = 2;
                    }

                } else {
                    log.info("KO");
                    NPCInteraction.interact(thug.get(), "Knock-Out");
                    counter = 2;
                }
                timeout = 1;
            }
        }

        else if (estado == State.RESTOCK) {

        }


    }

}
