package lu.feschhaff.ritampc.ModelPredictiveController;

import lu.feschhaff.ritampc.ModelPredictiveController.config.GoalCofiguration;
import lu.feschhaff.ritampc.ModelPredictiveController.optimizers.RandomSearchStrategy;

/**
 * @author Henri-Welsch
 * @sources {
 *     <a href="https://www.sciencedirect.com/topics/materials-science/predictive-control-model">Predictive Control Model</a>
 *     <a href="https://www.mathworks.com/help/mpc/gs/what-is-mpc.html">What Is Model Predictive Control?</a>
 *     <a href="https://www.mdpi.com/2079-9292/11/4/554">Application Strategies of Model Predictive Control </a>
 *     <a href="https://futurepublishingllc.com/?p=6142">NN- based MPC in Multiagent System</a>
 *     <a href="https://engineering.purdue.edu/~zak/Second_ed/MPC_handout.pdf">Model-based Predictive Contr</a>
 * }
 */
public class MpcController {

    private final RandomSearchStrategy optimizer;
    private final int horizon = 3;
    private final GoalCofiguration goalCofiguration;

    public MpcController(RandomSearchStrategy optimizer, GoalCofiguration goalCofiguration) {
        this.optimizer = optimizer;
        this.goalCofiguration = goalCofiguration;
    }

    // The MPC entrypoint should accept "current state" and return the first control action.
    public double computeNextAction(double currentTemp) {
//        Objective objective = buildObjective(currentTemp);

        // Optimize over the horizon; then apply only the first action.
        // OptimizationResult result = optimizer.optimize(objective);

        //return extractFirstAction(result);
        return 0;
    }

//    private Objective buildObjective(double currentTemp) {
//        // TODO: create an Objective that:
//        // 1) interprets the vector as u[0..horizon-1] (and possibly other actuators)
//        // 2) rolls out predictions from currentTemp for horizon steps
//        // 3) computes cost using goalCofiguration
//        throw new UnsupportedOperationException("Implement objective rollout + cost for MPC.");
//    }
//
//     private double extractFirstAction(OptimizationResult result) {
//        // TODO: depends on how OptimizationResult stores the best vector
//        // (e.g., Map<String, Float> bestVector with keys like \"heater_0\")
//        throw new UnsupportedOperationException("Implement best-action extraction from OptimizationResult.");
//    }
}