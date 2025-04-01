package com.sing.astatine;

import com.sing.astatine.utils.config.*;

public class Configuration {
    @ConfigComment("Allows players to eat food anytime, including in Creative mode")
    public static boolean alwaysEatable = false;
    @ConfigComment("Let creative mode players eat food without consuming the item")
    public static boolean creativeEating = true;
    @ConfigComment("Skip rendering for titlentities that out of the distance,-1 to disable")
    public static double maxTileEntityRenderDistance=-1;
    @ConfigComment("Skip rendering for entities that out of the distance,-1 to disable")
    public static double maxEntityRenderDistance=-1;
    @ConfigComment("Disable stats system,may increase performance.")
    public static boolean disableStats=false;
    @ConfigComment({ "Enables 32-bit integer storage for item stack sizes in world saves (vanilla: 8-bit byte).",
            "Affects only storage format, does NOT modify in-game stack limits",
            "Potential conflicts with other stack-extending mods",
            "Required for safe handling of stacks >127 when using force-stack-merging"})
    public static boolean enableExtendedStackStorage =false;
    @ConfigComment({
            "Delays rendering of projectile entities (snowballs/ender pearls/etc.) to prevent view obstruction,in ticks.",
            "Backports from new versions."
    })
    public static int projectileRenderDelay=2;
    @ConfigComment({"Allows item entities to merge beyond normal stack limits.",
            "WARNING: REQUIRES extended-stack-storage-format to prevent data loss on chunk unload(e.g. use enableExtendedStackStorage option)"})
    public static boolean forceItemEntityMerge =false;
    @Hidden
    @ConfigComment({
            "Force not to calculate the world brightness while rendering weather particles.",
            "That could avoid reading block information.",
            "-1 for Vanilla behavior"})
    public static boolean forceWeatherParticleUseConstLight = false;
    @Group("lang")
    public static class Lang {
        @ConfigComment("Forces ASCII font rendering for all text elements, " +
                "fixing incorrect Unicode font rendering when non-ASCII characters are present")
        public static boolean forceAsciiFont = true;

        @ConfigComment({
                "Optimizes language file loading by:",
                "- Skipping redundant resource reloads when switching languages",
                "- Replacing regex-based .lang file parsing with a manual parser"
        })
        public static boolean fastLang = true;
        @ConfigComment({
                "Disables Forge's extended .lang file processing",
                "Recommended for better performance when using optimizeLanguageLoading",
                "Disable if experiencing missing translation keys"
        })
        public static boolean disableForgeLangExtensions = false;
        @Experimental
        @ConfigComment({"Enables enhanced language selection interface with drag-and-drop prioritization",
                "- Pack-style language stacking (similar to resource packs)",
                "- Visual language preview support",
                "- Multiple language layer blending"})
        public static boolean languageSelector = false;
    }

    @ConfigComment({"Cache loaded chunks.",
            "WARNING: Directly storing Chunk objects might cause compatibility issues with some mods."})
    @Experimental
    @Group("chunkcache")
    public static class ChunkCache {
        public static boolean enabled = false;
        @ConfigComment({"Maximum number of chunks allowed in the cache.",
                "Higher values may improve chunk loading performance but increase memory usage.",
                "Minimum value: 16"})
        public static int maxCacheSize = 16384;
        @ConfigComment({"Minimum number of chunks guaranteed to be retained in cache,",
                "This helps reduce reloading overhead for core areas (e.g. player bases)."})
        public static int minReservedChunks = 127;
        @ConfigComment({"Indicate the increment speed of interest-level of chunks that be loaded.",
                "Greater value:cache more chunks",
                "Smaller value:cache less chunks",
                "Default: 100"})
        @Hidden
        //not implemented yet
        public static int priorityIncrementFactor = 100;
        @ConfigComment({
                "Interval in game ticks between automatic cache cleanup checks.",
                "1 second = 20 ticks (at 20 TPS).",
                "Default: 3200 ticks (2.5 minutes)"
        })
        public static int cleanupInterval = 3200;

