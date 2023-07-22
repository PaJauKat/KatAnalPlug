package com.example.cooker;

import com.example.EthanApiPlugin.Collections.*;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.BankInventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.ObjectPackets;
import com.example.Packets.TileItemPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;
import org.w3c.dom.html.HTMLImageElement;

import javax.inject.Inject;
import java.awt.*;
import java.util.Optional;

@Slf4j
@PluginDescriptor(
        name = "<html>[<font color=\"#FA4444\">P</font>] Cooker</html>",
        tags = {"pajau","cooker"},
        enabledByDefault = false
)
public class CookerPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private CookerConfig config;

    private boolean enAccion = false;
    private int quieto = 0;
    private int timeout = 0;

    private void reset() {

    }

    @Provides
    CookerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(CookerConfig.class);
    }


    @Subscribe
    void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("cooker") ) {
            if (event.getKey().equals("onOff") ) {
                enAccion=!enAccion;
                clientThread.invoke(() -> {
                    if (!enAccion) {
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Apagado", Color.red), "");
                    } else {
                        reset();
                        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", ColorUtil.wrapWithColorTag("Prendido", Color.green), "");
                    }
                });
            }
        }
    }

    @Subscribe
    void onGameTick(GameTick event) {
        if (!enAccion) {
            return;
        }
        if (timeout > 0) {
            timeout--;
            return;
        }

        if (client.getWidget(WidgetInfo.BANK_CONTAINER) != null) {
            Optional<Widget> cooked = BankInventory.search().withId(config.id() + 2).first();
            Optional<Widget> burnt = BankInventory.search().nameContains("Burnt").first();
            Optional<Widget> raw = Bank.search().withId(config.id()).first();
            if (cooked.isPresent()) {
                BankInventoryInteraction.useItem(cooked.get(), "Deposit-All");
            } else if (burnt.isPresent()) {
                BankInventoryInteraction.useItem(burnt.get(), "Deposit-All");
            } else if (raw.isPresent()) {
                BankInteraction.useItem(raw.get(), "Withdraw-All");
                Optional<TileObject> fire = TileObjects.search().withId(config.fireID()).first();
                fire.ifPresent(tileObject -> TileObjectInteraction.interact(tileObject,"Cook"));
                quieto = 5;
            } else {
                log.info("No item ID found");
            }
        } else if (client.getLocalPlayer().getPoseAnimation() == client.getLocalPlayer().getIdlePoseAnimation()) {
            if (client.getLocalPlayer().getAnimation() == -1) {
                quieto++;
            }else {
                quieto =0 ;
            }

            if (Inventory.search().withId(config.id()).result().isEmpty()) {

                if (config.bankType() == CookerConfig.bank.NPC) {
                    Optional<NPC> bank = NPCs.search().withId(config.bankID()).first();
                    bank.ifPresent(npc -> NPCInteraction.interact(npc, "Bank"));
                } else {
                    Optional<TileObject> bank = TileObjects.search().withId(config.bankID()).first();
                    bank.ifPresent(tileObject -> ObjectPackets.queueObjectAction(config.bankOp(), tileObject.getId(), tileObject.getX(), tileObject.getY(), false));
                }
            } else {
                if (quieto > 3) {
                    if (client.getWidget(270, 0) != null) {
                        int rawAmount = Inventory.getItemAmount(config.id());
                        WidgetPackets.queueResumePause(17694734,rawAmount);
                        quieto = 0;
                    }else {
                        Optional<TileObject> fire = TileObjects.search().withId(config.fireID()).first();
                        fire.ifPresent(tileObject -> TileObjectInteraction.interact(tileObject, "Cook"));
                        timeout = 1;
                    }
                }

            }
        }

    }
}
