package com.example.Callisto;

import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.InteractionApi.InteractionHelper;
import com.example.InteractionApi.NPCInteraction;
import com.example.Packets.NPCPackets;
import com.example.PacketUtils.WidgetInfoExtended;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Projectile;
import net.runelite.api.Varbits;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@PluginDescriptor(
        name = "<html>[<font color=\"#FA4444\">P</font>] Callisto</html>",
        enabledByDefault = false,
        tags = {"pajau"}
)
public class CallistoPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private CallistoConfig callistoConfig;
    private int timeout = 0;

    private Projectile atkMagia = null;

    private boolean flagMagic=false;
    private boolean flagRanged = false;
    private boolean flagMelee = false;

    @Provides
    CallistoConfig getConfig(ConfigManager configManager) {
        return (CallistoConfig) configManager.getConfig(CallistoConfig.class);
    }

    @Subscribe
    void onAnimationChanged(AnimationChanged event) {
        if (event.getActor().getAnimation() == 10013 && !flagMelee) {
            if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MISSILES) != 1) { //ranged
                InteractionHelper.toggleNormalPrayer(35454998);
            }
        }

        if(event.getActor().getAnimation() == 10015){
            //barragaTimeout=1;
            //NPCPackets.queueWidgetOnNPC((NPC)event.getActor(), Objects.requireNonNull(client.getWidget(WidgetInfoExtended.SPELL_ICE_BARRAGE.getPackedId())));
        }
    }

    @Subscribe
    void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc().getId() == 6609 && client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MISSILES) == 0) {
            InteractionHelper.toggleNormalPrayer(35454998);
            if (client.getVarbitValue(Varbits.PRAYER_RIGOUR)==0 && callistoConfig.actRigour()) {
                InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.PRAYER_RIGOUR.getPackedId());
            }
        }
    }

    @Subscribe
    void onNpcDespawned(NpcDespawned event) {
        if (event.getNpc().getId() == 6609 && client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MISSILES) == 1) {
            InteractionHelper.toggleNormalPrayer(35454998);
        } else if (event.getNpc().getId() == 6609 && client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MAGIC) == 1) {
            InteractionHelper.toggleNormalPrayer(35454997);
        }
        if (event.getNpc().getId() == 6609 && client.getVarbitValue(Varbits.PRAYER_RIGOUR) == 1) {
            InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.PRAYER_RIGOUR.getPackedId());
        }
    }

    private int barragaTimeout=0;
    private int checkTimeout =0;



    @Subscribe
    void onGameTick(GameTick event) {
        if (timeout > 0) {
            timeout--;
            return;
        }

        Optional<NPC> calisto = NPCs.search().withId(6609).first();
        if (barragaTimeout>0) {
            barragaTimeout--;
            if (barragaTimeout == 0) {
                if (calisto.isPresent()) {
                    if (client.getWidget(WidgetInfoExtended.SPELL_ICE_BARRAGE.getPackedId()) == null) {
                        log.info("no se encontro el ice barrage");
                        return;
                    }
                    NPCPackets.queueWidgetOnNPC(calisto.get(), Objects.requireNonNull(client.getWidget(WidgetInfoExtended.SPELL_ICE_BARRAGE.getPackedId())));
                    log.info("se procede a Congelar");
                }
            }
        }
        if (checkTimeout > 0) {
            checkTimeout--;
        }


        if (calisto.isPresent() && checkTimeout <= 0) {
            if ( calisto.get().getPoseAnimation() == 10009 ) {
                barragaTimeout = callistoConfig.barrageTimeout();
                checkTimeout = barragaTimeout + 5;
            } else if (calisto.get().getAnimation() == 10015) {
                barragaTimeout = callistoConfig.grrBarrageTimeout();
                checkTimeout = barragaTimeout + 5;
            }

            if (client.getLocalPlayer().getWorldLocation().distanceTo(calisto.get().getWorldArea()) <= 2) {
                if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MELEE) == 0 && !flagMagic) {
                    InteractionHelper.toggleNormalPrayer(35454999);
                    flagMelee = true;
                }
            } else {
                flagMelee = false;
            }


        }


    }

    @Subscribe
    void onProjectileMoved(ProjectileMoved event) {
        Projectile pj=event.getProjectile();
        if(pj==null) return;
        if (pj.getId() == 133) {
            if(pj.getInteracting().getName() == null || client.getLocalPlayer().getName() == null) return;
            if (pj.getInteracting().getName().contains(client.getLocalPlayer().getName())) {
                if(client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MAGIC)==0 && pj.getRemainingCycles()>15 && !flagMagic){
                    InteractionHelper.toggleNormalPrayer(35454997);
                    flagMagic=true;
                } else if (pj.getRemainingCycles() == 0) {
                    InteractionHelper.toggleNormalPrayer(35454998);
                    flagMagic = false;
                }

            }


        }
    }

}
