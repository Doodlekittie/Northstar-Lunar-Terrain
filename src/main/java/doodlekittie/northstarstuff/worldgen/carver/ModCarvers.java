package doodlekittie.northstarstuff.worldgen.carver;

import doodlekittie.northstarstuff.worldgen.carver.configuration.SimpleSphereCarverConfiguration;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static doodlekittie.northstarstuff.NorthstarStuff.MODID;

public class ModCarvers {
    public static final DeferredRegister<WorldCarver<?>> CARVER_TYPES =
            DeferredRegister.create(Registries.CARVER, MODID);

    public static final DeferredHolder<WorldCarver<?>, SimpleSphereCarver> SIMPLE_SPHERE =
            CARVER_TYPES.register("simple_sphere", () -> new SimpleSphereCarver(SimpleSphereCarverConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        CARVER_TYPES.register(eventBus);
    }
}
