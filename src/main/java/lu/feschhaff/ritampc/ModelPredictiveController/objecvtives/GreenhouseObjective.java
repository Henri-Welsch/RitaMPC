package lu.feschhaff.ritampc.ModelPredictiveController.objecvtives;

import lombok.Getter;
import lombok.Setter;
import lu.feschhaff.ritampc.PredictionModel.Models.Objects.ModelBundle;
import lu.feschhaff.ritampc.ModelPredictiveController.optimizers.CostFunction;

import java.util.Map;

/**
 * @author Henri-Welsch
 * @sources {}
 */
@Getter @Setter
public class GreenhouseObjective {
    private final ModelBundle[] modelBundles;
    private Map<String, Float> currentState;
    private float[] targetState;
    private float[] predictedState;
    CostFunction costFunction = new CostFunction();

    public GreenhouseObjective(
            ModelBundle[] modelBundles,
            Map<String, Float> currentState,
            float[] targetState
    ) {
        this.modelBundles = modelBundles;
        this.currentState = currentState;
        this.targetState = targetState;

        predictedState = new float[modelBundles.length];
    }

    public double evaluate(Map<String, Float> actionVector) {
        actionVector.putAll(currentState);

        int position = 0;
        for (ModelBundle modelBundle : modelBundles) {
            predictedState[position++] = modelBundle.predict(actionVector);
        }

        return CostFunction.calculateCost(null, predictedState, targetState);
    }
}
