//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.hydra;

import com.google.inject.Provides;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;


@PluginDescriptor(
        name = "Hydra Helper",
        enabledByDefault = false,
        description = "Overlays for normal Hydras.",
        tags = {"hydra", "helper", "baby", "small", "normal", "regular"}
)
public class HydraPlugin extends Plugin {
    static final Set<HydraAnimation> VALID_HYDRA_ANIMATIONS;
    private static final String CONFIG_GROUP_NAME = "hydra";
    private static final String CONFIG_ITEM_ATTACK_COUNTER = "attackCounterOverlay";
    private static final String CONFIG_ITEM_PRAYER_OVERLAY = "prayerOverlay";
    private static final String CONFIG_ITEM_POISON_PROJECTILE_OVERLAY = "poisonProjectileOverlay";
    private static final String CONFIG_ITEM_BOLD_ATTACK_COUNTER_OVERLAY = "boldAttackCounterOverlay";
    private static final String CONFIG_ITEM_MIRROR_MODE_COMPATABILITY = "mirrorMode";
    private static final String NPC_NAME_HYDRA = "Hydra";
    private static final int HYDRA_REGION_1 = 5279;
    private static final int HYDRA_REGION_2 = 5280;
    @Inject
    private Client client;
    @Inject
    private HydraConfig hydraConfig;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private HydraAttackCounterOverlay hydraAttackCounterOverlay;
    @Inject
    private HydraPrayerOverlay hydraPrayerOverlay;
    @Inject
    private HydraPrayerAttackCounterOverlay hydraPrayerAttackCounterOverlay;
    @Inject
    private HydraPoisonOverlay hydraPoisonOverlay;
    private final Map<Integer, Hydra> hydras = new HashMap();
    private final Map<LocalPoint, Projectile> poisonProjectiles = new HashMap();
    private NPC interactingNpc = null;

    public HydraPlugin() {
    }

    @Provides
    HydraConfig provideConfig(ConfigManager configManager) {
        return (HydraConfig)configManager.getConfig(HydraConfig.class);
    }

    protected void startUp() {
        if (this.hydraConfig.isAttackCounterOverlay()) {
            this.overlayManager.add(this.hydraAttackCounterOverlay);
        }

        if (this.hydraConfig.isPrayerOverlay()) {
            this.overlayManager.add(this.hydraPrayerOverlay);
            this.overlayManager.add(this.hydraPrayerAttackCounterOverlay);
        }

        if (this.hydraConfig.isPoisonOverlay()) {
            this.overlayManager.add(this.hydraPoisonOverlay);
        }

        this.hydraAttackCounterOverlay.setBoldAttackCounterOverlay(this.hydraConfig.isBoldAttackCounterOverlay());
        this.hydraAttackCounterOverlay.setHydras(this.hydras);
        this.hydraPrayerOverlay.setHydras(this.hydras);
        this.hydraPrayerAttackCounterOverlay.setHydras(this.hydras);
        this.hydraPoisonOverlay.setPoisonProjectiles(this.poisonProjectiles);
        this.resetHydras();
        this.poisonProjectiles.clear();
    }

