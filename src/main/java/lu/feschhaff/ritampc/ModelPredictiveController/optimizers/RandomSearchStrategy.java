package lu.feschhaff.ritampc.ModelPredictiveController.optimizers;

import lu.feschhaff.ritampc.ModelPredictiveController.objecvtives.GreenhouseObjective;
import lu.feschhaff.ritampc.ModelPredictiveController.variables.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Henri-Welsch
 * @sources {
 *     <a href="https://www.youtube.com/watch?v=a6V1uIEZcjU">...</a>
 *     <a href="https://www.educative.io/courses/optimization-for-machine-learning-with-numpy-and-scipy/random-search-optimization?utm_source=chatgpt.com">...</a>
 *     <a href="https://www.numberanalytics.com/blog/fundamentals-random-search-optimization?utm_source=chatgpt.com">...</a>
 *     <a href="https://www.do-mpc.com/en/latest/theory_mpc.html">...</a>
 * }
 */
public class RandomSearchStrategy {
    private final Random random = new Random();

    public OptimizationResult optimize(GreenhouseObjective objective, Map<String, Variable> variables, int iterations) {
        double bestCost = Double.POSITIVE_INFINITY;
        HashMap<String, Float> bestCandidate =  new HashMap<>();

        for (int i = 0; i < iterations; i++) {
            Map<String, Float> candidate = new HashMap<>();

            for (Map.Entry<String, Variable> entry : variables.entrySet()) {
                candidate.put(entry.getKey(), entry.getValue().sample(random));
            }

            double cost = objective.evaluate(candidate);
            if (cost < bestCost) {
                bestCost = cost;
                bestCandidate =  new HashMap<>(candidate);
            }
        }

        return new OptimizationResult(bestCandidate, bestCost);
    }
}
