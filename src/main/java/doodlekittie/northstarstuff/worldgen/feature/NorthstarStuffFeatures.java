package doodlekittie.northstarstuff.worldgen.feature;

import doodlekittie.northstarstuff.NorthstarStuff;
import doodlekittie.northstarstuff.worldgen.feature.configuration.ReplaceBlockConfiguration;
import doodlekittie.northstarstuff.worldgen.feature.configuration.TerracedPoolConfiguration;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public abstract class NorthstarStuffFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, NorthstarStuff.MODID);

    public static final DeferredHolder<Feature<?>, TerracedPoolFeature> TERRACED_POOL =
            FEATURES.register("terraced_pool", () -> new TerracedPoolFeature(TerracedPoolConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, ReplaceBlockFeature> REPLACE_BLOCK =
            FEATURES.register("replace_block", () -> new ReplaceBlockFeature(ReplaceBlockConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
