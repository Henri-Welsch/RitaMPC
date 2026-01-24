package lu.feschhaff.ritampc;

import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.util.HashMap;
import java.util.Map;

// https://xgboost.readthedocs.io/en/latest/jvm/java_intro.html
public class GradientBoostingManager {
    public static void main(String[] args) {
        GradientBoostingManager gradientBoostingManager = new GradientBoostingManager();

        try {
            DMatrix exampleTrainingData = gradientBoostingManager.getExampleTrainingData();
            DMatrix exampleEvaluationData = gradientBoostingManager.getExampleEvaluationData();

            gradientBoostingManager.trainAndGetModel(exampleTrainingData, exampleEvaluationData);
        } catch (XGBoostError error) {
            error.printStackTrace();
        }
    }

    public Booster trainAndGetModel(DMatrix trainingData, DMatrix evaluationData) throws XGBoostError {
        Map<String, Object> params = new HashMap<String, Object>() {
            {
                put("eta", 1.0);
                put("max_depth", 2);
                put("objective", "binary:logistic");
                put("eval_metric", "logloss");
            }
        };

        // Optional: watch list lets you track performance
        Map<String, DMatrix> watches = new HashMap<>();
        watches.put("train", trainingData);
        watches.put("eval", evaluationData);

        int numRounds = 10;  // number of boosting rounds
        return XGBoost.train(trainingData, params, numRounds, watches, null, null);
    }

    public DMatrix getExampleTrainingData() throws XGBoostError {
        float[] trainData = {1.0f,2.0f, 3.0f,4.0f, 5.0f,6.0f};
        float[] trainLabels = {0f, 1f, 0f};
        String[] labelNames = {"label1", "label2"};

        int rows = 3;
        int cols = 2;
        float missingValue = 0.0f;

        DMatrix dMatrix = new DMatrix(trainData, rows, cols, missingValue);
        dMatrix.setLabel(trainLabels);
        dMatrix.setFeatureNames(labelNames);

        return dMatrix;
    }

    public DMatrix getExampleEvaluationData() throws XGBoostError {
        float[] evalData = {2.0f,3.0f, 4.0f,5.0f};
        float[] evalLabels = {1f, 0f};
        String[] labelNames = {"label1", "label2"};

        int rows = 2;
        int cols = 2;
        float missingValue = 0.0f;

        DMatrix dMatrix = new DMatrix(evalData, rows, cols, missingValue);
        dMatrix.setLabel(evalLabels);
        dMatrix.setFeatureNames(labelNames);

        return dMatrix;
    }
}
