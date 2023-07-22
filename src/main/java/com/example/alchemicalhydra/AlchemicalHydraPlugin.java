//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.alchemicalhydra;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InteractionHelper;
import com.example.PacketUtils.WidgetInfoExtended;
import com.example.Packets.MousePackets;
import com.example.alchemicalhydra.entity.Hydra;
import com.example.alchemicalhydra.entity.HydraPhase;
import com.example.alchemicalhydra.overlay.AttackOverlay;
import com.example.alchemicalhydra.overlay.PrayerOverlay;
import com.example.alchemicalhydra.overlay.SceneOverlay;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@Singleton
@PluginDescriptor(
        name = "<html>[<font color=\"#59D634\">P</font>] Alchemical Hydra</html>",
        enabledByDefault = false,
        description = "A plugin for the Alchemical Hydra boss.",
        tags = {"alchemical", "hydra"}
)
public class AlchemicalHydraPlugin extends Plugin {
    private static final String MESSAGE_NEUTRALIZE = "The chemicals neutralise the Alchemical Hydra's defences!";
    private static final String MESSAGE_STUN = "The Alchemical Hydra temporarily stuns you.";
    private static final int[] HYDRA_REGIONS = new int[]{5279, 5280, 5535, 5536};
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AttackOverlay attackOverlay;
    @Inject
    private SceneOverlay sceneOverlay;
    @Inject
    private PrayerOverlay prayerOverlay;

    @Inject
    private AlchemicalHydraConfig config;

    private boolean atHydra;
    private Hydra hydra;
    public static final int HYDRA_1_1 = 8237;
    public static final int HYDRA_1_2 = 8238;
    public static final int HYDRA_LIGHTNING = 8241;
    public static final int HYDRA_2_1 = 8244;
    public static final int HYDRA_2_2 = 8245;
    public static final int HYDRA_FIRE = 8248;
    public static final int HYDRA_3_1 = 8251;
    public static final int HYDRA_3_2 = 8252;
    public static final int HYDRA_4_1 = 8257;
    public static final int HYDRA_4_2 = 8258;
    int fountainTicks = -1;
    int lastFountainAnim = -1;
    private final Map<LocalPoint, Projectile> poisonProjectiles = new HashMap();
    private int lastAttackTick = -1;
    private final Set<GameObject> vents = new HashSet();

    public AlchemicalHydraPlugin() {
    }

    @Provides
    AlchemicalHydraConfig provideConfig(ConfigManager configManager) {
        return (AlchemicalHydraConfig)configManager.getConfig(AlchemicalHydraConfig.class);
    }

    protected void startUp() {
        if (this.client.getGameState() == GameState.LOGGED_IN && this.isInHydraRegion()) {
            this.init();
        }

    }

    private void init() {
        this.atHydra = true;
        this.addOverlays();
        Iterator<NPC> var1 = this.client.getNpcs().iterator();
        log.info("q wea: {}",this.client.getNpcs());

        while(var1.hasNext()) {
            NPC npc = (NPC)var1.next();
            this.onNpcSpawned(new NpcSpawned(npc));
        }

    }

    protected void shutDown() {
        this.atHydra = false;
        this.removeOverlays();
        this.hydra = null;
        this.poisonProjectiles.clear();
        this.lastAttackTick = -1;
        this.fountainTicks = -1;
        this.vents.clear();
        this.lastFountainAnim = -1;
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        GameState gameState = event.getGameState();
        switch (gameState) {
            case LOGGED_IN:
                if (this.isInHydraRegion()) {
                    if (!this.atHydra) {
                        this.init();
                    }
                } else if (this.atHydra) {
                    this.shutDown();
                }
                break;
            case HOPPING:
            case LOGIN_SCREEN:
                if (this.atHydra) {
                    this.shutDown();
                }
        }

    }

    @Subscribe
    private void onGameObjectSpawned(GameObjectSpawned event) {
        if (this.isInHydraRegion()) {
            GameObject gameobject = event.getGameObject();
            int id = gameobject.getId();
            if (id == 34568 || id == 34569 || id == 34570) {
                this.vents.add(gameobject);
            }

        }
    }

