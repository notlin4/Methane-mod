package me.wolfie.methane;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "methane")
public class MethaneSettings implements ConfigData {
    @Comment("Toggle whether to keep fog settings even with Methane disabled.")
    public static boolean persistFogSettings = false;
    public static boolean disableAirFog = false; // the fog pass that obscures terrain
    public static boolean disableWaterFog = false; // the fog layer that tints everything blue
    public static boolean disableLavaFog = false; // the thing that tints everything orange
    public static boolean disablePowderedSnowFog = true; // pretty self-explanatory
    @Comment("(you should probably disable this)")
    public static boolean disableThickFog = false; // Nether Fog pass
    public static boolean disableSkyFog = false; // I think this is another fog pass
}