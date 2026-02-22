package lu.feschhaff.ritampc.models.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.util.Map;

/**
 * @author Henri-Welsch
 * @sources {}
 */
@AllArgsConstructor @Getter @Log4j2
public class ModelBundle {
    private ModelConfig modelConfig;
    private Booster booster;

    public float predict(Map<String, Float> input) {
        float[] floats = new float[input.size()];

        int position = 0;
        for (String feature : modelConfig.getFeatures()) {
             floats[position++] = input.get(feature);
        }

        try {
            DMatrix dMatrix = new DMatrix(floats, 1, floats.length, Float.NaN);
            return booster.predict(dMatrix)[0][0];
        } catch (XGBoostError e) {
            log.error(e.getMessage());
        }

        return 0;
    }
}