        @ConfigComment({
                "Minimum priority score required to retain chunks during cleanup.",
                "Chunks with scores below this threshold will be removed first.",
                "Set higher to keep fewer chunks; lower to preserve more."
        })
        public static int minLoadedTimeToRetain = 250;
        @ConfigComment({
                "Maximum idle time (in ticks) a cached chunk can remain unaccessed",
                "before becoming eligible for cleanup. Higher values keep chunks cached longer",
                "but may increase memory usage. Combines with access frequency for cleanup decisions"
        })
        public static int maxIdleTimeToPurge = 0;
        @ConfigComment("Print debug info in console")
        public static boolean debug=false;
    }

    @Group("world.time")
    public static class WorldTime {
        @ConfigComment({"Controls world time advancement granularity:",
                "- Value N: World time increments by 1 tick every N real-time ticks",
                "- Vanilla equivalent: 1 (Time progresses 1:1 with real ticks)",
                "- Higher values = Slower world time progression",
                "Example:",
                "3 -> World time advances 1 tick every 3 server ticks"})
        public static int worldTimeAdvancementInterval = 1;
        @ConfigComment("Displays in-game time in HUD overlay")
        public static boolean showGameTime = false;
        @ConfigComment("X-axis offset for time display overlay")
        public static int timeDisplayXOffset = 2;
        @ConfigComment("Y-axis offset for time display overlay")
        public static int timeDisplayYOffset = 4;
    }


    @Experimental
    @Group("random")
    public static class Random {
        @ConfigComment({"Replaces Vanilla's java.util.Random " +
                "with ThreadLocalRandom in specific classes for better thread performance.",
                "Behaves strangely most of the time."})
        public static boolean fastRandom = false;
        @Hidden
        @ConfigComment({
                "Replaces default random generator with XorShift128 implementation",
                "Conflicts with ThreadLocalRandom optimization"
        })
        public static boolean useFasterRandom = false;
    }

    @Group("star.gen")
    public static class StarGen {
        public static boolean enabled = false;
        @ConfigComment({
                "Controls number of visible stars in night sky",
                "0: Disable stars | >0: Custom count | Default: 1500"
        })
        public static int count = 3000;
        @ConfigComment({
                "Base size multiplier for generated stars (Vanilla: 0.15)"
        })
        public static double baseSize = 0.09;
        @ConfigComment({
                "Controls the random fluctuation amplitude for star sizes",
                "- Actual size = baseSize +- (random * amplitude)",
                "- Vanilla default: 0.1",
                "Example:",
                "0.3 -> Stars vary between ~30% of base size",
                "0.0 -> All stars have identical size"
        })
        public static double sizeFluctuation = 0.3;
        @ConfigComment({
                "Controls what method should be used while generating star sizes.",
                "Given f as the star size,b as baseSize.",
                "s for sizeFluctuation value,rand() for a random function that return a [0,1) value,randI()=rand()*2-1",
                "Allowed methods:",
                "0 -- liner-offset: f=b+randI()*s",
                "1 -- exponential: f=1-(ln(1-randI()))*s",
                "2 -- log-normal: f=e^([nextGaussian]()*s)"
        })
        public static int sizeGenerateMethod=3;
        @ConfigComment({
                "Seed value for star pattern generation",
                "0: Random seed each session | Requires star generation mixins"
        })
        public static long seed = 23333;
    }

    @Group("star.twinkling")
    @ConfigComment({
            "Controls star twinkling animation parameters",
            "Mathematical model:",
            "brightness = (base + sin(time * frequency) * amplitude) * timeFactor"
    })
    public static class StarTwinkling {
        public static boolean enabled = true;
        @ConfigComment({
                "Oscillation frequency for brightness variation",
                "Higher values = Faster twinkling",
                "Unit: radians per tick"
        })
        public static float frequency = 0.2F;
        @ConfigComment({"Peak brightness variation amplitude",
                "Actual range: [-amplitude, +amplitude]",
                "Example: 0.1 -> ~10% brightness variation"
        })
        public static float amplitude = 0.1F;
        @ConfigComment({
                "Base brightness level before modulation",
                "Vanilla default: 0.5",
                "Range: [0.0, 1.0]"
        })
        public static float base = 0.5F;
        @ConfigComment({
                "Time-of-day attenuation coefficient",
                "0.0: Have nothing to do with day time",
                "1.0: Full day/night cycle effect",
                "Vanilla behavior: 1.0"
        })
        public static float timeAttenuation = 1;
    }
}
