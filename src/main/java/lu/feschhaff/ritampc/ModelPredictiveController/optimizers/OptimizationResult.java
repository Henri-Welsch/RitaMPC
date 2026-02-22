package lu.feschhaff.ritampc.ModelPredictiveController.optimizers;

import java.util.HashMap;

/**
 * @author Henri-Welsch
 * @sources {}
 */
public class OptimizationResult {

    private final HashMap<String, Float> bestSolution;
    private final double bestCost;

    public OptimizationResult(HashMap<String, Float> bestSolution, double bestCost) {
        this.bestSolution = bestSolution;
        this.bestCost = bestCost;
    }

    public HashMap<String, Float> getBestSolution() {
        return bestSolution;
    }

    public double getBestCost() {
        return bestCost;
    }
}
