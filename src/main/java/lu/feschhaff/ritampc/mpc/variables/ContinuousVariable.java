package lu.feschhaff.ritampc.mpc.variables;

import lombok.AllArgsConstructor;

import java.util.Random;

/**
 * @author Henri-Welsch
 * @sources {}
 */
@AllArgsConstructor
public class ContinuousVariable implements Variable {
    private final double lower;
    private final double upper;

    @Override
    public float sample(Random random) {
        return (float) (lower + random.nextDouble() * (upper - lower));
    }
}
