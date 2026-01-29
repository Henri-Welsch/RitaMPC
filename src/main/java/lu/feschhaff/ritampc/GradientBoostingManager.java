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

    public static Booster trainAndGetModel(DMatrix trainingData, DMatrix evaluationData) throws XGBoostError {
        Map<String, Object> params = new HashMap<String, Object>() {
            {
                put("eta", 1.0);
                put("max_depth", 2);
                put("objective", "reg:squarederror");
                put("eval_metric", "logloss");
            }
        };

        // Optional: watch list lets you track performance
        Map<String, DMatrix> watches = new HashMap<>();
        watches.put("train", trainingData);

        if (evaluationData != null) {
            watches.put("eval", evaluationData);
        }

        int numRounds = 10;  // number of boosting rounds
        return XGBoost.train(trainingData, params, numRounds, watches, null, null);
    }

    public static DMatrix toDMatrix(FeatureSubSet subset) throws XGBoostError {
        int numCols = subset.getFeatures().size();
        int numRows = subset.getFeatures().get(0).getFeatures().size();

        // Flatten feature data row-major
        float[] data = new float[numRows * numCols];
        int idx = 0;

        for (FeaturePoint fp : subset.getFeatures()) {
            for (Float value : fp.getFeatures()) {
                data[idx++] = value != null ? value : Float.NaN;
            }
        }

        // Create DMatrix
        DMatrix matrix = new DMatrix(data, numRows, numCols, Float.NaN);

        // Labels
        float[] labels = new float[numRows];
        for (int i = 0; i < numRows; i++) {
            labels[i] = subset.getLabel().getFeatures().get(i);
        }
        matrix.setLabel(labels);

        // featureNames
        String[] featureNames = new String[numCols];
        for (int i = 0; i < numCols; i++) {
            featureNames[i] = subset.getFeatures().get(i).getEntity_id();
        }
        matrix.setFeatureNames(featureNames);

        return matrix;
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
