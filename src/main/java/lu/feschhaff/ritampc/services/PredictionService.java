package lu.feschhaff.ritampc.services;

import lombok.extern.log4j.Log4j2;
import lu.feschhaff.ritampc.Registries.BoosterRegistry;
import lu.feschhaff.ritampc.Registries.EntityRegistry;
import lu.feschhaff.ritampc.Registries.MicrometerRegistry;
import lu.feschhaff.ritampc.models.dtos.response.Response;
import lu.feschhaff.ritampc.models.objects.ModelBundle;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Service responsible for periodically running ML predictions using
 * XGBoost models and the latest entity state snapshot.
 *
 * @author Henri-Welsch
 * @sources {
 *     <a href="https://xgboost.readthedocs.io/en/latest/jvm/java_intro.html">Getting Started with XGBoost4J</a>
 *     <a href="https://www.geeksforgeeks.org/springboot/spring-boot-annotations/">Spring Boot - Annotations</a>
 * }
 */
@Service @Log4j2
public class PredictionService {
    private final EntityRegistry entityRegistry;
    private final BoosterRegistry boosterRegistry;
    private final MicrometerRegistry micrometerRegistry;

    Set<String> predictionTargets = new HashSet<>(Arrays.asList(
            "awair_element_82522_score",
            "awair_element_82522_temperature",
            "awair_element_82522_vocs"
    ));

    public PredictionService(
            EntityRegistry entityRegistry,
            BoosterRegistry boosterRegistry,
            MicrometerRegistry micrometerRegistry) {
        this.entityRegistry = entityRegistry;
        this.boosterRegistry = boosterRegistry;
        this.micrometerRegistry = micrometerRegistry;
    }

    /**
     * Scheduled job that periodically runs predictions for all configured targets.
     */
    @Scheduled(initialDelay = 10000, fixedRate = 10000)
    private void predictionSchedule() {
        Map<String, Response> entityRegistrySnapshot = entityRegistry.getEntityRegistrySnapshot();

        for (String predictionTarget : predictionTargets) {
            int stepsAhead = 0;
            ModelBundle modelBundle = boosterRegistry.getModelBundle(predictionTarget, stepsAhead);

            if (modelBundle == null) {
                String message = "No booster found for [target: {}] and [stepsAhead: {}]";
                log.info(message, predictionTarget,stepsAhead);
                continue;
            }

            List<String> featuresUsedForTraining = modelBundle.getModelConfig().getFeatures();

            try {
                float[] featureValues = extractValues(entityRegistrySnapshot, featuresUsedForTraining);
                DMatrix dMatrix = convertFloatsToDMatrix(featureValues);

                Booster booster = modelBundle.getBooster();
                float[][] predictionResult = makePrediction(booster, dMatrix);

                float v = predictionResult[0][0];
                micrometerRegistry.updateGauge(predictionTarget, "5m", v);

                log.info("Prediction for {}{}, based on {}", predictionTarget, Arrays.deepToString(predictionResult), Arrays.toString(featureValues));
            } catch (XGBoostError e) {
                log.warn("Converting array to DMatrix failed!", e);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        int stateStoreServiceSize = entityRegistry.getEntityRegistry().size();
        log.info("State Store contains {} elements!", stateStoreServiceSize);
    }


    /**
     * Executes a prediction using the given booster and input matrix.
     *
     * @param booster trained XGBoost model
     * @param dMatrix feature matrix used to predict
     * @return prediction result as a 2D float array
     * @throws Exception if XGBoost prediction fails
     */
    public float[][] makePrediction(Booster booster, DMatrix dMatrix) throws XGBoostError {
        return booster.predict(dMatrix);
    }


    /**
     * Extracts feature values from an entity registry in provided order.
     *
     * @param entitySnapshot snapshot of the entity registry
     * @param featuresToExtract ordered list of required feature IDs
     * @return array of extracted feature values from the entity registry
     */
    private float[] extractValues(Map<String, Response> entitySnapshot, List<String> featuresToExtract) throws NullPointerException {
        float[] result = new float[featuresToExtract.size()];
        int index = 0;

        for (String feature : featuresToExtract) {
            Response response = entitySnapshot.get(feature);

            if (response != null) {
                String state = response.getEvent().getData().getNew_state().getState();
                result[index++] = Float.parseFloat(state);
            } else {
                result[index++] = Float.NaN;
            }
        }

        return result; // TODO: Maybe a prediction threshold, to many NaNs cant be good.
    }

    public DMatrix convertFloatsToDMatrix(float[] floats) throws XGBoostError {
        return new DMatrix(floats, 1, floats.length, Float.NaN);
    }
}
