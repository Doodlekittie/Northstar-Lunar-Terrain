package doodlekittie.northstarstuff.registry;

import doodlekittie.northstarstuff.worldgen.noise.CircleNoise;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import static doodlekittie.northstarstuff.NorthstarStuff.MODID;

public class ModRegistries {
    public static final ResourceKey<Registry<CircleNoise.NoiseParameters>> CIRCLE_NOISE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MODID, "circle_noise"));

    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                CIRCLE_NOISE_REGISTRY_KEY,
                CircleNoise.NoiseParameters.DIRECT_CODEC
        );
    }
}
