package doodlekittie.northstarstuff.worldgen.carver;

import com.mojang.serialization.Codec;
import doodlekittie.northstarstuff.worldgen.carver.configuration.SimpleSphereCarverConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
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
        CarveSkipChecker skipchecker = (a, b, c, d, e) -> false;

        var length = config.length.sample(random);
        var radius = config.radius.sample(random);
        var step = radius / 10;

        double x = chunkPos.getBlockX(random.nextIntBetweenInclusive(0, 15));
        double z = chunkPos.getBlockZ(random.nextIntBetweenInclusive(0, 15));
        double y = config.y.sample(random, context);

        var startPos = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        if(!chunk.getBlockState(startPos).is(config.replaceable)) {
            return false;
        }

        var pitch = 0f;
        var yaw = 0f;

        for (var i = 0; i < length; i++) {
            var currentPos = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
            if(!chunk.getBlockState(currentPos).is(config.replaceable)) {
                break;
            }

            var r = config.radius.sample(random);
            if (Math.floorDiv((int) Math.floor(x), 16) == chunk.getPos().x
                    && Math.floorDiv((int) Math.floor(z), 16) == chunk.getPos().z) {
                this.carveSphere(x, y, z, r, chunk, config);
            }

            var addPitch = 0.1 / (random.nextFloat() + H) - H;
            var addYaw = 0.1 / (random.nextFloat() + H) - H;
            pitch = (float) (pitch + random.nextFloat() > 0.5 ? addPitch : -addPitch);
            yaw = (float) (yaw + random.nextFloat() > 0.5 ? addYaw : -addYaw);

            x = x + Math.floor(step * Math.cos(pitch) * Math.sin(yaw));
            z = z + Math.floor(step * Math.cos(pitch) * Math.cos(yaw));
            y = y + Math.floor(step * Math.sin(pitch));
        }

        return true;
    }

    private void carveSphere(double x, double y, double z, float radius, ChunkAccess chunk, SimpleSphereCarverConfiguration config) {
        // x, y, z, are the sphere centre
        // rX, rY, rZ, are relative to the centre
        for (var rX = 0; rX < radius; rX++) {
            for (var rY = 0; rY < radius; rY++) {
                for (var rZ = 0; rZ < radius; rZ++) {
                    if (rX * rX + rY * rY + rZ * rZ <= radius * radius) {
                        carveAndMirror((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z), rX, rY, rZ, chunk, config);
                    }
                }
            }
        }
    }

    private void carveAndMirror(int oX, int oY, int oZ, int rX, int rY, int rZ, ChunkAccess chunk, SimpleSphereCarverConfiguration config) {
        var relativePos = new BlockPos.MutableBlockPos(rX, rY, rZ);
        var absolutePos = new BlockPos.MutableBlockPos(0, 0, 0);

        for (var xM : new int[] {1, -1}) {
            relativePos.setX(relativePos.getX() * xM);
            for (var yM : new int[]{1, -1}) {
                relativePos.setY(relativePos.getY() * yM);
                for (var zM : new int[]{1, -1}) {
                    relativePos.setZ(relativePos.getZ() * zM);
                    absolutePos.set(oX + relativePos.getX(), oY + relativePos.getY(), oZ + relativePos.getZ());
                    if(chunk.getBlockState(absolutePos).is(config.replaceable)) {
                        chunk.setBlockState(absolutePos, Blocks.AIR.defaultBlockState(), false);
                    }
                }
            }
        }
    }
}
