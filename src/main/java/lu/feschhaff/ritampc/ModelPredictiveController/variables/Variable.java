package lu.feschhaff.ritampc.ModelPredictiveController.variables;

import java.util.Random;

/**
 * @author Henri-Welsch
 * @sources {}
 */
public interface Variable {
    float sample(Random random);
}
