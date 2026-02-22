package lu.feschhaff.ritampc.mpc.optimizers;

/**
 * @author Henri-Welsch
 * @sources {}
 */
public class CostFunction {

    public static double calculateCost(float[] weight, float[] pred, float[] tar) {
        if (pred.length != tar.length) {
            throw new IllegalArgumentException("pred and tar must have the same length");
        }

        double cost = 0;
        for (int t = 0; t < pred.length; t++) {
            float w = (weight != null) ? weight[t] : 1.0f;
            cost += w * Math.pow(pred[t] - tar[t], 2);
        }

        return cost;
    }

}
