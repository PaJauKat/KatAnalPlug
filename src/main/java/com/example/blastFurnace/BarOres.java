package com.example.blastFurnace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;

import java.util.List;

@Getter
@AllArgsConstructor
public enum BarOres {
    IRON(ItemID.IRON_BAR,ItemID.IRON_ORE,-1, Varbits.BLAST_FURNACE_IRON_BAR,List.of(ItemID.IRON_ORE)),
    STEEL(ItemID.STEEL_BAR,ItemID.IRON_ORE,ItemID.COAL,Varbits.BLAST_FURNACE_STEEL_BAR,List.of(ItemID.IRON_ORE)),
    MITHRIL(ItemID.MITHRIL_BAR,ItemID.MITHRIL_ORE,ItemID.COAL, Varbits.BLAST_FURNACE_MITHRIL_BAR,List.of(ItemID.MITHRIL_ORE,ItemID.COAL,ItemID.MITHRIL_ORE)),
    ADAMANT(ItemID.ADAMANTITE_BAR, ItemID.ADAMANTITE_ORE,ItemID.COAL,Varbits.BLAST_FURNACE_ADAMANTITE_BAR,List.of(ItemID.COAL,ItemID.ADAMANTITE_ORE)),
    GOLD(ItemID.GOLD_BAR,ItemID.GOLD_ORE,-1,Varbits.BLAST_FURNACE_GOLD_BAR,List.of(ItemID.GOLD_ORE)),
    RUNITE(ItemID.RUNITE_BAR,ItemID.RUNITE_ORE,ItemID.COAL,Varbits.BLAST_FURNACE_RUNITE_BAR,List.of(ItemID.RUNITE_ORE)),
    SILVER(ItemID.SILVER_BAR,ItemID.SILVER_ORE,-1,Varbits.BLAST_FURNACE_SILVER_BAR,List.of(ItemID.SILVER_ORE));

    private final int bar;
    private final int primary;
    private final int secondary;
    private final int barVarbit;
    private final List<Integer> ciclo;
}
