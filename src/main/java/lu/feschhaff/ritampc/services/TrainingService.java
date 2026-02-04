package lu.feschhaff.ritampc.services;

import lombok.extern.log4j.Log4j2;
import lu.feschhaff.ritampc.DataManager;
import lu.feschhaff.ritampc.models.objects.FeaturePoint;
import lu.feschhaff.ritampc.models.objects.FeatureSubSet;
import lu.feschhaff.ritampc.models.dtos.response.Response;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Joé Welsch
 * @sources {
 *     <a href="https://xgboost.readthedocs.io/en/latest/jvm/java_intro.html">...</a>
 *     <a href="https://www.baeldung.com/spring-scheduled-tasks">...</a>
 * }
 */

@EnableScheduling @Service @Log4j2
public class TrainingService {
    private final StateStoreService stateStoreService;

    @Value("${booster.model.location}")
    private String boosterModelLocation;


    public TrainingService(StateStoreService stateStoreService) {
        this.stateStoreService = stateStoreService;
    }

    public static void main(String[] args) {

        try {
            DMatrix exampleTrainingData = TrainingService.getExampleTrainingData();
            DMatrix exampleEvaluationData = TrainingService.getExampleEvaluationData();

            TrainingService.trainAndGetModel(exampleTrainingData, exampleEvaluationData);
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




        // TODO, I think there is an error here
        // Labels
        float[] labels = new float[numRows];
        for (int i = 0; i < numRows; i++) {
            try {
                if (subset.getLabel() != null) {
                    Float v = subset.getLabel().getFeatures().get(i);
                    labels[i] = v ==  null ? Float.NaN : v;
                }
            } catch (Exception e) {
                log.error("Failed to read label {}", subset.getLabel(), e);
            }
        }

        // Create DMatrix
        DMatrix matrix = new DMatrix(data, numRows, numCols, Float.NaN);
        matrix.setLabel(labels);

        // featureNames
        String[] featureNames = new String[numCols];
        for (int i = 0; i < numCols; i++) {
            featureNames[i] = subset.getFeatures().get(i).getEntity_id();
        }
        matrix.setFeatureNames(featureNames);

        return matrix;
    }

    public static DMatrix getExampleTrainingData() throws XGBoostError {
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

    public static DMatrix getExampleEvaluationData() throws XGBoostError {
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


    // @Scheduled(initialDelay = 1000, fixedRate = 1000)
    public void predict() throws XGBoostError, IOException {
        Map<String, Response> stateStore = stateStoreService.getStateStore();
        Map<String, Response> stateStoreSnapshot = new HashMap<>(stateStore);

        Set<Path> paths = DataManager.listFilesUsingDirectoryStream(boosterModelLocation);

        for (Path path : paths) {
            if (!path.getFileName().toString().endsWith(".json")) { continue; }

            String pathAsString = path.toString();

            Booster booster = XGBoost.loadModel(pathAsString);
            String[] featureNames = booster.getFeatureNames();
            List<FeaturePoint> features = new ArrayList<>();

            for (String featureName : featureNames) {
                Response response = stateStoreSnapshot.get(featureName);

                if (response == null) {
                    log.warn("Skipping prediction -- Could not find feature record {} in map!", featureName);
                    continue;
                }
                String state = response.getEvent().getData().getNew_state().getState();

                float stateAsFloat = Float.parseFloat(state);
                FeaturePoint featurePoint = new FeaturePoint(featureName, stateAsFloat);
                features.add(featurePoint);
            }

            FeatureSubSet featureSubSet = new FeatureSubSet(null, features);
            DMatrix dMatrix = TrainingService.toDMatrix(featureSubSet);

            float[][] predict = booster.predict(dMatrix);

            String[] s = pathAsString.split("__");
            String s1 = s[s.length - 1];
            log.info("Predicted {}, for {}" , Arrays.deepToString(predict), s1);
        }
    }

    @Scheduled(initialDelay = 10000, fixedRate = 10000)
    private void stateStoreSchedule() {
        int stateStoreSize = stateStoreService.getStateStore().size();
        log.debug("Current state store size: {}", stateStoreSize);
    }
}