    @Subscribe
    private void onGameObjectDespawned(GameObjectDespawned event) {
        GameObject gameobject = event.getGameObject();
        this.vents.remove(gameobject);
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (this.hydra != null && this.hydra.getNpc() != null && config.prayProtect()
                && Arrays.stream(client.getMapRegions()).anyMatch(x-> Arrays.stream(HYDRA_REGIONS).anyMatch(y->x==y)) && client.isInInstancedRegion()) {
            if (hydra.getNextAttack() != null) {
                if (!client.isPrayerActive(hydra.getNextAttack().getPrayer())) {
                    Prayer prayer = hydra.getNextAttack().getPrayer();
                    InteractionHelper.toggleNormalPrayer(prayer == Prayer.PROTECT_FROM_MAGIC ? WidgetInfoExtended.PRAYER_PROTECT_FROM_MAGIC.getPackedId() :
                            WidgetInfoExtended.PRAYER_PROTECT_FROM_MISSILES.getPackedId());
                }
            }
            if (client.getVarbitValue(Varbits.PRAYER_RIGOUR)==0) {
                InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.PRAYER_RIGOUR.getPackedId());
            }

            HydraPhase phase = this.hydra.getPhase();
            int animationId = EthanApiPlugin.getAnimation(this.hydra.getNpc());
            if (animationId == phase.getDeathAnimation2() && phase != HydraPhase.FLAME || animationId == phase.getDeathAnimation1() && phase == HydraPhase.FLAME) {
                switch (phase) {
                    case POISON:
                        this.hydra.changePhase(HydraPhase.LIGHTNING);
                        break;
                    case LIGHTNING:
                        this.hydra.changePhase(HydraPhase.FLAME);
                        break;
                    case FLAME:
                        this.hydra.changePhase(HydraPhase.ENRAGED);
                        break;
                    case ENRAGED:
                        this.hydra = null;
                        if (!this.poisonProjectiles.isEmpty()) {
                            this.poisonProjectiles.clear();
                        }
                }

            } else {
                if (animationId == phase.getSpecialAnimationId() && phase.getSpecialAnimationId() != 0) {
                    this.hydra.setNextSpecial();
                }

                if (!this.poisonProjectiles.isEmpty()) {
                    this.poisonProjectiles.values().removeIf((p) -> {
                        return p.getEndCycle() < this.client.getGameCycle();
                    });
                }

            }


        } else {
            if (client.getVarbitValue(Varbits.PRAYER_RIGOUR) + client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MAGIC) +
                    client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MISSILES) > 0) {
                MousePackets.queueClickPacket();
                InteractionHelper.togglePrayer();
                MousePackets.queueClickPacket();
                InteractionHelper.togglePrayer();
            }
        }




        this.attackOverlay.decrementStunTicks();
        this.updateVentTicks();



    }

    private void updateVentTicks() {
        if (this.fountainTicks > 0) {
            --this.fountainTicks;
            if (this.fountainTicks == 0) {
                this.fountainTicks = 8;
            }
        }

        if (!this.vents.isEmpty()) {
            Iterator var1 = this.vents.iterator();
            if (var1.hasNext()) {
                GameObject vent = (GameObject)var1.next();
                int animation = this.getAnimation(vent);
                if (animation == 8279 && this.lastFountainAnim == 8280) {
                    this.fountainTicks = 2;
                }

                this.lastFountainAnim = animation;
            }
        }

    }

    int getAnimation(GameObject gameObject) {
        DynamicObject dynamicObject = (DynamicObject)gameObject.getRenderable();
        return dynamicObject.getAnimation().getId();
    }

    @Subscribe
    private void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();
        if (npc.getId() == 8615) {
            this.hydra = new Hydra(npc);
            if (this.client.isInInstancedRegion() && this.fountainTicks == -1) {
                this.fountainTicks = 11;
            }
        }

    }

    @Subscribe
    private void onAnimationChanged(AnimationChanged event) {
        //log.info("esta {}",event.getActor().getName());
        if (event.getActor().getName() == null) return;
        //log.info("this.hydra: {}",this.hydra);

        if (this.hydra != null && event.getActor().getName().toLowerCase().contains("hydra")) {
            log.info("hydra fase: {}",this.hydra.getPhase());
            log.info("hydra attacks: {}",this.hydra.getPhase().getAttacksPerSwitch());
            HydraPhase phase = this.hydra.getPhase();
            int animationId = event.getActor().getAnimation();
            if (animationId == phase.getDeathAnimation2() && phase != HydraPhase.FLAME || animationId == phase.getDeathAnimation1() && phase == HydraPhase.FLAME) {
                switch (phase) {
                    case POISON:
                        this.hydra.changePhase(HydraPhase.LIGHTNING);
                        break;
                    case LIGHTNING:
                        this.hydra.changePhase(HydraPhase.FLAME);
                        break;
                    case FLAME:
                        this.hydra.changePhase(HydraPhase.ENRAGED);
                        break;
                    case ENRAGED:
                        this.hydra = null;
                        if (!this.poisonProjectiles.isEmpty()) {
                            this.poisonProjectiles.clear();
                        }
                }

            } else {
                if (animationId == phase.getSpecialAnimationId() && phase.getSpecialAnimationId() != 0) {
                    this.hydra.setNextSpecial();
                }

                if (!this.poisonProjectiles.isEmpty()) {
                    this.poisonProjectiles.values().removeIf((p) -> {
                        return p.getEndCycle() < this.client.getGameCycle();
                    });
                }

            }
        }
    }

    @Subscribe
    private void onProjectileMoved(ProjectileMoved event) {
        Projectile projectile = event.getProjectile();
        if (this.hydra != null && this.client.getGameCycle() < projectile.getStartCycle()) {
            int projectileId = projectile.getId();
            if (this.hydra.getPhase().getSpecialProjectileId() == projectileId) {
                if (this.hydra.getAttackCount() >= this.hydra.getNextSpecial()) {
                    this.hydra.setNextSpecial();
                }

                this.poisonProjectiles.put(event.getPosition(), projectile);
            } else if (this.client.getTickCount() != this.lastAttackTick && (projectileId == Hydra.AttackStyle.MAGIC.getProjectileID() || projectileId == Hydra.AttackStyle.RANGED.getProjectileID())) {
                this.hydra.handleProjectile(projectileId);
                this.lastAttackTick = this.client.getTickCount();
            }

        }

    }

    @Subscribe
    private void onChatMessage(ChatMessage event) {
        ChatMessageType chatMessageType = event.getType();
        if (chatMessageType == ChatMessageType.SPAM || chatMessageType == ChatMessageType.GAMEMESSAGE) {
            String message = event.getMessage();
            if (message.equals("The chemicals neutralise the Alchemical Hydra's defences!")) {
                this.clientThread.invokeLater(() -> {
                    this.hydra.setImmunity(false);
                });
            } else if (message.equals("The Alchemical Hydra temporarily stuns you.")) {
                this.attackOverlay.setStunTicks();
            }

        }
    }

    private void addOverlays() {
        this.overlayManager.add(this.sceneOverlay);
        this.overlayManager.add(this.attackOverlay);
        this.overlayManager.add(this.prayerOverlay);
    }

    private void removeOverlays() {
        this.overlayManager.remove(this.sceneOverlay);
        this.overlayManager.remove(this.attackOverlay);
        this.overlayManager.remove(this.prayerOverlay);
    }

    private boolean isInHydraRegion() {
        return this.client.isInInstancedRegion() && Arrays.equals(this.client.getMapRegions(), HYDRA_REGIONS);
    }

    public ClientThread getClientThread() {
        return this.clientThread;
    }

    public Hydra getHydra() {
        return this.hydra;
    }

    public int getFountainTicks() {
        return this.fountainTicks;
    }

    public Map<LocalPoint, Projectile> getPoisonProjectiles() {
        return this.poisonProjectiles;
    }

    public Set<GameObject> getVents() {
        return this.vents;
    }
}