    protected void shutDown() {
        this.overlayManager.remove(this.hydraAttackCounterOverlay);
        this.overlayManager.remove(this.hydraPrayerOverlay);
        this.overlayManager.remove(this.hydraPrayerAttackCounterOverlay);
        this.overlayManager.remove(this.hydraPoisonOverlay);
        this.resetHydras();
        this.poisonProjectiles.clear();
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("hydra")) {
            boolean newConfigValue = Boolean.parseBoolean(event.getNewValue());
            switch (event.getKey()) {
                case "attackCounterOverlay":
                    if (newConfigValue) {
                        this.overlayManager.add(this.hydraAttackCounterOverlay);
                    } else {
                        this.overlayManager.remove(this.hydraAttackCounterOverlay);
                    }
                    break;
                case "prayerOverlay":
                    if (newConfigValue) {
                        this.overlayManager.add(this.hydraPrayerOverlay);
                        this.overlayManager.add(this.hydraPrayerAttackCounterOverlay);
                    } else {
                        this.overlayManager.remove(this.hydraPrayerOverlay);
                        this.overlayManager.remove(this.hydraPrayerAttackCounterOverlay);
                    }
                    break;
                case "poisonProjectileOverlay":
                    if (newConfigValue) {
                        this.overlayManager.add(this.hydraPoisonOverlay);
                    } else {
                        this.overlayManager.remove(this.hydraPoisonOverlay);
                    }
                    break;
                case "boldAttackCounterOverlay":
                    this.hydraAttackCounterOverlay.setBoldAttackCounterOverlay(this.hydraConfig.isBoldAttackCounterOverlay());
            }

        }
    }

    @Subscribe
    private void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();
        if (isActorHydra(npc)) {
            this.addHydra(npc);
        }

    }

    @Subscribe
    private void onNpcDespawned(NpcDespawned event) {
        NPC npc = event.getNpc();
        if (isActorHydra(npc)) {
            this.removeHydra(npc);
            this.poisonProjectiles.clear();
        }

    }

    @Subscribe
    private void onInteractingChanged(InteractingChanged event) {
        Actor source = event.getSource();
        if (isActorHydra(source)) {
            NPC npc = (NPC)source;
            this.addHydra(npc);
            this.updateInteractingNpc(npc);
        }
    }

    @Subscribe
    private void onAnimationChanged(AnimationChanged event) {
        Actor actor = event.getActor();
        if (isActorHydra(actor)) {
            NPC npc = (NPC)event.getActor();
            this.addHydra(npc);
            this.updateInteractingNpc(npc);

            HydraAnimation hydraAnimation;
            try {
                hydraAnimation = HydraAnimation.fromId(npc.getAnimation());
            } catch (IllegalArgumentException var6) {
                hydraAnimation = null;
            }

            if (hydraAnimation != null && VALID_HYDRA_ANIMATIONS.contains(hydraAnimation)) {
                Hydra hydra = (Hydra)this.hydras.get(npc.getIndex());
                if (hydra.getHydraAnimation() == null) {
                    hydra.setHydraAnimation(hydraAnimation);
                } else if (!Objects.equals(hydra.getHydraAnimation(), hydraAnimation)) {
                    hydra.setHydraAnimation(hydraAnimation);
                    hydra.resetAttackCount();
                }

                hydra.updateAttackCount();
                if (!this.poisonProjectiles.isEmpty()) {
                    this.updatePoisonProjectiles();
                }

            }
        }
    }

    @Subscribe
    private void onProjectileMoved(ProjectileMoved event) {
        if (this.interactingNpc != null && this.client.getGameCycle() < event.getProjectile().getStartCycle()) {
            Projectile projectile = event.getProjectile();
            int projectileId = projectile.getId();
            if (projectileId == 1644) {
                this.poisonProjectiles.put(event.getPosition(), projectile);
            }

        }
    }

    private void updatePoisonProjectiles() {
        Set<LocalPoint> expiredPoisonProjectiles = new HashSet();
        Iterator var2 = this.poisonProjectiles.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<LocalPoint, Projectile> entry = (Map.Entry)var2.next();
            if (((Projectile)entry.getValue()).getEndCycle() < this.client.getGameCycle()) {
                expiredPoisonProjectiles.add((LocalPoint)entry.getKey());
            }
        }

        var2 = expiredPoisonProjectiles.iterator();

        while(var2.hasNext()) {
            LocalPoint projectileLocalPoint = (LocalPoint)var2.next();
            this.poisonProjectiles.remove(projectileLocalPoint);
        }

    }

    boolean isPlayerAtHydraRegion() {
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return false;
        } else {
            WorldPoint worldPoint = player.getWorldLocation();
            if (worldPoint == null) {
                return false;
            } else {
                int regionId = worldPoint.getRegionID();
                return regionId == 5279 || regionId == 5280;
            }
        }
    }

    private static boolean isActorHydra(Actor actor) {
        return Objects.equals(actor.getName(), "Hydra");
    }

    private void updateInteractingNpc(NPC npc) {
        if (!Objects.equals(this.interactingNpc, npc) && Objects.equals(npc.getInteracting(), this.client.getLocalPlayer())) {
            this.interactingNpc = npc;
        }

    }

    private void addHydra(NPC npc) {
        int npcIndex = npc.getIndex();
        if (!this.hydras.containsKey(npcIndex)) {
            this.hydras.put(npcIndex, new Hydra(npc));
        }

    }

    private void removeHydra(NPC npc) {
        int npcIndex = npc.getIndex();
        this.hydras.remove(npcIndex);
        if (Objects.equals(this.interactingNpc, npc)) {
            this.interactingNpc = null;
        }

    }

    private void resetHydras() {
        this.hydras.clear();
        this.interactingNpc = null;
    }

    NPC getInteractingNpc() {
        return this.interactingNpc;
    }

    static {
        VALID_HYDRA_ANIMATIONS = EnumSet.of(HydraAnimation.RANGE, HydraAnimation.MAGIC);
    }
}
