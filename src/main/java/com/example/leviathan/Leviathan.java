package com.example.leviathan;

import com.example.InteractionApi.InteractionHelper;
import com.example.PacketUtils.WidgetInfoExtended;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;

import javax.inject.Inject;
import java.util.*;

public class Leviathan extends Plugin {
    @Inject
    private Client client;

    private Projectile[] projectiles = new Projectile[5];
    private int c = 0;
    private NPC leviathan = null;

    @Subscribe
    void onProjectileMoved(ProjectileMoved event) {
        if (client.getGameCycle() < event.getProjectile().getStartCycle()) {
            projectiles[c]=event.getProjectile();
            c = (c+1)%5;

        }
    }

    @Subscribe
    void onGameTick(GameTick event) {
        if (leviathan!= null && leviathan.isDead()) {
            apagarPrayers(Prayer.PROTECT_FROM_MAGIC,Prayer.PROTECT_FROM_MISSILES,Prayer.RIGOUR);
        }
        if (leviathan == null) {
            return;
        }

        Optional<Projectile> proj2Pray = Arrays.stream(projectiles).min(Comparator.comparingInt(x->Math.abs(30-x.getRemainingCycles())));
        if (proj2Pray.isPresent()) {
            if (proj2Pray.get().getId() == 2489) {
                checkPray(Prayer.PROTECT_FROM_MAGIC);
            } else if (proj2Pray.get().getId() == 2487) {
                checkPray(Prayer.PROTECT_FROM_MISSILES);
            } else if (proj2Pray.get().getId() == 2488) {
                checkPray(Prayer.PROTECT_FROM_MELEE);
            }
        }
    }

    void checkPray(Prayer prayer) {
        if (prayer.equals(Prayer.PROTECT_FROM_MAGIC)) {
            if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MAGIC) == 0) {
                InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.PRAYER_PROTECT_FROM_MAGIC.getPackedId());
            }
        } else if (prayer.equals(Prayer.PROTECT_FROM_MISSILES)) {
            if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MISSILES) == 0) {
                InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.PRAYER_PROTECT_FROM_MISSILES.getPackedId());
            }
        }else if (prayer.equals(Prayer.PROTECT_FROM_MELEE)) {
            if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MELEE) == 0) {
                InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.PRAYER_PROTECT_FROM_MELEE.getPackedId());
            }
        }

    }

    void apagarPrayers(Prayer... prayers) {
        for (int i = 0; i < prayers.length; i++) {
            if (client.getVarbitValue(prayers[i].getVarbit()) == 1) {
                InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.valueOf("PRAYER_"+prayers[i].name()).getPackedId());
            }
        }
    }

    @Subscribe
    void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc().getId() == 12241) {
            leviathan = event.getNpc();
        }
    }

    @Subscribe
    void onNpcDespawned(NpcDespawned event) {
        if (event.getNpc().getId() == 12241) {
            leviathan = null;
        }
    }


}
