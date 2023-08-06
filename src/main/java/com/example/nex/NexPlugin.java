package com.example.nex;

import com.example.EthanApiPlugin.Collections.*;
import com.example.InteractionApi.*;
import com.example.PacketUtils.WidgetInfoExtended;
import com.example.Packets.*;
import com.example.PajauApi.PajauApiPlugin;
import com.google.inject.Provides;
import com.sun.jna.IntegerType;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
        name = "<html>[<font color=\"#FA4444\">P</font>] Nex Tulong</html>",
        tags = {"pajau"},
        enabledByDefault = false
)
public class NexPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private KeyManager keyManager;

    @Inject
    private NexConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private NexOverlay nexOverlay;

    @Inject
    private NexOverlayPanel nexOverlayPanel;



    @Provides
    NexConfig getConfig(ConfigManager configManager) {
        return (NexConfig) configManager.getConfig(NexConfig.class);
    }

    private boolean entranding = false;
    private NPC fumus;
    private NPC umbra;
    private NPC bloodReaver;
    private NPC cruor;
    private NPC glacies;
    private boolean encendido = false;
    private int timeout = 0;
    private NPC nex;
    private List<GameObject> estalagmitas = new ArrayList<>();
    private List<Integer> meleeGearIDs = new ArrayList<>();
    private List<Integer> rangedGearIDs = new ArrayList<>();
    private List<Integer> specGearIDs = new ArrayList<>();
    private List<Integer> kcGearIDs = new ArrayList<>();
    private int potTimeout = 0;
    private List<GameObject> weaNegra = new ArrayList<>();
    private int thereIs = 0;
    private int containThis = 0;
    private boolean specing = false;
    private int alert = 0;
    private int cough = 0;
    private boolean healing = false;
    private int bloodSacrifice = 0;
    private int prisionero = 0;
    private List<Widget> remain = new ArrayList<>();
    private List<Widget> items2bank = new ArrayList<>();
    private ListIterator<Widget> items2bankIterator;
    private List<Integer> itemsBuenos = new ArrayList<>();
    private int dropIndex = 0;
    private int darkness = 0;
    private boolean walkUnder = false;
    private boolean mierdaDepositada = false;
    private int runBitch = 0;
    private int aloneTimer = 0;
    private int piernasID = -1;
    private int torsoID = -1;
    private List<Widget> itemsNaqVer;
    private boolean itemChekeados = false;
    private List<Integer> equipFaltante = new ArrayList<>();
    private List<List<Object>> suppFaltante = new ArrayList<>();
    private boolean invChecked4Gear= false;
    private List<Widget> gearInInven = new ArrayList<>();
    private int stalagmiteCount = 0;

    @Getter
    private State estado = State.APAGADO;

    @Getter
    private WorldPoint escapeTile = null;

    @Getter
    private List<WorldPoint> tileBuscados = new ArrayList<>();


    void reset() {
        alert = 0;
        cough = 0;
        healing = false;
        bloodSacrifice = 0;
        specing = false;
        containThis = 0;
        thereIs = 0;
        prisionero = 0;
        tileBuscados = new ArrayList<>();
        escapeTile = null;
        weaNegra = new ArrayList<>();
        potTimeout = 0;
        estalagmitas = new ArrayList<>();
        dropIndex = 0;
        remain = new ArrayList<>();
        darkness = 0;
        walkUnder = false;
        cc = 0;
        nn = 0;
        supp = new ArrayList<>();
        prePot = new ArrayList<>();
        mierdaDepositada = false;
        itemsBuenos = new ArrayList<>();
        entranding = false;
        runBitch = 0;
        aloneTimer = 0;
        piernasID = -1;
        torsoID = -1;
        itemChekeados = false;
        equipFaltante = new ArrayList<>();
        suppFaltante = new ArrayList<>();
        invChecked4Gear= false;
        gearInInven = new ArrayList<>();
        stalagmiteCount = 0;


        fumus = null;
        umbra = null;
        bloodReaver = null;
        cruor = null;
        glacies = null;
        timeout = 0;
        nex = null;
    }


    //-----------------------CONSTANTS--------------------------------------------------
    private final Random N = new Random();

    private static final int CONTAIN_THIS_DURATION = 14;
    private final int PORTAL_CLOSED = 42968;
    private final int PORTAL_OPEN = 42967;
    private final int POT_TIMEOUT = 2;
    private final WorldPoint DUNGEON_TILE = new WorldPoint(2912, 5190, 0);
    private final WorldPoint CENTER_TILE = new WorldPoint(2925, 5203, 0);
    private final int STALAGMITE_ID_PRISON = 42944;

    private final WorldArea areaSalida = new WorldArea(2892, 5199, 8, 9, 0);
    private final WorldArea areaNexP2 = new WorldArea(2917, 5202, 15, 3, 0);

    private final WorldArea areaUmbra = new WorldArea(2927, 5205, 11, 11, 0);


    //todo ver bien los tiles de la cruz
    WorldArea areaEast1 = new WorldArea(2927, 5202, 4, 3, 0);
    WorldArea areaEast2 = new WorldArea(2931, 5202, 4, 3, 0);

    WorldArea areaNorth1 = new WorldArea(2924, 5205, 3, 4, 0);
    WorldArea areaNorth2 = new WorldArea(2924, 5209, 3, 4, 0);

    WorldArea areaWest1 = new WorldArea(2920, 5202, 4, 3, 0);
    WorldArea areaWest2 = new WorldArea(2916, 5202, 4, 3, 0);

    WorldArea areaSouth1 = new WorldArea(2924, 5198, 3, 4, 0);
    WorldArea areaSouth2 = new WorldArea(2924, 5194, 3, 4, 0);
    final WorldPoint[] ptsPrueba = {
            new WorldPoint(2935, 5205, 0),
            new WorldPoint(2923, 5213, 0),
            new WorldPoint(2927, 5213, 0),
    };

    final WorldArea umbraAttackArea = new WorldArea(2927, 5205, 11, 11, 0);
    private final List<Integer> NEX_IDS = List.of(
            NpcID.NEX,
            NpcID.NEX_11282,
            NpcID.NEX_11279,
            NpcID.NEX_11280,
            NpcID.NEX_11281
    );
    final int R = 3;

    final int mcTries = 20;

    private final List<Integer> itemsNoMierda = List.of(
            ItemID.SARADOMIN_BREW4,
            ItemID.SUPER_RESTORE4,
            ItemID.SUPER_COMBAT_POTION4,
            ItemID.RANGING_POTION4,

            ItemID.ANGLERFISH,
            ItemID.SATURATED_HEART,
            ItemID.SUPER_COMBAT_POTION1,
            ItemID.RANGING_POTION1,
            ItemID.ANTIDOTE1_5958,
            ItemID.MENAPHITE_REMEDY1,

            ItemID.BOOK_OF_THE_DEAD,
            ItemID.RUNE_POUCH,
            ItemID.RUNE_POUCH_23650,
            ItemID.DIVINE_RUNE_POUCH,
            ItemID.DIVINE_RUNE_POUCH_L,
            ItemID.DIVINE_RUNE_POUCH_L
    );

    private final WorldArea areaMasterPortal = new WorldArea(2908, 5202, 1, 3, 0);
    private final int FUMUS_ID = 11283;
    private final int UMBRA_ID = 11284;
    private final int CRUOR_ID = 11285;
    private final int GLACIES_ID = 11286;
    private final int REAVER_ID = 0;
    private final WorldArea lobbyArea = new WorldArea(2900, 5199, 9, 9, 0);


    boolean checkGearIds() {
        if (!config.meleeGear().equals("")) {
            meleeGearIDs = Arrays.stream(config.meleeGear().split(",")).map(Integer::parseInt).collect(Collectors.toList());
        }
        if (!config.rangedGear().equals("")) {
            rangedGearIDs = Arrays.stream(config.rangedGear().split(",")).map(Integer::parseInt).collect(Collectors.toList());
        }
        if (!config.specGear().equals("")) {
            specGearIDs = Arrays.stream(config.specGear().split(",")).map(Integer::parseInt).collect(Collectors.toList());
        }

        if (meleeGearIDs.size() == 0) {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Melee gear not found", Color.red), "");
        }
        if (rangedGearIDs.size() == 0) {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Ranged gear not found", Color.red), "");
        }
        if (specGearIDs.size() == 0) {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Spec gear not found", Color.red), "");
        }

        if (meleeGearIDs.size() == 0 || rangedGearIDs.size() == 0 || specGearIDs.size() == 0) {
            encendido = false;
            estado = State.STARTING;
            return false;
        } else {
            return true;
        }
    }


    @SneakyThrows
    @Subscribe
    void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("nexAnal")) {
            if (event.getKey().equals("onOff")) {
                encendido = !encendido;
                clientThread.invoke(() -> {
                    if (!encendido) {
                        estado = State.APAGADO;
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Apagado", Color.red), "");
                    } else {
                        reset();
                        if (checkGearIds()) {
                            if (config.mode() == NexConfig.modo.KC) {
                                estado = State.GETTING_KC;
                            } else if (config.mode() == NexConfig.modo.NEX) {
                                estado = State.RECOVERY;
                            }
                            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Prendido", Color.green), "");
                        } else {
                            estado = State.APAGADO;
                            encendido = false;
                            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Couldn't turn on", Color.green), "");
                        }

                    }
                });
            } else if (event.getKey().equals("fetchGearRanged")) {
                List<EquipmentItemWidget> rangedGear = Equipment.search().result();
                rangedGearIDs = rangedGear.stream().map(EquipmentItemWidget::getEquipmentItemId).collect(Collectors.toList());
                StringBuilder gearIDsString = new StringBuilder();
                for (Integer id : rangedGearIDs) {
                    gearIDsString.append(id).append(",");
                }
                gearIDsString.deleteCharAt(gearIDsString.length() - 1);
                configManager.setConfiguration("nexAnal", "rangedGear", gearIDsString.toString());

                clientThread.invokeLater(() -> {
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", rangedGear.size() + " anal", "");
                    configManager.sendConfig();
                    configManager.load();
                });
            } else if (event.getKey().equals("fetchGearMelee")) {
                List<EquipmentItemWidget> meleeGear = Equipment.search().result();
                meleeGearIDs = meleeGear.stream().map(EquipmentItemWidget::getEquipmentItemId).collect(Collectors.toList());
                StringBuilder gearIDsString = new StringBuilder();
                for (Integer id : meleeGearIDs) {
                    gearIDsString.append(id).append(",");
                }
                if (gearIDsString.length() != 0) {
                    gearIDsString.deleteCharAt(gearIDsString.length() - 1);
                }
                configManager.setConfiguration("nexAnal", "meleeGear", gearIDsString.toString());

                clientThread.invokeLater(() -> {
                    configManager.sendConfig();
                    configManager.load();
                });
            } else if (event.getKey().equals("fetchSpecGear")) {
                List<EquipmentItemWidget> specGear = Equipment.search().result();
                specGearIDs = specGear.stream().map(EquipmentItemWidget::getEquipmentItemId).collect(Collectors.toList());
                StringBuilder gearIDsString = new StringBuilder();
                for (Integer id : specGearIDs) {
                    gearIDsString.append(id).append(",");
                }
                gearIDsString.deleteCharAt(gearIDsString.length() - 1);
                configManager.setConfiguration("nexAnal", "specGear", gearIDsString.toString());

                clientThread.invokeLater(() -> {
                    configManager.sendConfig();
                    configManager.load();
                });
            } else if (event.getKey().equals("fetchKcGear")) {
                List<EquipmentItemWidget> kcGear = Equipment.search().result();
                kcGearIDs = kcGear.stream().map(EquipmentItemWidget::getEquipmentItemId).collect(Collectors.toList());
                StringBuilder gearIDsString = new StringBuilder();
                for (Integer id : kcGearIDs) {
                    gearIDsString.append(id).append(",");
                }
                gearIDsString.deleteCharAt(gearIDsString.length() - 1);
                configManager.setConfiguration("nexAnal", "kcGear", gearIDsString.toString());

                clientThread.invokeLater(() -> {
                    configManager.sendConfig();
                    configManager.load();
                });
            } else if (event.getKey().equals("fetchWepMelee")) {
                clientThread.invoke(() -> client.runScript(545, 25362450, 3, 1, 1, 2));
                Widget wepSlot = client.getWidget(25362450);
                if (wepSlot == null) {
                    return;
                }
                if (wepSlot.getChild(1) == null) {
                    return;
                }
                int wepId = wepSlot.getChild(1).getItemId();

                configManager.setConfiguration("nexAnal", "meleeWepID", wepId);

                clientThread.invokeLater(() -> {
                    configManager.sendConfig();
                    configManager.load();
                });
            }


        }
    }


    @Override
    protected void startUp() throws Exception {
        this.overlayManager.add(nexOverlay);
        this.overlayManager.add(nexOverlayPanel);
        estado = State.APAGADO;
        encendido = false;
        if (!config.meleeGear().equals("")) {
            meleeGearIDs = Arrays.stream(config.meleeGear().split(",")).map(Integer::parseInt).collect(Collectors.toList());
        }
        if (!config.rangedGear().equals("")) {
            rangedGearIDs = Arrays.stream(config.rangedGear().split(",")).map(Integer::parseInt).collect(Collectors.toList());
        }
        if (!config.specGear().equals("")) {
            specGearIDs = Arrays.stream(config.specGear().split(",")).map(Integer::parseInt).collect(Collectors.toList());
        }
        if (!config.kcGear().equals("")) {
            kcGearIDs = Arrays.stream(config.kcGear().split(",")).map(Integer::parseInt).collect(Collectors.toList());
        }


    }

    @Override
    protected void shutDown() throws Exception {
        this.overlayManager.remove(nexOverlay);
        this.overlayManager.remove(nexOverlayPanel);
        estado = State.APAGADO;
        encendido = false;
        reset();
    }


    private boolean lowHP(int hpTrigger) {
        return client.getBoostedSkillLevel(Skill.HITPOINTS) <= hpTrigger;
    }

    public boolean isIdle(Player gamer) {
        return gamer.getIdlePoseAnimation() == gamer.getPoseAnimation();
    }

    public boolean isIdle(NPC mono) {
        return mono.getIdlePoseAnimation() == mono.getPoseAnimation();
    }


    void checkMeleeGear() {
        List<Widget> gearInInventory = Inventory.search().idInList(meleeGearIDs).result();
        //log.info("size gear: {}",gearInInventory.size());
        if (gearInInventory.size() > 0) {
            log.info("equipando");
            for (Widget item : gearInInventory) {
                WidgetPackets.queueWidgetActionPacket(3, item.getId(), item.getItemId(), item.getIndex());
            }
        }
    }

    void checkRangedGear() {
        List<Widget> gearInInventory = Inventory.search().idInList(rangedGearIDs).result();
        //log.info("size gear: {}",gearInInventory.size());
        if (gearInInventory.size() > 0) {
            log.info("equipando");
            for (Widget item : gearInInventory) {
                WidgetPackets.queueWidgetActionPacket(3, item.getId(), item.getItemId(), item.getIndex());
            }
        }
    }

    void checkGear(String stilo,List<Integer> exceptions) {
        List<Integer> IDs = new ArrayList<>();
        if (stilo.equals("ranged")) {
            IDs.addAll(rangedGearIDs);
        } else if (stilo.equals("melee")) {
            IDs.addAll(meleeGearIDs);
        } else if (stilo.equals("spec")) {
            IDs.addAll(specGearIDs);
        } else if (stilo.equals("wepMelee")) {
            IDs.add(config.meleeWepID());
        } else {
            log.info("gear no identificada");
            return;
        }

        if (exceptions.size() > 0) {
            for (int ex : exceptions) {
                if (ex != -1) {
                    IDs.removeIf(x -> x == ex);
                }
            }
        }

        List<Widget> gearInInventory = Inventory.search().idInList(IDs).result();
        if (gearInInventory.size() > 0) {
            log.info("equipando " + stilo);
            for (Widget item : gearInInventory) {
                WidgetPackets.queueWidgetActionPacket(3, item.getId(), item.getItemId(), item.getIndex());
            }
        }
    }

    void checkGear(String stilo) {
        checkGear(stilo, new ArrayList<>());
    }

    void checkPrayer(Prayer prayer) {
        if (client.getVarbitValue(prayer.getVarbit()) != 1) {
            log.info("Prendiendo {}", prayer.name());
            InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.valueOf("PRAYER_" + prayer.name()).getPackedId());
        }
    }

    void checkBoost(Skill skill) {
        if (potTimeout > 0) {
            return;
        }
        Optional<Widget> pot = Optional.empty();
        if (skill == Skill.RANGED) {
            pot = Inventory.search().matchesWildCardNoCase("Ranging potion*").first();
        } else if (skill == Skill.STRENGTH) {
            pot = Inventory.search().matchesWildCardNoCase("Super combat*").first();
        }
        if (client.getBoostedSkillLevel(skill) >= client.getRealSkillLevel(skill) - 2
                && client.getBoostedSkillLevel(skill) < config.lvlReboost()) {
            if (pot.isPresent()) {
                InventoryInteraction.useItem(pot.get(), "Drink");
                potTimeout = 2;
            } else {
                log.info("No se encontro boost potion");
            }
        }
    }

    CAs getCA() {
        for (int i = 0; i < CAs.values().length; i++) {
            if (client.getVarbitValue(CAs.values()[i].getVarbit()) == 0) {
                return CAs.values()[i - 1];
            }
        }
        return CAs.GRAND_MASTER;
    }

    private List<List<Object>> supp = new ArrayList<>();
    private List<List<Object>> prePot = new ArrayList<>();
    private List<Optional<Widget>> potting = new ArrayList<>();
    private int cc = 0;
    private int nn = 0;


    @Subscribe
    void onGameTick(GameTick event) {
        //if(!encendido) return;
        if (timeout > 0) {
            timeout--;
            return;
        }
        if (cough > 0) {
            cough--;
        }
        if (bloodSacrifice > 0) {
            bloodSacrifice--;
        }
        if (potTimeout > 0) {
            potTimeout--;
        }

        alert++;
        if (alert > 5) {
            alert = 0;
            log.info("estado = {}", estado);
        }

        Player player = client.getLocalPlayer();
        LocalPoint playerTrueLocal = LocalPoint.fromWorld(client, player.getWorldLocation());
        if (playerTrueLocal == null) {
            return;
        }
        WorldPoint playerFromLocal = WorldPoint.fromLocalInstance(client, playerTrueLocal);

        if (estado.isEnPelea()) {
            if (nex == null && estado != State.FASE_5 && estado != State.GETTING_KC) {
                Optional<NPC> npc = NPCs.search().withId(11278).first();
                if (npc.isPresent()) {
                    nex = npc.get();
                } else {
                    log.info("nex not found");
                    return;
                }
            }


            if (healing) {
                if (estado == State.FASE_2) {
                    if (client.getBoostedSkillLevel(Skill.HITPOINTS) >= client.getRealSkillLevel(Skill.HITPOINTS)) {
                        healing = false;
                    }
                } else if (client.getBoostedSkillLevel(Skill.HITPOINTS) >= config.hpStopHealing()) {
                    healing = false;
                }
            }

            if (potTimeout == 0) {
                if (lowHP(config.hpTriggerHeal()) || healing) {
                    healing = true;
                    Optional<Widget> foodUsed = Inventory.search().matchesWildCardNoCase(config.foodName()).first();
                    if (foodUsed.isPresent()) {
                        potTimeout = 2;
                        if (foodUsed.get().getName().contains("(")) {
                            InventoryInteraction.useItem(foodUsed.get(), "Drink");
                        } else {
                            InventoryInteraction.useItem(foodUsed.get(), "Eat");
                        }
                    }
                } else if (client.getBoostedSkillLevel(Skill.PRAYER) < config.prayThreshold()) {
                    Optional<Widget> prayPot = Inventory.search().matchesWildCardNoCase(config.prayRestorationName()).first();
                    if (prayPot.isPresent()) {
                        potTimeout = 2;
                        InventoryInteraction.useItem(prayPot.get(), "Drink");
                    }
                } else if (client.getBoostedSkillLevel(Skill.STRENGTH) < client.getRealSkillLevel(Skill.STRENGTH) - 2) {
                    Optional<Widget> restore = Inventory.search().matchesWildCardNoCase("Super restore*").first();
                    if (restore.isPresent()) {
                        potTimeout = 2;
                        InventoryInteraction.useItem(restore.get(), "Drink");
                    }
                }
            }

        }


        if (estado == State.STARTING) {
            if (client.getVarpValue(172) == 0) {
                WidgetPackets.queueWidgetActionPacket(1, WidgetInfo.COMBAT_AUTO_RETALIATE.getPackedId(), -1, -1);
                log.info("Retaliate puesto en off");
                return;
            }
            if (player.getWorldLocation().isInArea(lobbyArea)) {
                estado = State.BANKING;
            }

            if (!encendido) return;
            estado = State.BANKING;

        } else if (estado == State.BANKING) {
            if (!config.shouldBank()) {
                estado = State.ENTRAR;
                return;
            }

            /*if (!itemChekeados) {
                itemChekeados = true;
                equipFaltante = checkEquipment();
                log.info("eq faltante: {}", equipFaltante);

                suppFaltante = checkInv();
                if (suppFaltante.size() == 0 && equipFaltante.size() == 0 && itemsNaqVer.size() == 0) {
                    estado = State.ENTRAR;
                    return;
                }
            }

            if (!Bank.isOpen()) {
                if (isIdle(player)) {
                    Optional<NPC> reis = NPCs.search().withId(NpcID.ASHUELOT_REIS_11289).first();
                    if (reis.isPresent()) {
                        NPCInteraction.interact(reis.get(), "Bank");
                    } else {
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Bank not found", Color.red), "");
                    }
                }
            }

            //Depositando------------------------
            if (itemsNaqVer.size() > 0) {

                log.info("woof");
                int meow=0;
                for (List<Object> c : suppFaltante) {
                    if((int)c.get(1) < 0) meow++;
                }
                if (itemsNaqVer.size() + meow > 5) {
                    //deposit all
                    itemChekeados = false;
                } else {
                    if (cc < itemsNaqVer.size()) {
                        int m = Math.min(1 + N.nextInt(3), itemsNaqVer.size() - cc);
                        for (int i = cc; i < m + cc; i++) {
                            BankInventoryInteraction.useItem(itemsNaqVer.get(i), "Deposit-All");
                        }
                        cc += m;
                        if (cc == itemsNaqVer.size()) {
                            itemChekeados = false;
                            cc=0;
                        }
                        return;
                    }

                }
            }


            List<Integer> suppliesSobrepasados = suppFaltante.stream().filter(x -> (int) x.get(1) < 0).map(y->(int)y.get(0)).collect(Collectors.toList());
            if (suppliesSobrepasados.size() > 0) {
                log.info("meow");
                if (cc < suppliesSobrepasados.size()) {
                    int m = Math.min(1 + N.nextInt(3), suppliesSobrepasados.size() - cc);
                    for (int i = cc; i < m + cc; i++) {
                        BankInventoryInteraction.useItem(suppliesSobrepasados.get(i), "Deposit-All");
                    }
                    cc += m;
                    if (cc == suppliesSobrepasados.size()) {
                        itemChekeados = false;
                        cc=0;
                    }
                    return;
                }

            }


            //Sacando--------------------------
            if (equipFaltante.size() > 0) {
                log.info("equip Faltante:{}",equipFaltante.size());
                if (cc < equipFaltante.size()) {
                    int m = Math.min(1 + N.nextInt(3), equipFaltante.size() - cc);
                    for (int i = cc; i < m + cc; i++) {
                        Optional<Widget> widgetDelEquipFaltante = Bank.search().withId(equipFaltante.get(i)).first();
                        if (widgetDelEquipFaltante.isPresent()) {
                            log.info("Sacando: {}",widgetDelEquipFaltante.get().getName());
                            if (widgetDelEquipFaltante.get().getName().contains("arrow") || widgetDelEquipFaltante.get().getName().contains("bolt")) {
                                BankInteraction.useItem(widgetDelEquipFaltante.get(), "Withdraw-All");
                            } else {
                                BankInteraction.useItem(widgetDelEquipFaltante.get(), "Withdraw-1");
                            }

                        }

                    }
                    cc += m;
                    if (cc >= equipFaltante.size()) {
                        itemChekeados = false;
                        cc = 0;
                    }
                    return;
                } else {
                    itemChekeados = false;
                    cc=0;
                    return;
                }
            }

            if (!invChecked4Gear) {
                invChecked4Gear = true;
                gearInInven = BankInventory.search().idInList(meleeGearIDs).result();
            }


            if (gearInInven.size() > 0) {
                if (cc < gearInInven.size()) {
                    int m = Math.min(1 + N.nextInt(3), gearInInven.size() - cc);
                    for (int i = cc; i < cc + m; i++) {
                        MousePackets.queueClickPacket();
                        WidgetPackets.queueWidgetActionPacket(9,gearInInven.get(i).getId(),gearInInven.get(i).getItemId(),gearInInven.get(i).getIndex());
                    }
                    cc += m;
                    if (cc == gearInInven.size()) {
                        invChecked4Gear = false;
                        cc=0;
                    }
                    log.info("Se sacaron {} items",m);
                    return;
                }
            }


            if (suppFaltante.size() > 0) {
                if (cc < suppFaltante.size()) {
                    Optional<Widget> item = Bank.search().withId((Integer) suppFaltante.get(cc).get(0)).first();
                    if (item.isPresent()) {
                        if ((int) suppFaltante.get(cc).get(1) == 1) {
                            BankInteraction.useItem(item.get(), "Withdraw-1");
                        } else {
                            BankInteraction.withdrawX(item.get(), (Integer) suppFaltante.get(cc).get(1));
                            timeout = 1;
                        }
                    } else {
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Falta " + suppFaltante.get(cc).get(2), Color.red), "");
                    }
                    cc++;

                    if (cc == suppFaltante.size()) {
                        cc=0;
                        itemChekeados = false;
                    }
                }

            }*/






            /*if (checkInv().size() == 0 ) {
                estado = State.ENTRAR;
                return;
            }*/




            if (Bank.isOpen()) {
                if (!mierdaDepositada) {
                    if (itemsBuenos.isEmpty()) {
                        itemsBuenos.addAll(rangedGearIDs);
                        itemsBuenos.addAll(meleeGearIDs);
                        itemsBuenos.addAll(specGearIDs);
                        itemsBuenos.add(config.meleeWepID());
                        itemsBuenos.addAll(List.of(
                                ItemID.BOOK_OF_THE_DEAD,
                                ItemID.RUNE_POUCH,
                                ItemID.DIVINE_RUNE_POUCH,
                                ItemID.RUNE_POUCH_L,
                                ItemID.DIVINE_RUNE_POUCH_L));


                    }
                    if (!BankInventory.search().filter(x -> !itemsBuenos.contains(x.getItemId())).empty()) {
                        if (config.bankAnal()) {
                            items2bank = BankInventory.search().filter(x -> rangedGearIDs.stream().noneMatch(y -> x.getItemId() == y)).filterUnique().result();
                            items2bankIterator = items2bank.listIterator();
                            cc = 0;
                            while (items2bankIterator.hasNext() && cc < 6) {
                                cc++;
                                Widget next = items2bankIterator.next();
                                BankInventoryInteraction.useItem(next, "Deposit-All");
                            }
                        } else {
                            items2bank = BankInventory.search().filter(x -> rangedGearIDs.stream().noneMatch(y -> x.getItemId() == y)).result();
                            if (items2bank.size() > 0) {
                                BankInventoryInteraction.useItem(items2bank.get(0), "Deposit-All");
                                timeout = N.nextInt(2);
                                log.info("Depositando {}", items2bank.get(0));

                            }
                        }
                        return;
                    }
                    mierdaDepositada = true;
                }


                if (prePot.size() > 0 || supp.size() > 0) {
                    if (cc < supp.size() - 1) { //sacando los suplies
                        if (((Optional<?>) supp.get(cc).get(0)).isPresent()) {
                            Widget item = (Widget) ((Optional<?>) supp.get(cc).get(0)).get();
                            int amount = (Integer) supp.get(cc).get(1);
                            int listas = BankInventory.search().withId(item.getItemId()).result().size();
                            if (amount - listas > 0) {
                                BankInteraction.withdrawX(item, amount - listas);
                                log.info("sacando {}", supp.get(cc).get(2));
                                cc++;
                            } else if (amount - listas < 0) {
                                BankInventoryInteraction.useItem(item, "Deposit-All");
                                log.info("ya hay {}", item.getName());
                            }
                            timeout = 1;
                            return;
                        } else {
                            log.info("No " + supp.get(cc).get(2));
                        }
                    }
                    client.runScript(138);

                    if (config.shouldPrepot()) {
                        if (nn < prePot.size()) {   //withdrawing Pre-pot
                            if (((Optional<?>) prePot.get(nn).get(0)).isPresent()) {
                                //BankInteraction.withdrawX((Widget) ((Optional<?>) prePot.get(nn).get(0)).get(), (Integer) prePot.get(nn).get(1));
                                BankInteraction.useItem((Widget) ((Optional<?>) prePot.get(nn).get(0)).get(), "Withdraw-1");
                                log.info("sacando {}", prePot.get(nn).get(2));
                                nn++;
                                timeout = 2;
                                return;
                            } else {
                                log.info("No " + prePot.get(nn).get(2));
                            }
                        }


                        log.info("Cargando las dosis");
                        if (config.heart()) {
                            potting.add(BankInventory.search().withId(ItemID.SATURATED_HEART).first());
                            log.info("cargando el corazon");
                        }
                        if (config.antiPre()) {
                            potting.add(BankInventory.search().withId(ItemID.ANTIDOTE1_5958).first());
                        }
                        if (config.combatPre()) {
                            potting.add(BankInventory.search().withId(ItemID.SUPER_COMBAT_POTION1).first());
                        }
                        if (config.rangPre()) {
                            potting.add(BankInventory.search().withId(ItemID.RANGING_POTION1).first());
                        }
                        if (config.menaphitePre()) {
                            potting.add(BankInventory.search().withId(ItemID.MENAPHITE_REMEDY1).first());
                        }
                        if (config.anglerPre()) {
                            potting.add(BankInventory.search().withId(ItemID.ANGLERFISH).first());
                        }
                    }

                    nn = 0;
                    cc = 0;
                    client.runScript(138);
                    mierdaDepositada = false;
                    estado = State.ENTRAR;

                } else { //cargando los supplies necesarios
                    supp.add(List.of(Bank.search().withId(ItemID.SUPER_RESTORE4).first(), config.restoreAmount(), "Super restore(4)"));
                    supp.add(List.of(Bank.search().withId(ItemID.SUPER_COMBAT_POTION4).first(), config.combatAmount(), "Super Combat(4)"));
                    supp.add(List.of(Bank.search().withId(ItemID.RANGING_POTION4).first(), config.rangAmount(), "Ranging potion(4)"));
                    supp.add(List.of(Bank.search().withId(ItemID.SARADOMIN_BREW4).first(), config.saraAmount(), "Saradomin brew(4)"));
                    if (config.thralls()) {
                        supp.add(List.of(Bank.search().withId(ItemID.BOOK_OF_THE_DEAD).first(), 1,"Book of the death"));
                        supp.add(List.of(Bank.search().withId(ItemID.DIVINE_RUNE_POUCH).first(), config.saraAmount(), "Saradomin brew(4)"));
                    }

                    if (config.shouldPrepot()) {
                        if (config.heart()) {
                            prePot.add(List.of(Bank.search().withId(ItemID.SATURATED_HEART).first(), 1, "Heart"));
                        }
                        if (config.antiPre()) {
                            prePot.add(List.of(Bank.search().withId(ItemID.ANTIDOTE1_5958).first(), 1, "Antidote 1"));
                        }
                        if (config.combatPre()) {
                            prePot.add(List.of(Bank.search().withId(ItemID.SUPER_COMBAT_POTION1).first(), 1, "Combat 1"));
                        }
                        if (config.rangPre()) {
                            prePot.add(List.of(Bank.search().withId(ItemID.RANGING_POTION1).first(), 1, "Ranging 1"));
                        }
                        if (config.menaphitePre()) {
                            prePot.add(List.of(Bank.search().withId(ItemID.MENAPHITE_REMEDY1).first(), 1, "Menaphite remedy 1"));
                        }
                        if (config.anglerPre()) {
                            prePot.add(List.of(Bank.search().withId(ItemID.ANGLERFISH).first(), 1, "Anglerfish"));
                        }
                    }

                }

            } else {
                if (isIdle(player)) {
                    Optional<NPC> reis = NPCs.search().withId(NpcID.ASHUELOT_REIS_11289).first();
                    if (reis.isPresent()) {
                        NPCInteraction.interact(reis.get(), "Bank");
                    } else {
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Bank not found", Color.red), "");
                    }
                }
            }


        } else if (estado == State.ENTRAR) {
            if (client.getVarbitValue(172) == 1) {
                WidgetPackets.queueWidgetActionPacket(1, WidgetInfo.COMBAT_AUTO_RETALIATE.getPackedId(), -1, -1);
                log.info("Retaliate puesto en off");
                return;
            }
            if (player.getWorldLocation().isInArea(lobbyArea)) {
                if (config.waitForMaster()) {
                    Optional<Player> master = Players.search().filter(x -> Text.removeTags(x.getName()).equalsIgnoreCase(config.masterName())).first();
                    if (master.isPresent() && master.get().getWorldLocation().isInArea(areaMasterPortal)) {
                        entranding = true;
                    }
                } else {
                    entranding = true;
                }
            } else {
                if (isIdle(player)) {
                    if (playerFromLocal.equals(config.playerKey().getP1Tile())) {
                        estado = State.FASE_1;
                    } else {
                        moverHacia(client, config.playerKey().getP1Tile());
                    }
                }
            }

            if (entranding) {
                if (config.shouldPrepot() && potting.size() > 0) {
                    if (Bank.isOpen()) {   //bank must be open for prepot

                        if (nn < potting.size()) {  //drinking prepots
                            if (potting.get(nn).isPresent()) {
                                WidgetPackets.queueWidgetActionPacket(9, potting.get(nn).get().getId(), potting.get(nn).get().getItemId(), potting.get(nn).get().getIndex());
                                log.info("Usando {}", potting.get(nn).get().getName());
                                nn++;
                                if (nn == potting.size()) {
                                    timeout = N.nextInt(2);
                                } else {
                                    timeout = 2;
                                }
                                return;
                            } else {
                                log.info("No se encontro una wea");
                            }
                        }
                        Optional<Widget> corazon = BankInventory.search().idInList(List.of(ItemID.SATURATED_HEART, ItemID.IMBUED_HEART)).first();
                        if (corazon.isPresent()) {
                            BankInventoryInteraction.useItem(corazon.get(), "Deposit-All");
                            return;
                        }

                        if (!Inventory.full()) {
                            Optional<Widget> saras = Bank.search().withId(ItemID.SARADOMIN_BREW4).first();
                            if (saras.isPresent()) {
                                BankInteraction.useItem(saras.get(), "Withdraw-All");
                            } else {
                                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Brews not found", Color.red), "");
                                estado = State.APAGADO;
                            }
                            return;
                        }

                        if (config.wep2h()) {
                            Optional<Widget> sara = BankInventory.search().withId(ItemID.SARADOMIN_BREW4).first();
                            sara.ifPresent(x -> BankInventoryInteraction.useItem(x, "Deposit-1"));
                        }

                        potting = new ArrayList<>();
                        prePot = new ArrayList<>();
                        supp = new ArrayList<>();

                        client.runScript(29);
                        //client.runScript(138);

                    } else {
                        if (isIdle(player)) {
                            Optional<NPC> reis = NPCs.search().withId(NpcID.ASHUELOT_REIS_11289).first();
                            if (reis.isPresent()) {
                                NPCInteraction.interact(reis.get(), "Bank");
                                timeout = 1;
                            } else {
                                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Bank not found", Color.red), "");
                            }
                        }
                    }
                } else {
                    Optional<TileObject> portal = TileObjects.search().withId(PORTAL_OPEN).first();
                    if (portal.isPresent()) {
                        TileObjectInteraction.interact(portal.get(), "Pass (normal)");
                        timeout = 5;
                    }
                }


            }


        } else if (estado == State.FASE_1) {//-------------------------------------------------------------------------
            //Special----------------------------------------
            if (specing) {
                if (client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) < config.specPercent() * 10) {
                    specing = false;
                    log.info("speecing disminuido con gametick");
                } else if (Equipment.search().withId(config.specID()).first().isPresent() && client.getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0) {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetActionPacket(1, 10485795, -1, -1);
                }
            }
            //Pray Gear----------------------------------------
            checkPrayer(Prayer.PROTECT_FROM_MAGIC);
            if (specing) {
                checkPrayer(config.specStyle().getPrayer());
                checkGear("spec");
            } else {
                checkGear("melee");
                checkPrayer(Prayer.PIETY);
            }

            if (cough == 0) {
                checkBoost(Skill.STRENGTH);
            }

            //Atacke y esquive---------------------------------

            if (thereIs == 6) {
                moverHacia(client, config.playerKey().getP1Tile());
            } else if (thereIs == 4) {
                int angulo = nex.getCurrentOrientation();
                Angle ang = new Angle(nex.getCurrentOrientation());
                log.info("angulo:{}", ang);

                if (ang.getNearestDirection() == Direction.EAST) {
                    if (playerFromLocal.isInArea(areaEast1)) {
                        moverHacia(client, new WorldPoint(2925, 5205, 0));
                    } else if (player.getWorldLocation().isInArea(areaEast2)) {
                        moverHacia(client, new WorldPoint(2935, 5203, 0));
                    }
                } else if (ang.getNearestDirection() == Direction.NORTH) {
                    if (playerFromLocal.isInArea(areaNorth1)) {
                        moverHacia(client, new WorldPoint(2923, 5203, 0));
                    } else if (playerFromLocal.isInArea(areaNorth2)) {
                        moverHacia(client, new WorldPoint(2925, 5213, 0));
                    }
                } else if (ang.getNearestDirection() == Direction.SOUTH) {
                    if (playerFromLocal.isInArea(areaSouth1)) {
                        moverHacia(client, new WorldPoint(2923, 5203, 0));
                    } else if (playerFromLocal.isInArea(areaSouth2)) {
                        moverHacia(client, new WorldPoint(2925, 5213, 0));
                    }
                } else if (ang.getNearestDirection() == Direction.WEST) {
                    if (playerFromLocal.isInArea(areaWest1)) {
                        moverHacia(client, new WorldPoint(2925, 5205, 0));
                    } else if (playerFromLocal.isInArea(areaWest2)) {
                        moverHacia(client, new WorldPoint(2914, 5205, 0));
                    }
                }
            }else if (player.getInteracting() == null) {
                NPCInteraction.interact(nex, "Attack");
                log.info("atackando a nex");
            }
            if (thereIs > 0) {
                thereIs++;
            }

            if (!healing
                    && client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) >= config.specPercent() * 10
                    && client.getBoostedSkillLevel(config.specStyle().getSkill()) >= client.getRealSkillLevel(config.specStyle().getSkill())) {
                specing = true;
            }

        } else if (estado == State.FUMUS) {//--------------------------------------------------------------
            if (cough == 0) {
                checkBoost(Skill.RANGED);
            }
            checkGear("ranged");
            checkPrayer(Prayer.PROTECT_FROM_MAGIC);
            checkPrayer(Prayer.RIGOUR);

            if (fumus == null || fumus.isDead()) {
                Optional<NPC> npc = NPCs.search().withId(FUMUS_ID).first();
                if (npc.isPresent()) {
                    fumus = npc.get();
                } else {
                    estado = State.FASE_2;
                    specing = false;
                }
            } else if (isIdle(player)) {
                if (WorldPoint.fromLocalInstance(client, player.getLocalLocation()).equals(config.playerKey().getFTile())) {
                    Actor enemy = player.getInteracting();
                    if (enemy == null || enemy.getName() == null || !enemy.getName().equals(fumus.getName())) {
                        log.info("atakando a fumus");
                        NPCInteraction.interact(fumus, "Attack");
                    }
                } else {
                    moverHacia(client, config.playerKey().getFTile());
                }
            }
        } else if (estado == State.FASE_2) {
            //Special------------------------------------------------------------
            if (specing && config.specP2()) {
                if (client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) < config.specPercent() * 10) {
                    specing = false;
                    log.info("speecing disminuido con gametick");
                }
                if (Equipment.search().withId(config.specID()).first().isPresent() && client.getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0) {
                    log.info("activando spec");
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetActionPacket(1, 10485795, -1, -1);
                }
            }

            //Prayer y Gear---------------------------------------------------------
            if (specing && config.specP2()) {
                checkPrayer(config.specStyle().getPrayer());
                checkGear("spec");
            } else {
                checkPrayer(Prayer.RIGOUR);
                checkGear("ranged");

            }
            checkPrayer(Prayer.PROTECT_FROM_MISSILES);
            checkBoost(Skill.RANGED);

            WorldPoint nexLoc = nex.getWorldLocation().dx(1).dy(1);

            //Atacke y esquive---------------------------------------------------------
            if (nex.getPoseAnimation() == 9175 || walkUnder) {
                if (nex.getInteracting() != null && nex.getInteracting().getName() != null && !nex.getInteracting().getName().equalsIgnoreCase(player.getName())) {
                    if (nex.getInteracting().getWorldLocation().distanceTo(nexLoc) > 4) {
                        runBitch++;
                        log.info("runBitch: {}", runBitch);
                        if (runBitch >= 2) {
                            walkUnder = false;
                            runBitch = 0;
                            return;
                        }
                    }
                }

                if (nexLoc.distanceTo(player.getWorldLocation()) <= 4) {
                    if (darkness > 0) {
                        healing = true;
                    }
                    walkUnder = true;
                    MousePackets.queueClickPacket();
                    MovementPackets.queueMovement(nexLoc);
                    log.info("moviendo debajo de nex con healing: {}",healing);
                    return;
                } else {
                    if (walkUnder) {
                        runBitch = 0;
                        walkUnder = false;
                        return;
                    }
                }
            }


            if (weaNegra.size() > 0) {
                log.info("wea negra!");
                if (weaNegra.stream().anyMatch(x -> x.getWorldLocation().equals(player.getWorldLocation())) && isIdle(player)) {
                    log.info("debajo mio");
                    WorldPoint wpGamer = player.getWorldLocation();
                    if (client.getCollisionMaps() == null) {
                        return;
                    }
                    CollisionData col = client.getCollisionMaps()[client.getPlane()];
                    MousePackets.queueClickPacket();
                    if (isWalkable(col, wpGamer.dx(1).dy(0)) && weaNegra.stream().noneMatch(x -> x.getWorldLocation().equals(wpGamer.dx(1).dy(0)))) {
                        MovementPackets.queueMovement(wpGamer.dx(1).dy(0));
                    } else if (isWalkable(col, wpGamer.dx(0).dy(1)) && weaNegra.stream().noneMatch(x -> x.getWorldLocation().equals(wpGamer.dx(0).dy(1)))) {
                        MovementPackets.queueMovement(wpGamer.dx(0).dy(1));
                    } else if (isWalkable(col, wpGamer.dx(-1).dy(0)) && weaNegra.stream().noneMatch(x -> x.getWorldLocation().equals(wpGamer.dx(-1).dy(0)))) {
                        MovementPackets.queueMovement(wpGamer.dx(-1).dy(0));
                    } else if (isWalkable(col, wpGamer.dx(0).dy(-1)) && weaNegra.stream().noneMatch(x -> x.getWorldLocation().equals(wpGamer.dx(0).dy(-1)))) {
                        MovementPackets.queueMovement(wpGamer.dx(0).dy(-1));
                    }
                }
            } else if (isIdle(nex)) {
                LocalPoint nexTrueLocal = LocalPoint.fromWorld(client, nex.getWorldLocation());
                if (nexTrueLocal == null) return;
                WorldPoint nexFromLocal = WorldPoint.fromLocalInstance(client, nexTrueLocal);
                WorldPoint centerPoint = new ArrayList<>(WorldPoint.toLocalInstance(client, CENTER_TILE)).get(0);
                WorldPoint tilePreferido = new WorldPoint(centerPoint.getX(), nexLoc.getY() + 1 + config.rangeDistant().getDistant(), 0);

                if (nexFromLocal.isInArea(areaNexP2) && !player.getWorldLocation().equals(tilePreferido)) {
                    if (isIdle(player)) {
                        MousePackets.queueClickPacket();
                        MovementPackets.queueMovement(tilePreferido);
                    }
                } else if (player.getWorldLocation().distanceTo(nexLoc) < config.rangeDistant().getDistant()) {
                    if (isIdle(player)) {
                        escapeTile = null;
                        escapeTile = metodo3(config.rangeDistant().getDistant());
                        if (escapeTile != null) {
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(escapeTile);
                            log.info("moviendo al escapeTile {}", WorldPoint.fromLocalInstance(client, LocalPoint.fromWorld(client, escapeTile)));
                            timeout = 1;
                        } else {
                            log.info("no se encontro un puto tile");
                        }
                    }
                } else if (!player.isInteracting()) {
                    log.info("Atackando a la perra");
                    NPCInteraction.interact(nex, "Attack");
                }
            }

            if (!healing
                    && config.specP2()
                    && client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) >= config.specPercent() * 10
                    && client.getBoostedSkillLevel(config.specStyle().getSkill()) >= client.getRealSkillLevel(config.specStyle().getSkill())) {
                specing = true;
            }
            if (client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) < config.specPercent() * 10) {
                specing = false;
            }

        } else if (estado == State.UMBRA) {
            checkPrayer(Prayer.PROTECT_FROM_MISSILES);
            checkPrayer(Prayer.RIGOUR);
            checkBoost(Skill.RANGED);
            checkRangedGear();

            WorldPoint nexWp = nex.getWorldLocation().dx(1).dy(1);
            if (client.getCollisionMaps() == null) return;
            CollisionData col = client.getCollisionMaps()[client.getPlane()];




             if (umbra == null || umbra.isDead()) {
                Optional<NPC> npc = NPCs.search().withId(UMBRA_ID).first();
                if (npc.isPresent()) {
                    umbra = npc.get();
                } else {
                    estado = State.FASE_3;
                }
            } else if (nex.getPoseAnimation() == 9175 || walkUnder) {
                if (nexWp.distanceTo(player.getWorldLocation()) <= 4) {
                    if (darkness > 0) {
                        healing = true;
                    }
                    walkUnder = true;
                    MousePackets.queueClickPacket();
                    MovementPackets.queueMovement(nexWp);
                    log.info("moviendo debajo de nex");
                    return;
                } else {
                    if (walkUnder) {
                        walkUnder = false;
                        return;
                    }
                }
            }else if (weaNegra.size() > 0) {
                    log.info("wea negra!");
                    if (weaNegra.stream().anyMatch(x -> x.getWorldLocation().equals(player.getWorldLocation())) && isIdle(player)) {
                     log.info("debajo mio");
                     WorldPoint wpGamer = player.getWorldLocation();
                     MousePackets.queueClickPacket();
                     if (isWalkable(col, wpGamer.dx(1).dy(0)) && weaNegra.stream().noneMatch(x -> x.getWorldLocation().equals(wpGamer.dx(1).dy(0)))) {
                         MovementPackets.queueMovement(wpGamer.dx(1).dy(0));
                     } else if (isWalkable(col, wpGamer.dx(0).dy(1)) && weaNegra.stream().noneMatch(x -> x.getWorldLocation().equals(wpGamer.dx(0).dy(1)))) {
                         MovementPackets.queueMovement(wpGamer.dx(0).dy(1));
                     } else if (isWalkable(col, wpGamer.dx(-1).dy(0)) && weaNegra.stream().noneMatch(x -> x.getWorldLocation().equals(wpGamer.dx(-1).dy(0)))) {
                         MovementPackets.queueMovement(wpGamer.dx(-1).dy(0));
                     } else if (isWalkable(col, wpGamer.dx(0).dy(-1)) && weaNegra.stream().noneMatch(x -> x.getWorldLocation().equals(wpGamer.dx(0).dy(-1)))) {
                         MovementPackets.queueMovement(wpGamer.dx(0).dy(-1));
                     }
                    }
            } else if (isIdle(player)) {
                if (player.getWorldLocation().distanceTo(nexWp) >= 6) {
                    Actor enemy = player.getInteracting();
                    if (enemy == null || enemy.getName() == null || !enemy.getName().equals(umbra.getName())) {
                        log.info("atakando a umbra");
                        NPCInteraction.interact(umbra, "Attack");
                    }
                } else {
                    LocalPoint nexLp = LocalPoint.fromWorld(client, nexWp);
                    if (nexLp == null) {
                        return;
                    }

                    List<WorldPoint> tilesSafe = config.rangeDistant().getTilesUmbra().stream().filter(x -> WorldPoint.fromLocalInstance(client, nexLp).distanceTo(x) >= 6).collect(Collectors.toList());
                    Optional<WorldPoint> tileCercano = tilesSafe.stream().min(Comparator.comparingInt(x -> x.distanceTo(playerFromLocal)));
                    if (tileCercano.isPresent()) {
                        moverHacia(client, tileCercano.get());
                    } else {
                        log.info("Ningun tile chupo pico");
                    }
                }
            }
        } else if (estado == State.FASE_3) {
            //Special------------------------------------------
            if (specing) {
                if (client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) < config.specPercent() * 10) {
                    specing = false;
                    log.info("speecing disminuido con gametick");
                }
                if (Equipment.search().withId(config.specID()).first().isPresent() && client.getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0) {
                    MousePackets.queueClickPacket();
                    WidgetPackets.queueWidgetActionPacket(1, 10485795, -1, -1);
                }
            }

            //Pray Gear----------------------------------------
            checkPrayer(Prayer.PROTECT_FROM_MAGIC);
            if (specing) {
                checkPrayer(config.specStyle().getPrayer());
                checkGear("spec");
            } else {
                checkGear("melee");
                checkPrayer(Prayer.PIETY);
            }
            checkBoost(Skill.STRENGTH);


            //Atacke y esquive---------------------------------
            WorldPoint destinationTile = null;
            if (client.getLocalDestinationLocation() != null) {
                destinationTile = WorldPoint.fromLocal(client, client.getLocalDestinationLocation());
            }
            List<Player> players = client.getPlayers();
            players.removeIf(x -> x.getName() != null && x.getName().equalsIgnoreCase(client.getLocalPlayer().getName()));

            if (bloodSacrifice > 0) {
                if (player.getWorldLocation().distanceTo(nex.getWorldArea()) <= 6) {
                    if (destinationTile == null || destinationTile.distanceTo(nex.getWorldLocation()) <= 6) {
                        escapeTile = metodo3(7);
                        if (escapeTile != null) {
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(escapeTile);
                        }
                    }
                }
            } else if (nex.getAnimation() == 9183) {
                specing = false;
                boolean fightingReaver = player.getInteracting() != null
                        && player.getInteracting().getName() != null
                        && bloodReaver != null
                        && player.getInteracting().getName().equals(bloodReaver.getName());
                if (!fightingReaver) {
                    NPCInteraction.interact(bloodReaver, "Attack");
                }
            } else if (isIdle(player) && (player.getInteracting() == null || player.getInteracting().getName() == null || !player.getInteracting().getName().equals(nex.getName()))) {
                NPCInteraction.interact(nex, "Attack");
            } else if (isIdle(player) && player.getWorldArea().isInMeleeDistance(nex.getWorldArea()) && isIdle(nex) && players.stream().anyMatch(x -> x.getWorldLocation().distanceTo(player.getWorldLocation()) <= 1)) {
                players.forEach(x -> {
                    if (x.getWorldLocation().distanceTo(player.getWorldLocation()) <= 1) {
                        log.info("nombre: {}", x.getName());
                    }
                });
                log.info("aloneTimer: {}", aloneTimer);
                if (aloneTimer == 0) {
                    aloneTimer = 2 + N.nextInt(9);
                } else {
                    aloneTimer--;
                    if (aloneTimer == 1) {
                        aloneTimer = 0;
                        List<WorldPoint> tilePlayers = players.stream().map(Actor::getWorldLocation).collect(Collectors.toList());
                        WorldPoint nexC = nex.getWorldLocation().dx(1).dy(1);
                        WorldPoint tileAlone = PajauApiPlugin.TilesAvalibleRadial(client, 2, nexC, tile -> {
                            WorldArea areaTile = new WorldArea(tile.dx(-1).dy(-1), 3, 3);
                            boolean tileContaminado = tilePlayers.stream().anyMatch(areaTile::contains);
                            return !tile.equals(nexC.dx(-2).dy(-2)) && !tile.equals(nexC.dx(2).dy(-2)) && !tile.equals(nexC.dx(2).dy(2)) && !tile.equals(nexC.dx(-2).dy(2)) && !tileContaminado;
                        });
                        if (tileAlone != null) {
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(tileAlone);
                        }
                    }
                }


            } else if (!healing
                    && player.getInteracting() != null && player.getInteracting().getName() != null
                    && player.getInteracting().getName().equals(nex.getName())
                    && client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) >= config.specPercent() * 10
                    && client.getBoostedSkillLevel(config.specStyle().getSkill()) >= client.getRealSkillLevel(config.specStyle().getSkill())) {

                specing = true;
            }


        } else if (estado == State.CRUOR) {

            checkPrayer(Prayer.PROTECT_FROM_MAGIC);
            checkPrayer(Prayer.RIGOUR);
            checkBoost(Skill.RANGED);
            checkRangedGear();

            WorldPoint nexLoc = nex.getWorldLocation();
            WorldPoint destTile = null;
            if (client.getLocalDestinationLocation() != null) {
                destTile = WorldPoint.fromLocal(client, client.getLocalDestinationLocation());
            }


            if (cruor == null || cruor.isDead()) {
                Optional<NPC> npc = NPCs.search().withId(NpcID.CRUOR).first();
                if (npc.isPresent()) {
                    cruor = npc.get();
                } else {
                    estado = State.FASE_4;
                }
            } else if (bloodSacrifice > 0) {
                if (player.getWorldLocation().distanceTo(nexLoc) <= 6 || (destTile != null && destTile.distanceTo(nexLoc) <= 6)) {
                    if (destTile == null || destTile.distanceTo(nexLoc) <= 6) {
                        escapeTile = metodo3(7);
                        if (escapeTile != null) {
                            MousePackets.queueClickPacket();
                            MovementPackets.queueMovement(escapeTile);
                            log.info("meow");
                        }
                    }
                } else if (player.getInteracting() == null || player.getInteracting().getName() == null || !player.getInteracting().getName().equals(cruor.getName())) {
                    NPCInteraction.interact(cruor, "Attack");
                }
            } else if (player.getInteracting() == null || player.getInteracting().getName() == null || !player.getInteracting().getName().equals(cruor.getName())) {
                NPCInteraction.interact(cruor, "Attack");
            }
        } else if (estado == State.FASE_4) {
            //Special---------------------------------------------------------------
            if (client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) < config.specPercent() * 10) {
                specing = false;
            }
            //Prayer y Gear---------------------------------------------------------
            if (estalagmitas.size() > 0 && stalagmiteCount <= 2) {
                checkPrayer(Prayer.PROTECT_FROM_MISSILES);
            } else {
                checkPrayer(Prayer.PROTECT_FROM_MAGIC);
            }

            if (estalagmitas.size() > 0) {
                checkGear("wepMelee");
            } else if (containThis > 0) {
                containThis--;
                if (specing && config.specStyle().getSkill() == Skill.RANGED) {
                    checkGear("spec");
                } else {
                    checkGear("ranged");
                }
                checkPrayer(Prayer.RIGOUR);
                checkBoost(Skill.RANGED);
            } else {
                if (specing) {
                    checkPrayer(config.specStyle().getPrayer());
                    checkGear("spec");
                } else {
                    checkGear("melee");
                    checkPrayer(Prayer.PIETY);
                }
                checkBoost(Skill.STRENGTH);
            }

            //atacking and dodging---------------------------------------------------------
            List<Player> players = client.getPlayers();
            players.removeIf(x -> x.getName() != null && x.getName().equalsIgnoreCase(client.getLocalPlayer().getName()));

            if (isIdle(player)) {
                if (estalagmitas.size() > 0) {
                    stalagmiteCount--;
                    WorldArea playerArea = new WorldArea(player.getWorldLocation().dx(-1).dy(-1), 3, 3);

                    if (stalagmiteCount == 4) {
                        healing = true;
                    }

                    if (estalagmitas.stream().allMatch(x -> x.getWorldLocation().isInArea(playerArea))) {
                        players.removeIf(x -> x.getWorldLocation().isInArea(player.getWorldArea()));
                        Optional<Player> nearestPlayer = players.stream().min(Comparator.comparingInt(x -> x.getWorldLocation().distanceTo(playerArea)));
                        if (nearestPlayer.isPresent()) {
                            if (nearestPlayer.get().getWorldLocation().distanceTo(playerArea) >= 4) {
                                healing = true;
                            }
                        }
                    } else {
                        log.info("Stalagmitas");
                        Optional<GameObject> estalagmitaCercana = estalagmitas.stream().min(Comparator.comparingInt(x -> x.getWorldLocation().distanceTo(player.getWorldLocation())));
                        if (estalagmitaCercana.isPresent()) {
                            log.info("Liberando de estalagmitas");
                            TileObjectInteraction.interact(estalagmitaCercana.get(), "Attack");
                        } else {
                            log.info("no se encontro estalagmita cercana");
                        }
                    }
                } else if (containThis > 0) {
                    log.info("Contain this");
                    WorldArea areaContain = new WorldArea(nex.getWorldLocation().dx(-1).dy(-1), 5, 5);
                    if (player.getWorldLocation().isInArea(areaContain)) {
                        WorldPoint tileContainEscape = null;
                        for (int i = 1; i < 6; i++) {
                            tileContainEscape = PajauApiPlugin.TilesAvalibleRadial(client, i, player.getWorldLocation(), x -> !x.isInArea(areaContain));
                            if (tileContainEscape != null) {
                                MousePackets.queueClickPacket();
                                MovementPackets.queueMovement(tileContainEscape);
                                break;
                            }
                        }
                    } else if (player.getInteracting() == null || player.getInteracting().getName() == null || !player.getInteracting().getName().equals(nex.getName())) {
                        if (specing && Equipment.search().withId(config.specID()).first().isPresent() && client.getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0) {
                            MousePackets.queueClickPacket();
                            WidgetPackets.queueWidgetActionPacket(1, 10485795, -1, -1);
                        }
                        NPCInteraction.interact(nex, "Attack");
                        log.info("Pichuleando a la maraka");

                    }
                } else if (player.getInteracting() == null || player.getInteracting().getName() == null || !player.getInteracting().getName().equals(nex.getName())) {
                    if (specing && Equipment.search().withId(config.specID()).first().isPresent() && client.getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0) {
                        MousePackets.queueClickPacket();
                        WidgetPackets.queueWidgetActionPacket(1, 10485795, -1, -1);
                    }
                    NPCInteraction.interact(nex, "Attack");
                    log.info("Atackando a la maraka");
                } else if (player.getWorldArea().isInMeleeDistance(nex.getWorldArea()) && isIdle(nex) && players.stream().anyMatch(x -> x.getWorldLocation().distanceTo(player.getWorldLocation()) <= 1)) {
                    if (aloneTimer == 0) {
                        aloneTimer = 2 + N.nextInt(9);
                    } else {
                        aloneTimer--;
                        if (aloneTimer == 1) {
                            aloneTimer = 0;
                            List<WorldPoint> tilePlayers = players.stream().map(Actor::getWorldLocation).collect(Collectors.toList());
                            WorldPoint nexC = nex.getWorldLocation().dx(1).dy(1);
                            WorldPoint tileAlone = PajauApiPlugin.TilesAvalibleRadial(client, 2, nexC, tile -> {
                                WorldArea areaTile = new WorldArea(tile.dx(-1).dy(-1), 3, 3);
                                boolean tileContaminado = tilePlayers.stream().anyMatch(areaTile::contains);
                                return !tile.equals(nexC.dx(-2).dy(-2)) && !tile.equals(nexC.dx(2).dy(-2)) && !tile.equals(nexC.dx(2).dy(2)) && !tile.equals(nexC.dx(-2).dy(2)) && !tileContaminado;
                            });
                            if (tileAlone != null) {
                                MousePackets.queueClickPacket();
                                MovementPackets.queueMovement(tileAlone);
                            }
                        }
                    }
                }

                if (!healing
                        && client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) >= config.specPercent() * 10
                        && client.getBoostedSkillLevel(config.specStyle().getSkill()) >= client.getRealSkillLevel(config.specStyle().getSkill())) {
                    specing = true;
                }

            } else {
                if (containThis > 0) {
                    if (client.getLocalDestinationLocation() != null) {
                        WorldPoint destTile = WorldPoint.fromLocal(client, client.getLocalDestinationLocation());
                        WorldArea areaContain = new WorldArea(nex.getWorldLocation().dx(-1).dy(-1), 5, 5);
                        if (destTile.isInArea(areaContain)) {
                            WorldPoint tileContainEscape = null;
                            for (int i = 1; i < 6; i++) {
                                tileContainEscape = PajauApiPlugin.TilesAvalibleRadial(client, i, player.getWorldLocation(), x -> !x.isInArea(areaContain));
                                if (tileContainEscape != null) {
                                    MousePackets.queueClickPacket();
                                    MovementPackets.queueMovement(tileContainEscape);
                                    break;
                                }
                            }
                        }
                    }
                }
            }


        } else if (estado == State.GLACIES) {
            checkPrayer(Prayer.RIGOUR);
            checkBoost(Skill.RANGED);

            if (containThis > 0) {
                containThis--;
            }
            if (estalagmitas.size() > 0) {
                if (stalagmiteCount <= 2) {
                    checkPrayer(Prayer.PROTECT_FROM_MISSILES);
                }
                checkGear("wepMelee");
            } else {
                checkPrayer(Prayer.PROTECT_FROM_MAGIC);
                checkRangedGear();
            }

            if (glacies == null || glacies.isDead()) {
                Optional<NPC> npc = NPCs.search().withId(NpcID.GLACIES).first();
                if (npc.isPresent()) {
                    glacies = npc.get();
                } else {
                    estado = State.FASE_5;
                }
            } else if (estalagmitas.size() > 0) {
                List<Player> players = client.getPlayers();
                players.removeIf(x -> x.getName() != null && x.getName().equalsIgnoreCase(client.getLocalPlayer().getName()));
                stalagmiteCount--;
                WorldArea playerArea = new WorldArea(player.getWorldLocation().dx(-1).dy(-1), 3, 3);
                if (stalagmiteCount == 4) {
                    healing = true;
                }

                if (estalagmitas.stream().allMatch(x -> x.getWorldLocation().isInArea(playerArea))) {
                    players.removeIf(x -> x.getWorldLocation().isInArea(player.getWorldArea()));
                    Optional<Player> nearestPlayer = players.stream().min(Comparator.comparingInt(x -> x.getWorldLocation().distanceTo(playerArea)));
                    if (nearestPlayer.isPresent()) {
                        if (nearestPlayer.get().getWorldLocation().distanceTo(playerArea) >= 4) {
                            healing = true;
                        }
                    }
                } else {
                    Optional<GameObject> estalagmitaCercana = estalagmitas.stream().min(Comparator.comparingInt(x -> x.getWorldLocation().distanceTo(player.getWorldLocation())));
                    log.info("Liberando de estalagmitas");
                    TileObjectInteraction.interact(estalagmitaCercana.get(), "Attack");
                }
            } else if (containThis > 0) {
                WorldArea areaContain = new WorldArea(nex.getWorldLocation().dx(-1).dy(-1), 5, 5);
                WorldPoint destTile = null;
                if (client.getLocalDestinationLocation() != null) {
                    destTile = WorldPoint.fromLocal(client, client.getLocalDestinationLocation());
                }

                if (player.getWorldLocation().isInArea(areaContain) || (destTile != null && destTile.isInArea(areaContain))) {
                    if (destTile == null || destTile.isInArea(areaContain)) {
                        WorldPoint tileContainEscape = null;
                        for (int i = 1; i < 6; i++) {
                            tileContainEscape = PajauApiPlugin.TilesAvalibleRadial(client, i, player.getWorldLocation(), x -> !x.isInArea(areaContain));
                            if (tileContainEscape != null) {
                                MousePackets.queueClickPacket();
                                MovementPackets.queueMovement(tileContainEscape);
                                log.info("Se encontro tile de escape");
                                break;
                            }
                        }
                        log.info("meow: {}", tileContainEscape == null ? "no tile de escape" : "escapando de contain");
                    }
                } else if (player.getWorldLocation().distanceTo(glacies.getWorldLocation()) <= config.rangeDistant().getDistant()) {
                    if (player.getInteracting() == null || player.getInteracting().getName() == null || !player.getInteracting().getName().equals(glacies.getName())) {
                        NPCInteraction.interact(glacies, "Attack");
                        log.info("Pichuleando a la maraka");
                    }
                } else if (containThis <= CONTAIN_THIS_DURATION - 5) {
                    if (player.getInteracting() == null || player.getInteracting().getName() == null || !player.getInteracting().getName().equals(glacies.getName())) {
                        if (player.getWorldLocation().distanceTo(glacies.getWorldLocation()) <= config.rangeDistant().getDistant()) {
                            NPCInteraction.interact(glacies, "Attack");
                            log.info("Pichuleando a la maraka");
                        } else {
                            if (isIdle(player)) {
                                moverHacia(client, config.playerKey().getGTile());
                            }
                        }
                    }
                }
            } else if (player.getInteracting() == null || player.getInteracting().getName() == null || !player.getInteracting().getName().equals(glacies.getName())) {
                NPCInteraction.interact(glacies, "Attack");
                log.info("Pichuleando a la maraka");
            }

        } else if (estado == State.FASE_5) {
            NPCs.search().idInList(NEX_IDS).first().ifPresent(x -> nex = x);
            if (nex.getId() == 11282 || nex.isDead() || nex == null) {
                escapeTile = metodo3(5);
                if (escapeTile != null) {
                    MousePackets.queueClickPacket();
                    MovementPackets.queueMovement(escapeTile);
                    log.info("escapando de la cayampa");
                    estado = State.LOOTING;
                    reset();
                    return;
                }
            }

            specing = client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) >= config.specPercent() * 10;

            if (config.pantiesDown()) {
                if (piernasID == -1) {
                    Optional<EquipmentItemWidget> legs = Equipment.search().indexIs(7).first();
                    if (legs.isPresent()) {
                        piernasID = legs.get().getEquipmentItemId();
                        WidgetPackets.queueWidgetAction(legs.get(),"Remove");
                        log.info("piernasID: {}",piernasID);
                    }
                }
            }

            if (config.titsOut()) {
                if (torsoID == -1) {
                    Optional<EquipmentItemWidget> torso = Equipment.search().indexIs(4).first();
                    if (torso.isPresent()) {
                        torsoID = torso.get().getEquipmentItemId();
                        WidgetPackets.queueWidgetAction(torso.get(),"Remove");
                        log.info("torsoID: {}",torsoID);
                    }
                }
            }

            if (nex != null && nex.getId() == 11281) {
                if (specing && config.specStyle().getSkill() == Skill.RANGED) {
                    checkGear("spec",List.of(piernasID,torsoID));
                } else {
                    checkGear("ranged",List.of(piernasID,torsoID));
                }
                checkPrayer(Prayer.RIGOUR);
                checkBoost(Skill.RANGED);
            } else {
                if (specing) {
                    checkGear("spec",List.of(piernasID,torsoID));
                    checkPrayer(config.specStyle().getPrayer());
                } else {
                    checkGear("melee",List.of(piernasID,torsoID));
                    checkPrayer(Prayer.PIETY);
                }
                checkBoost(Skill.STRENGTH);
            }

            if (nex.getInteracting() != null && nex.getInteracting().getName() != null && nex.getInteracting().getName().equals(player.getName())) {
                checkPrayer(Prayer.PROTECT_FROM_MELEE);
            } else {
                checkPrayer(Prayer.PROTECT_FROM_MAGIC);
            }

            if (estalagmitas.size() > 0) {
                Optional<GameObject> estalagmitaCercana = estalagmitas.stream().min(Comparator.comparingInt(x -> x.getWorldLocation().distanceTo(player.getWorldLocation())));
                log.info("Liberando de estalagmitas");
                TileObjectInteraction.interact(estalagmitaCercana.get(), "Attack");
                return;
            }

            if (specing && Equipment.search().withId(config.specID()).first().isPresent() && client.getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetActionPacket(1, 10485795, -1, -1);
            }

            if (player.getInteracting() == null) {
                NPCInteraction.interact(nex, "Attack");
            }


        } else if (estado == State.LOOTING) {
            if (client.getVarbitValue(Varbits.PRAYER_PIETY) == 1) {
                InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.PRAYER_PIETY.getPackedId());
            }
            if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MELEE) == 1) {
                InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.PRAYER_PROTECT_FROM_MELEE.getPackedId());
            }
            if (client.getVarbitValue(Varbits.PRAYER_RIGOUR) == 1) {
                InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.PRAYER_RIGOUR.getPackedId());
            }
            if (client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MAGIC) == 1) {
                InteractionHelper.toggleNormalPrayer(WidgetInfoExtended.PRAYER_PROTECT_FROM_MAGIC.getPackedId());
            }

            if (player.getWorldLocation().isInArea(areaSalida)) {
                if (client.getVarbitValue(13080) > getCA().getKcNeeded()) {
                    Optional<TileObject> puerta = TileObjects.search().withId(42934).first();
                    puerta.ifPresent(x -> TileObjectInteraction.interact(x, "Open"));
                    timeout = 2+N.nextInt(3);
                    return;
                } else {
                    estado = State.GETTING_KC;
                    return;
                }
            } else if (player.getWorldLocation().isInArea(lobbyArea)) {
                if (Inventory.search().idInList(meleeGearIDs).first().isPresent()) {
                    checkGear("melee");
                    log.info("Equipando las weas de melee para bankear {}",meleeGearIDs);
                    timeout = 2;
                    return;
                } else {
                    estado = State.RECOVERY;
                    return;
                }
            }

            Optional<NPC> npc = NPCs.search().idInList(NEX_IDS).first();

            if (npc.isPresent()) {
                if (npc.get().getId() == 11282) {
                    //wait
                } else {
                    //todo buscar que estado estoy
                }
            } else {
                //drop vials
                if (Inventory.getItemAmount(ItemID.VIAL) > 0) {
                    if (remain.isEmpty()) {
                        remain = Inventory.search().withId(ItemID.VIAL).result();
                    }
                    int woof = PajauApiPlugin.nRand.nextInt(6) + 2 + dropIndex;
                    for (int i = dropIndex; i < woof; i++) {
                        if (i < remain.size()) {
                            dropIndex = i + 1;
                            InventoryInteraction.useItem(remain.get(i), "Drop");
                        } else {
                            break;
                        }
                    }
                    return;
                } else {
                    remain = new ArrayList<>();
                    dropIndex = 0;
                }

                if (isIdle(player)) {
                    List<ETileItem> items = TileItems.search().itemsExcludingMatchingWildcardsNoCase("vial").result();
                    if (items.size() > 0 && !Inventory.full()) {
                        List<ETileItem> itemNoSupplies = TileItems.search().itemsExcludingMatchingWildcardsNoCase("shark", "prayer potion*", "saradomin brew", "super restore*").result();
                        if (itemNoSupplies.size() > 0) {
                            MousePackets.queueClickPacket();
                            TileItemPackets.queueTileItemAction(itemNoSupplies.get(0), false);
                        } else {
                            MousePackets.queueClickPacket();
                            TileItemPackets.queueTileItemAction(items.get(0), false);
                        }
                    } else { //inventory full
                        Optional<Widget> zarosItem = Inventory.search().withId(config.zarosItemId()).first();
                        if (zarosItem.isPresent() && !player.getWorldLocation().isInArea(lobbyArea)) {
                            MousePackets.queueClickPacket();
                            WidgetPackets.queueWidgetActionPacket(3, zarosItem.get().getId(), zarosItem.get().getItemId(), zarosItem.get().getIndex());
                            timeout = 1;
                            return;
                        }
                        Optional<TileObject> altar = TileObjects.search().withId(42965).first();
                        if (altar.isPresent()) {
                            MousePackets.queueClickPacket();
                            TileObjectInteraction.interact(altar.get(), "Teleport");
                        }
                    }
                }
            }
        } else if (estado == State.RECOVERY) {
            if (Bank.isOpen()) {
                if (client.getBoostedSkillLevel(Skill.HITPOINTS) < client.getRealSkillLevel(Skill.HITPOINTS)) {
                    Optional<Widget> foodInInventory = BankInventory.search().withId(config.recoveryFoodId()).first();
                    Optional<Widget> foodInBank = Bank.search().withId(config.recoveryFoodId()).first();
                    if (foodInInventory.isPresent()) {
                        BankInventoryInteraction.useItem(foodInInventory.get(), "Eat");
                        timeout = 2 + N.nextInt(2);
                    } else if (foodInBank.isPresent()) {
                        if (Inventory.full()) {
                            List<Integer> shit2Bank = new ArrayList<>(List.of(ItemID.SHARK, ItemID.SARADOMIN_BREW4, ItemID.VIAL, ItemID.SUPER_RESTORE4));
                            shit2Bank.removeIf(x -> x == config.recoveryFoodId());
                            Optional<Widget> shit = BankInventory.search().idInList(shit2Bank).first();
                            shit.ifPresent(x -> BankInventoryInteraction.useItem(x, "Deposit-All"));
                        } else {
                            BankInteraction.useItem(foodInBank.get(), "Withdraw-All");
                            timeout = 1;
                        }
                    } else {
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Recovery food not found", Color.red), "");
                    }
                } else if (client.getBoostedSkillLevel(Skill.PRAYER) < client.getRealSkillLevel(Skill.PRAYER) - 20
                        || client.getBoostedSkillLevel(Skill.RANGED) < client.getRealSkillLevel(Skill.RANGED)
                        || client.getBoostedSkillLevel(Skill.STRENGTH) < client.getRealSkillLevel(Skill.STRENGTH)
                        || client.getBoostedSkillLevel(Skill.ATTACK) < client.getRealSkillLevel(Skill.ATTACK)) {
                    Optional<Widget> restoreInBank = Bank.search().idInList(List.of(ItemID.SUPER_RESTORE1, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE4)).first();
                    Optional<Widget> restoreInInventory = BankInventory.search().idInList(List.of(ItemID.SUPER_RESTORE1, ItemID.SUPER_RESTORE2, ItemID.SUPER_RESTORE3, ItemID.SUPER_RESTORE4)).first();

                    if (restoreInInventory.isPresent()) {
                        BankInventoryInteraction.useItem(restoreInInventory.get(), "Drink");
                        timeout = 2 + N.nextInt(2);
                    } else if (restoreInBank.isPresent()) {
                        if (Inventory.full()) {
                            List<Integer> shit2Bank = new ArrayList<>(List.of(ItemID.SHARK, ItemID.SARADOMIN_BREW4, ItemID.VIAL, config.recoveryFoodId()));
                            Optional<Widget> shit = BankInventory.search().idInList(shit2Bank).first();
                            shit.ifPresent(x -> BankInventoryInteraction.useItem(x, "Deposit-All"));
                        } else {
                            BankInteraction.useItem(restoreInBank.get(), "Withdraw-All");
                            timeout = 2 + N.nextInt(2);
                        }
                    } else {
                        estado = State.APAGADO;
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Restore potion not found for recovery", Color.ORANGE), "");
                    }
                } else {
                    estado = State.BANKING;
                }
            } else {
                if (isIdle(player)) {
                    Optional<NPC> reis = NPCs.search().withId(NpcID.ASHUELOT_REIS_11289).first();
                    if (reis.isPresent()) {
                        NPCInteraction.interact(reis.get(), "Bank");
                    } else {
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Bank not found", Color.red), "");
                    }
                }
            }
        } else if (estado == State.GETTING_KC) {
            if (potTimeout == 0) {
                if (client.getBoostedSkillLevel(Skill.PRAYER) < 15) {
                    Optional<Widget> pot = Inventory.search().matchesWildCardNoCase("prayer*").first();
                    if (pot.isPresent()) {
                        InventoryInteraction.useItem(pot.get(), "Drink");
                        potTimeout = 2;
                    } else {
                        pot = Inventory.search().matchesWildCardNoCase("super restore*").first();
                        if (pot.isPresent()) {
                            InventoryInteraction.useItem(pot.get(), "Drink");
                            potTimeout = 2;
                        } else {
                            estado = State.APAGADO;
                            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Prayer restoration pot not found", Color.red), "");
                        }
                    }
                    return;
                } else if (client.getVarpValue(VarPlayer.POISON) >= 0) {
                    Optional<Widget> pot = Inventory.search().matchesWildCardNoCase("*anti*").first();
                    if (pot.isPresent()) {
                        InventoryInteraction.useItem(pot.get(), "Drink");
                        potTimeout = 2;
                    } else {
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Anti-poison pot not found", Color.red), "");
                    }
                } else if (client.getBoostedSkillLevel(config.kcStyle().getSkill()) <= config.kcLvlReBoost()) {
                    Optional<Widget> pot;
                    if (config.kcStyle() == NexConfig.Style.MELEE) {
                        pot = Inventory.search().matchesWildCardNoCase("*combat*").first();
                    } else {
                        pot = Inventory.search().matchesWildCardNoCase("*ranging*").first();
                    }

                    if (pot.isPresent()) {
                        InventoryInteraction.useItem(pot.get(), "Drink");
                        potTimeout = 2;
                    } else {
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Boost pot not found", Color.red), "");
                    }
                } else if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= 50) {
                    Optional<Widget> food = Inventory.search().withAction("Eat").first();
                    if (food.isPresent()) {
                        InventoryInteraction.useItem(food.get(), "Eat");
                        potTimeout = 3;
                    } else {
                        estado = State.APAGADO;
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Food not found", Color.red), "");
                    }
                }
            }

            checkPrayer(Prayer.PROTECT_FROM_MAGIC);
            checkPrayer(config.kcStyle().getPrayer());
            if (!player.isInteracting()) {
                if (config.kcLoot()) {
                    if (!Inventory.full()) {
                        Optional<ETileItem> loot = TileItems.search().eachItemAboveXValue(2000).first();
                        if (loot.isPresent()) {
                            MousePackets.queueClickPacket();
                            TileItemPackets.queueTileItemAction(loot.get(), false);
                            timeout = N.nextInt(2);
                            return;
                        }
                    }
                }

                //atackar a un npc
                int id;
                if (client.getBoostedSkillLevel(Skill.SLAYER) >= 83 && config.kcStyle() == NexConfig.Style.RANGED) {
                    id = NpcID.SPIRITUAL_MAGE_11292;
                } else {
                    id = NpcID.BLOOD_REAVER;
                }
                Optional<NPC> npc = NPCs.search().interactingWithLocal().first();
                if (npc.isPresent()) {
                    NPCInteraction.interact(npc.get(), "Attack");
                    log.info("atackando");
                    timeout = 2 + N.nextInt(4);
                    return;
                }
                npc = NPCs.search().withId(id).notInteracting().nearestToPlayer();
                if (npc.isPresent()) {
                    NPCInteraction.interact(npc.get(), "Attack");
                    log.info("atackando");
                    timeout = 2 + N.nextInt(4);
                } else {
                    log.info("Npc not found");
                }
            }


        }


    }

    private List<Integer> checkEquipment() {

        Set<Integer> itemsNeeded = new HashSet<>();
        itemsNeeded.addAll(meleeGearIDs);
        itemsNeeded.addAll(rangedGearIDs);
        itemsNeeded.addAll(specGearIDs);
        itemsNeeded.add(config.meleeWepID());

        List<Integer> itemsLeft = new ArrayList<>();

        for (Integer a : itemsNeeded) {
            if (Inventory.search().withId(a).empty() && Equipment.search().withId(a).empty() ) {
                itemsLeft.add(a);
            }
        }

        return itemsLeft;
    }


    private List<List<Object>> checkInv() {
        List<List<Object>> itemNeeded = new ArrayList<>(List.of(
                List.of(ItemID.SUPER_RESTORE4, config.restoreAmount(),"Super restore(4)"),
                List.of(ItemID.SUPER_COMBAT_POTION4, config.combatAmount(), "Super combat potion(4)"),
                List.of(ItemID.RANGING_POTION4, config.rangAmount(), "Ranging potion(4)")
                ));

        if (config.shouldPrepot()) {
            if (config.heart()) {
                itemNeeded.add(List.of(ItemID.SATURATED_HEART,1,"Saturated Heart"));
            }
            if (config.anglerPre()) {
                itemNeeded.add(List.of(ItemID.ANGLERFISH,1 ,"Anglerfish"));
            }
            if (config.antiPre()) {
                itemNeeded.add(List.of(ItemID.ANTIDOTE1_5958,1,"Antidote++ (1)" ));
            }
            if (config.menaphitePre()) {
                itemNeeded.add(List.of(ItemID.MENAPHITE_REMEDY1,1 ,"Menaphite  (1)"));
            }
            if (config.rangPre()) {
                itemNeeded.add(List.of(ItemID.RANGING_POTION1,1 ,"Ranging potion(1)"));
            }
            if (config.combatPre()) {
                itemNeeded.add(List.of(ItemID.SUPER_COMBAT_POTION1,1, "Super combat potion(1)" ));
            }
        }

        List<List<Object>> itemIncorrecto = new ArrayList<>();
        List<Widget> item;

        for (int i = 0; i < itemNeeded.size(); i++) {
            int itemID = (int) itemNeeded.get(i).get(0);
            int itemQty = (int) itemNeeded.get(i).get(1);
            String itemName = (String) itemNeeded.get(i).get(2);
            item = Inventory.search().withId(itemID).result();

            if (item.size() != itemQty) {
                itemIncorrecto.add(List.of(itemID,  itemQty - item.size(), itemName));
            }
        }

        //-------------------------------------------------------------------------------------

        List<Integer> correctItems = new ArrayList<>();

        correctItems.addAll(rangedGearIDs);
        correctItems.addAll(meleeGearIDs);
        correctItems.addAll(specGearIDs);
        correctItems.add(config.meleeWepID());
        correctItems.addAll(List.of(
                ItemID.BOOK_OF_THE_DEAD,
                ItemID.RUNE_POUCH,
                ItemID.DIVINE_RUNE_POUCH,
                ItemID.RUNE_POUCH_L,
                ItemID.DIVINE_RUNE_POUCH_L));
        correctItems.addAll(List.of(ItemID.SUPER_RESTORE4,ItemID.SUPER_COMBAT_POTION4,ItemID.RANGING_POTION4));

        if (config.shouldPrepot()) {
            if (config.heart()) {
                correctItems.add(ItemID.SATURATED_HEART);
            }
            if (config.anglerPre()) {
                correctItems.add(ItemID.ANGLERFISH);
            }
            if (config.antiPre()) {
                correctItems.add(ItemID.ANTIDOTE1_5958);
            }
            if (config.menaphitePre()) {
                correctItems.add(ItemID.MENAPHITE_REMEDY1);
            }
            if (config.rangPre()) {
                correctItems.add(ItemID.RANGING_POTION1);
            }
            if (config.combatPre()) {
                correctItems.add(ItemID.SUPER_COMBAT_POTION1);
            }
        }


        itemsNaqVer = BankInventory.search().filter(x -> !correctItems.contains(x.getItemId())).filterUnique().result();
        log.info("itemsNaqVer: {}",itemsNaqVer.size());

        return itemIncorrecto;

    }

    @Subscribe
    void onNpcSpawned(NpcSpawned event) {
        if (event.getNpc().getId() == NpcID.BLOOD_REAVER_11294) {
            bloodReaver = event.getNpc();
        }
    }

    @Subscribe
    void onNpcDespawned(NpcDespawned event) {
        if (event.getNpc().getId() == NpcID.BLOOD_REAVER_11294) {
            bloodReaver = null;
        }
    }

    @Subscribe
    void onChatMessage(ChatMessage event) {
        if (event.getMessage().contains("Fumus, don't fail me!")) {
            estado = State.FUMUS;
        } else if (event.getMessage().contains("Umbra, don't fail me!")) {
            estado = State.UMBRA;
        } else if (event.getMessage().contains("Cruor, don't fail me!")) {
            estado = State.CRUOR;
        } else if (event.getMessage().contains("Glacies, don't fail me!")) {
            estado = State.GLACIES;
        } else if (event.getMessage().contains("There is...")) {
            thereIs = 1;
        } else if (event.getMessage().contains("*Cough*")) {
            cough = 4;
        } else if (event.getMessage().contains("Nex has marked you for a blood")) {
            bloodSacrifice = 10;
            log.info("marcado");
        } else if (event.getMessage().contains("You managed to escape from Nex")) {
            bloodSacrifice = 0;
            log.info("escape");
        } else if (event.getMessage().contains("Nex unleashes a blood sacrifice!")) {
            bloodSacrifice = 0;
            log.info("le saco la puta");
        } else if (event.getMessage().contains("You failed to escape from Nex's blood sacrifice!")) {
            bloodSacrifice = 0;
            log.info("le saco la puta meow");
        } else if (event.getMessage().contains("Contain this!")) {
            containThis = CONTAIN_THIS_DURATION;
            log.info("contenme esta");
        } else if (event.getMessage().contains("Die now, in a prison of ice!")) {
            prisionero = 7;
        } else if (event.getMessage().contains("Embrace darkness!")) {
            darkness = 30;
        }
    }


    @Subscribe
    void onGameObjectSpawned(GameObjectSpawned event) {
        if (event.getGameObject().getId() == 42942) {
            weaNegra.add(event.getGameObject());
        } else if (event.getGameObject().getId() == STALAGMITE_ID_PRISON) {
            estalagmitas.add(event.getGameObject());
            stalagmiteCount = 10;
        }
    }

    @Subscribe
    void onGameObjectDespawned(GameObjectDespawned event) {
        if (event.getGameObject().getId() == 42942) {
            weaNegra.clear();
        } else if (event.getGameObject().getId() == STALAGMITE_ID_PRISON) {
            estalagmitas.clear();
            stalagmiteCount = 0;
        }
    }

    @Subscribe
    void onVarbitChanged(VarbitChanged event) {
        if (event.getVarpId() == 300) {
            if (event.getValue() < config.specPercent()) {
                specing = false;
                log.info("speecing disminuido con VarbitChanged");
            }
        }
    }

    private void moverHacia(Client client, WorldPoint wp) {
        MousePackets.queueClickPacket();
        MovementPackets.queueMovement(WorldPoint.toLocalInstance(client, wp).toArray(WorldPoint[]::new)[0]);
    }

    public boolean isWalkable(CollisionData colData, int x, int y) {   //x y son coordenadas de Scene
        return (colData.getFlags()[x][y] & (CollisionDataFlag.BLOCK_MOVEMENT_OBJECT + CollisionDataFlag.BLOCK_MOVEMENT_FLOOR +
                CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION)) == 0;
    }

    public boolean isWalkable(CollisionData colData, WorldPoint wp) {   //x y son coordenadas de Scene
        int x = wp.getX() - client.getBaseX();
        int y = wp.getY() - client.getBaseY();
        return (colData.getFlags()[x][y] & (CollisionDataFlag.BLOCK_MOVEMENT_OBJECT + CollisionDataFlag.BLOCK_MOVEMENT_FLOOR +
                CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION)) == 0;
    }

    private WorldPoint metodo1() {
        WorldPoint playerWp = client.getLocalPlayer().getWorldLocation();
        WorldPoint nexWp = nex.getWorldLocation().dx(1).dy(1);
        if (client.getCollisionMaps() == null) {
            return null;
        }

        int x = nexWp.getX();
        int y = nexWp.getY() + 11;
        List<WorldPoint> possibleTile = new ArrayList<>();
        if (playerWp.getX() <= nexWp.getX()) {
            if (playerWp.getY() >= nexWp.getY()) {
                x = nexWp.getX();
                y = nexWp.getY() + 11;
                while (x > nexWp.getX() - 12) {
                    tileBuscados.add(new WorldPoint(x, y, 0));
                    if (isWalkable(client.getCollisionMaps()[client.getPlane()], x - client.getBaseX(), y - client.getBaseY())) {
                        possibleTile.add(new WorldPoint(x, y, 0));
                    }
                    x = x - 2;
                }

                x = nexWp.getX() - 11;
                y = nexWp.getY() - 10;
                while (y >= nexWp.getY()) {
                    tileBuscados.add(new WorldPoint(x, y, 0));
                    if (isWalkable(client.getCollisionMaps()[0], x - client.getBaseX(), y - client.getBaseY())) {
                        possibleTile.add(new WorldPoint(x, y, 0));
                    }
                    y = y - 2;
                }
            } else {
                x = nexWp.getX() - 11;
                y = nexWp.getY() + 2;

                while (y > nexWp.getY() - 12) {
                    tileBuscados.add(new WorldPoint(x, y, 0));
                    if (isWalkable(client.getCollisionMaps()[0], x - client.getBaseX(), y - client.getBaseY())) {
                        possibleTile.add(new WorldPoint(x, y, 0));
                    }
                    y = y - 2;
                }

                x = nexWp.getX() - 10;
                y = nexWp.getY() - 11;
                while (x <= nexWp.getX()) {
                    tileBuscados.add(new WorldPoint(x, y, 0));
                    if (isWalkable(client.getCollisionMaps()[0], x - client.getBaseX(), y - client.getBaseY())) {
                        possibleTile.add(new WorldPoint(x, y, 0));
                    }
                    x = x + 2;
                }
            }
        } else {
            if (playerWp.getY() <= nexWp.getY()) {
                x = nexWp.getX() + 2;
                y = nexWp.getY() - 11;

                while (x < nexWp.getX() + 12) {
                    tileBuscados.add(new WorldPoint(x, y, 0));
                    if (isWalkable(client.getCollisionMaps()[0], x - client.getBaseX(), y - client.getBaseY())) {
                        possibleTile.add(new WorldPoint(x, y, 0));
                    }
                    x = x + 2;
                }

                x = nexWp.getX() + 11;
                y = nexWp.getY() - 10;
                while (y <= nexWp.getY()) {
                    tileBuscados.add(new WorldPoint(x, y, 0));
                    if (isWalkable(client.getCollisionMaps()[0], x - client.getBaseX(), y - client.getBaseY())) {
                        possibleTile.add(new WorldPoint(x, y, 0));
                    }
                    y = y + 2;
                }
            } else {
                x = nexWp.getX() + 11;
                y = nexWp.getY() + 2;

                while (y < nexWp.getY() + 12) {
                    tileBuscados.add(new WorldPoint(x, y, 0));
                    if (isWalkable(client.getCollisionMaps()[0], x - client.getBaseX(), y - client.getBaseY())) {
                        possibleTile.add(new WorldPoint(x, y, 0));
                    }
                    y = y + 2;
                }

                x = nexWp.getX() + 10;
                y = nexWp.getY() + 11;
                while (x > nexWp.getX()) {
                    tileBuscados.add(new WorldPoint(x, y, 0));
                    if (isWalkable(client.getCollisionMaps()[0], x - client.getBaseX(), y - client.getBaseY())) {
                        possibleTile.add(new WorldPoint(x, y, 0));
                    }
                    x = x - 2;
                }
            }
        }

        Optional<WorldPoint> finalTile = possibleTile.stream().min(Comparator.comparingInt(z -> z.distanceTo(playerWp)));
        finalTile.ifPresent(tile -> log.info("escapeTile {}", tile));
        return finalTile.orElse(null);

    }


    private WorldPoint metodo2() {
        Player player = client.getLocalPlayer();
        WorldPoint nexLoc = nex.getWorldLocation().dx(1).dy(1);
        WorldPoint escapeTile = null;
        if (client.getCollisionMaps() == null) {
            return null;
        }

        CollisionData col = client.getCollisionMaps()[0];
        int dx = nexLoc.getX() - player.getWorldLocation().getX();
        int dy = nexLoc.getY() - player.getWorldLocation().getY();

        if (dx >= 0 && Math.abs(dx) >= Math.abs(dy)) {
            for (int i = 0; i < mcTries; i++) {
                int ax = nexLoc.getX() - 7 - N.nextInt(5);
                int ay = player.getWorldLocation().getY() - R + N.nextInt(2 * R + 1);

                if (isWalkable(col, ax - client.getBaseX(), ay - client.getBaseY())) {
                    escapeTile = new WorldPoint(ax, ay, client.getPlane());
                    break;
                }
            }
        } else if (dx <= 0 && Math.abs(dx) >= Math.abs(dy)) {
            for (int i = 0; i < mcTries; i++) {
                int ax = nexLoc.getX() + 7 + N.nextInt(5);
                int ay = player.getWorldLocation().getY() - R + N.nextInt(2 * R + 1);

                if (isWalkable(col, ax - client.getBaseX(), ay - client.getBaseY())) {
                    escapeTile = new WorldPoint(ax, ay, client.getPlane());
                    break;
                }
            }
        } else if (dy > 0 && Math.abs(dy) > Math.abs(dx)) {
            for (int i = 0; i < mcTries; i++) {
                int ax = player.getWorldLocation().getX() - R + N.nextInt(2 * R + 1);
                int ay = nexLoc.getY() - 7 - N.nextInt(2 * R + 1);

                if (isWalkable(col, ax - client.getBaseX(), ay - client.getBaseY())) {
                    escapeTile = new WorldPoint(ax, ay, client.getPlane());
                    break;
                }
            }
        } else if (dy < 0 && Math.abs(dy) > Math.abs(dx)) {
            for (int i = 0; i < mcTries; i++) {
                int ax = player.getWorldLocation().getX() - R + N.nextInt(2 * R + 1);
                int ay = nexLoc.getY() + 7 + N.nextInt(2 * R + 1);

                if (isWalkable(col, ax - client.getBaseX(), ay - client.getBaseY())) {
                    escapeTile = new WorldPoint(ax, ay, client.getPlane());
                    break;
                }
            }
        }

        return escapeTile;

    }


    private WorldPoint metodo3(int radio) {
        WorldPoint nexLoc = nex.getWorldLocation().dx(1).dy(1);
        Player player = client.getLocalPlayer();


        int cara = 0;
        int dx = nexLoc.getX() - player.getWorldLocation().getX();
        int dy = nexLoc.getY() - player.getWorldLocation().getY();
        List<WorldPoint> runTile = new ArrayList<>();

        if (dx >= 0 && Math.abs(dx) >= Math.abs(dy)) {
            cara = 0;
        } else if (dx <= 0 && Math.abs(dx) >= Math.abs(dy)) {
            cara = 2;
        } else if (dy > 0 && Math.abs(dy) > Math.abs(dx)) {
            cara = 1;
        } else if (dy < 0 && Math.abs(dy) > Math.abs(dx)) {
            cara = 3;
        }

        log.info("cara inicial: {}", cara);

        for (int i = 0; i < 4; i++) {
            runTile = buscarEn(cara, radio, 2);
            if (runTile != null && runTile.size() > 0) {
                break;
            }
            cara = (cara + 1) % 4;
        }
        if (runTile != null && runTile.size() > 0) {
            Optional<WorldPoint> escapeTile = runTile.stream().min(Comparator.comparingInt(x -> {
                return Math.abs(x.getX() - player.getWorldLocation().getX()) + Math.abs(x.getY() - player.getWorldLocation().getY());
            }));
            return escapeTile.orElse(null);
        }
        return null;
    }


    private List<WorldPoint> buscarEn(int cara, int radio, int step) {
        WorldPoint nexLoc = nex.getWorldLocation().dx(1).dy(1);

        WorldPoint dungeonTile = new ArrayList<>(WorldPoint.toLocalInstance(client, DUNGEON_TILE)).get(0);
        WorldArea dungeonArea = new WorldArea(dungeonTile, 27, 27);

        if (client.getCollisionMaps() == null) {
            return null;
        }
        List<WorldPoint> runTile = new ArrayList<>();
        int x0, y0;

        WorldPoint tilePrueba = null;
        if (cara == 0) {    //izquierda
            x0 = nexLoc.getX() - (radio + 1);
            y0 = nexLoc.getY() - (radio + 1);
            while (y0 < nexLoc.getY() + radio + 2) {
                tilePrueba = new WorldPoint(x0, y0, 0);
                if (isWalkable(client.getCollisionMaps()[0], x0 - client.getBaseX(), y0 - client.getBaseY()) && tilePrueba.isInArea(dungeonArea)) {
                    runTile.add(tilePrueba);
                }
                y0 += step;
            }
        } else if (cara == 2) { //derecha
            x0 = nexLoc.getX() + (radio + 1);
            y0 = nexLoc.getY() - (radio + 1);
            while (y0 < nexLoc.getY() + radio + 2) {
                tilePrueba = new WorldPoint(x0, y0, 0);
                if (isWalkable(client.getCollisionMaps()[0], x0 - client.getBaseX(), y0 - client.getBaseY()) && tilePrueba.isInArea(dungeonArea)) {
                    runTile.add(new WorldPoint(x0, y0, 0));
                }
                y0 += step;
            }
        } else if (cara == 3) { //arriba
            x0 = nexLoc.getX() - (radio + 1);
            y0 = nexLoc.getY() + (radio + 1);
            while (x0 < nexLoc.getX() + radio + 2) {
                tilePrueba = new WorldPoint(x0, y0, 0);
                if (isWalkable(client.getCollisionMaps()[0], x0 - client.getBaseX(), y0 - client.getBaseY()) && tilePrueba.isInArea(dungeonArea)) {
                    runTile.add(new WorldPoint(x0, y0, 0));
                }
                x0 += step;
            }
        } else if (cara == 1) { //abajo
            x0 = nexLoc.getX() - (radio + 1);
            y0 = nexLoc.getY() - (radio + 1);
            while (x0 < nexLoc.getX() + radio + 2) {
                tilePrueba = new WorldPoint(x0, y0, 0);
                if (isWalkable(client.getCollisionMaps()[0], x0 - client.getBaseX(), y0 - client.getBaseY()) && tilePrueba.isInArea(dungeonArea)) {
                    runTile.add(new WorldPoint(x0, y0, 0));
                }
                x0 += step;
            }
        }
        return runTile;
    }

}
