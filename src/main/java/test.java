import doodlekittie.northstarstuff.worldgen.noise.CircleNoise;

public class test {
    public static void main(String[] args) {
        var noise = new CircleNoise(new CircleNoise.NoiseParameters(6, 2), 12345L);
        var range = 100;
        for (var x = -range; x <= range; x++) {
            StringBuilder line = new StringBuilder();
            for (var y = -30; y <= 30; y++) {
                line.append(" ").append((int) Math.floor(noise.getValue(x, y) * 10));
            }
            System.out.println(line);
        }
    }
}
