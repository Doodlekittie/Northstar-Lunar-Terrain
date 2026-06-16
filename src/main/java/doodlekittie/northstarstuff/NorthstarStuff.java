package doodlekittie.northstarstuff;

import doodlekittie.northstarstuff.block.PointedScorchiaBlock;
import doodlekittie.northstarstuff.block.PointedScoriaBlock;
import doodlekittie.northstarstuff.registry.ModRegistries;
import doodlekittie.northstarstuff.worldgen.carver.ModCarvers;
import doodlekittie.northstarstuff.worldgen.densityfunction.ModDensityFunctions;
import doodlekittie.northstarstuff.worldgen.feature.NorthstarStuffFeatures;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(NorthstarStuff.MODID)
public class NorthstarStuff {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "northstarstuff";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();



    // Create a Deferred Register to hold Blocks which will all be registered under the "northstarstuff" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
//    // Create a Deferred Register to hold Items which will all be registered under the "northstarstuff" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
//    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "northstarstuff" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

//    // Creates a new Block with the id "northstarstuff:example_block", combining the namespace and path
    public static final DeferredBlock<Block> METEOR_STONE = BLOCKS.registerSimpleBlock("meteor_stone",
        BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.DEEPSLATE).sound(SoundType.DEEPSLATE));

    public static final DeferredBlock<PointedScoriaBlock> POINTED_SCORIA =
        BLOCKS.register("pointed_scoria", registryName -> new PointedScoriaBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.POINTED_DRIPSTONE)
        ));
    public static final DeferredBlock<PointedScorchiaBlock> POINTED_SCORCHIA =
            BLOCKS.register("pointed_scorchia", registryName -> new PointedScorchiaBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.POINTED_DRIPSTONE)
            ));

//    // Creates a new BlockItem with the id "northstarstuff:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> POINTED_SCORIA_ITEM = ITEMS.registerSimpleBlockItem("pointed_scoria", POINTED_SCORIA);
    public static final DeferredItem<BlockItem> POINTED_SCORCHIA_ITEM = ITEMS.registerSimpleBlockItem("pointed_scorchia", POINTED_SCORCHIA);
    public static final DeferredItem<BlockItem> METEOR_STONE_ITEM = ITEMS.registerSimpleBlockItem("meteor_stone", METEOR_STONE);

//    // Creates a new food item with the id "northstarstuff:example_id", nutrition 1 and saturation 2
//    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
//            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

//    // Creates a creative tab with the id "northstarstuff:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.northstarstuff")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> POINTED_SCORIA_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(POINTED_SCORIA_ITEM.get());
                output.accept(POINTED_SCORCHIA_ITEM.get());
                output.accept(METEOR_STONE_ITEM.get());// Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public NorthstarStuff(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModRegistries::registerDatapackRegistries);

        NorthstarStuffFeatures.register(modEventBus);
        ModCarvers.register(modEventBus);
        ModDensityFunctions.DENSITY_FUNCTION_TYPES.register(modEventBus);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (NorthstarStuff) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
//        NeoForge.EVENT_BUS.register(ModRegistries.class);

//        // Register the item to a creative tab
//        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
//    private void addCreative(BuildCreativeModeTabContentsEvent event) {
//        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
//            event.accept(EXAMPLE_BLOCK_ITEM);
//        }
//    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
//    @SubscribeEvent
//    public void onServerStarting(ServerStartingEvent event) {
//        // Do something when the server starts
//        LOGGER.info("HELLO from server starting");
//    }

//    @SubscribeEvent
//    public static void registerRegistries(NewRegistryEvent event) {
//        event.register(CIRCLE_NOISE_REGISTRY);
//    }
}
