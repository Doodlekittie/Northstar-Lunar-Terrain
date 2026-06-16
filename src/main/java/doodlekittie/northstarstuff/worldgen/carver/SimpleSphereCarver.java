package doodlekittie.northstarstuff.worldgen.carver;

import com.mojang.serialization.Codec;
import doodlekittie.northstarstuff.worldgen.carver.configuration.SimpleSphereCarverConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;

public class SimpleSphereCarver extends WorldCarver<SimpleSphereCarverConfiguration> {
    private static final double H = 0.09160797; // Magic number

    public SimpleSphereCarver(Codec<SimpleSphereCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean isStartChunk(SimpleSphereCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= config.probability;
    }

    private static boolean shouldSkip() {
        return false;
    }

    @Override
    public boolean carve(@NonNull CarvingContext context, SimpleSphereCarverConfiguration config, @NonNull ChunkAccess chunk,
                         @NonNull Function<BlockPos, Holder<Biome>> biomeAccessor, @NonNull RandomSource random, @NonNull Aquifer aquifer,
                         @NonNull ChunkPos chunkPos, @NonNull CarvingMask mask) {
        CarveSkipChecker skipchecker = (a, b, c, d, e) -> shouldSkip();

        var length = config.length.sample(random);
        var radius = config.radius.sample(random);
        var step = radius / 10;

        var x = chunkPos.getBlockX(random.nextIntBetweenInclusive(0, 15));
        var z = chunkPos.getBlockZ(random.nextIntBetweenInclusive(0, 15));
        var y = config.y.sample(random, context);

        if(!chunk.getBlockState(new BlockPos(x, y, z)).is(config.replaceable)) {
            return false;
        }

        var pitch = 0f;
        var yaw = 0f;

        for (var i = 0; i < length; i++) {
            if(!chunk.getBlockState(new BlockPos(x, y, z)).is(config.replaceable)) {
                break;
            }

            var r = config.radius.sample(random);
            this.carveEllipsoid(context, config, chunk, biomeAccessor, aquifer, x, y, z, r, r, mask, skipchecker);

            var addPitch = 0.1 / (random.nextFloat() + H) - H;
            var addYaw = 0.1 / (random.nextFloat() + H) - H;
            pitch = (float) (pitch + random.nextFloat() > 0.5 ? addPitch : -addPitch);
            yaw = (float) (yaw + random.nextFloat() > 0.5 ? addYaw : -addYaw);

            x = x + (int) Math.floor(step * Math.cos(pitch) * Math.sin(yaw));
            z = z + (int) Math.floor(step * Math.cos(pitch) * Math.cos(yaw));
            y = y + (int) Math.floor(step * Math.sin(yaw));
        }

        return true;
    }
}
