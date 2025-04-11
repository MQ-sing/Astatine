# Astatine 1.12.2
A Minecraft 1.12.2 mod that optimizing/improving some things miscellaneously.

# Core Features
### Fast Random (Experimental)
Partial unstable port of [Francium mod](https://github.com/MCTeamPotato/Francium).  
Replaces `Random` with `ThreadLocalRandom` in specific classes via bytecode editing.

**Not tested completely! May cause unexpected problems.**

### FastLang

Include the [FastLang](https://www.mcmod.cn/class/17229.html) mod features:  
Avoid loading entire game resources(As what F3+T do) when select language,just load language itself.

This mod also `Splitter`/replaces regex-based .lang parsing with a manual state machine.  

*MojangAB uses `Splitter`s to parse the most EXTREME COMPLEX syntax:`key=value`*  
*What are they doing all the days?*

### Multi Language
Allows selecting multiple language just like resource packs in a selecting GUI.  
For this implement, the performance is much higher than [Compromise mod](https://github.com/Nova-Committee/Compromise) which has similar features.It combines language keys into the hashmap directly instead iterating over the selected language list every time when solve a translation key.  

This is a backporting of [Language Reload mod](https://github.com/Jerozgen/LanguageReload).

### Starfield Overhaul

Rewrite Vanilla's star generating logic,allows to modify:
- The count of stars
- Basic size of stars
- Size deviation of stars,also include the random method(linear-offset/exponential/log-normal)
- Random seed to generate stars

Overhauls star generation by replacing linear offset with selectable custom algorithms. Despite using a faster RNG, the added algorithmic complexity may increase game load time.
### Star twinkling

Rewrite `net.minecraft.world.World#getStarBrightnessBody` method, implement a twinkling effect for stars.

Timing function for brightness: sin(t), where t represents time. 
Considering to add more brightness functions.
Allow controlling:
- The basic brightness
- Twinkling frequency
- Twinkling amplitude
### Force ASCII Font

Do what the [ForceASCIIFont Mod](https://github.com/ZekerZhayard/ForceASCIIFont) do,  
Fixes bug: render all characters as Unicode font style if the language file of the corresponding language contains non-ASCII characters.

Just overrides FontRenderer.getUnicodeFlag() to always return `false`.

### Eating Anytime
Bypasses canEat() checks in EntityPlayer.
- Creative mode players can eat anytime.
- Configurable to allow eating any time,ignores any eating precondition.

### World Time Scaling
Increase the world time once `N` ticks(N is an integer) only in order to slow down day-cycle in times.

### Chunk Caching

Chunks with total kept time ≥ chunkcache.minLoadedTimeToRetain (ticks) are cached.

Sort cached chunks by total kept time (ascending).

Every chunkcache.cleanupInterval ticks,For the first 50% of entries:
- Remove if time since last load ≥ chunkcache.maxIdleTimeToPurge (ticks).
- Top chunkcache.minReservedChunks chunks (longest load time) are immune to eviction. 

While caching things,if cache size ≥ chunkcache.maxCacheSize, evicts the least-loaded non-reserved chunk at the same time.

### Item Stack Merging
Forces merging of any nearby EntityItem regardless of stackability.  

Modifies EntityItem.combineItems() to replace getMaxStackSize() with Integer.MAX_VALUE.  
*Other vanilla checks would still prevent merging.*

### Rendering Skip
Avoid rendering (Tile)Entities beyond max(Tile)EntityRenderDistance(Euclidean distance).

### Projectile Hiding
Render projectiles only if their age (ticks) exceeds the threshold.

## About the configuration system
Location: config/astatine.properties.
Read once at game startup. No runtime reloading...  

The configuration file would be generated with complete comments and default values.

## Planned Features
*These features are just an idea,to the actual implement may be a far time.*
- Rewrite weather particles(rain/snow).*I don't know what it should be actually*
- Migrate current configuration system to the [ConfigAnyTime Mod](https://www.mcmod.cn/class/11060.html) which support runtime configuration modifying and visual config screen.*Incompatible with current configuration file syntax.*  

*These features are in development but ready to use.May cause some problems.*
- Remove the stats system completely.*May invalidates advancement system.*
- Save chunks that player has reached instead of saving them all after unloading.Conflicts with vanilla's village generating.
- Try to implement a bit of [Lithium](https://github.com/CaffeineMC/lithium) features:
  - Faster data collections&algorithms.(e.g. replace `HashSet<Long>` with `LongOpenHashSet`)