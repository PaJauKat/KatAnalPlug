package com.example;

import com.example.AgroReset.agroResetPlugin;
import com.example.AutoTele.AutoTele;
import com.example.CaTimer.CaTimerPlugin;
import com.example.Callisto.CallistoPlugin;
import com.example.Caminador.TestCaminadorPlugin;
import com.example.E3t4g.e3t4g;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.Gorilas.GorilasPlugin;
import com.example.GreenDrags.GreenDragsPlugin;
import com.example.LavaRunecrafter.LavaRunecrafterPlugin;
import com.example.NightmareHelper.NightmareHelperPlugin;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.PrayerFlicker.EthanPrayerFlickerPlugin;
import com.example.Robador.RobadorPlugin;
import com.example.Toacito.ToacitoPlugin;
import com.example.UpkeepPlugin.UpkeepPlugin;
import com.example.alchemicalhydra.AlchemicalHydraPlugin;
import com.example.blackjacking.Blackjacking;
import com.example.faldita.FaldaPlugin;
import com.example.fungus.FungusPlugin;
import com.example.gauntletFlicker.gauntletFlicker;
import com.example.harpoon2ticker.SwordFish2Tick;
import com.example.hydra.HydraPlugin;
import com.example.nexSimple.NexSimplePlugin;
import com.example.superglass.SuperGlassMakerPlugin;
import com.example.theatre.TheatrePlugin;
import com.example.venenatis.VenenatisPlugin;
import com.example.worldFinder.WorldFinderPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(EthanApiPlugin.class, PacketUtilsPlugin.class, EthanPrayerFlickerPlugin.class,
                gauntletFlicker.class,
                SuperGlassMakerPlugin.class, UpkeepPlugin.class, LavaRunecrafterPlugin.class,
                NightmareHelperPlugin.class, SwordFish2Tick.class
                , e3t4g.class, AutoTele.class,
                TheatrePlugin.class, ToacitoPlugin.class, RobadorPlugin.class, GreenDragsPlugin.class, GorilasPlugin.class,
                CaTimerPlugin.class, CallistoPlugin.class, FaldaPlugin.class, agroResetPlugin.class, NexSimplePlugin.class,
                VenenatisPlugin.class, WorldFinderPlugin.class, Blackjacking.class, FungusPlugin.class, HydraPlugin.class,
                AlchemicalHydraPlugin.class, TestCaminadorPlugin.class
                );
        RuneLite.main(args);
    }
}