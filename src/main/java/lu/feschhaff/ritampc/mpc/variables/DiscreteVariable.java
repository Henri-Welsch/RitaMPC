package lu.feschhaff.ritampc.mpc.variables;

import lombok.AllArgsConstructor;

import java.util.Random;

/**
 * @author Henri-Welsch
 * @sources {}
 */
@AllArgsConstructor
public class DiscreteVariable implements Variable {
    private final float[] allowedValues;

    @Override
    public float sample(Random random) {
        return allowedValues[random.nextInt(allowedValues.length)];
    }
}
