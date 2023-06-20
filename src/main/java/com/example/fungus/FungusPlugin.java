package com.example.fungus;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;

import javax.inject.Inject;

public class FungusPlugin extends Plugin {
    @Inject
    private Client client;

    private State estado;

    private int timeout;

    private enum State {
        BANKING,
        HACIA_SPOT,
        HACIA_BANK,
        CALLAMPEANDO
    }

    @Subscribe
    void onGameTick(GameTick event) {
        if (timeout > 0) {
            timeout--;
            return;
        }

        Player player = client.getLocalPlayer();

        switch (estado) {
            case CALLAMPEANDO:
                if (player.getAnimation()==-1) {
                }

        }

    }





}
