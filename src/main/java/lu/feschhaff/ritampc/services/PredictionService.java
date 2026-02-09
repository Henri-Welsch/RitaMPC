package lu.feschhaff.ritampc.services;

import lombok.extern.log4j.Log4j2;
import lu.feschhaff.ritampc.Registries.BoosterRegistry;
import lu.feschhaff.ritampc.Registries.EntityRegistry;
import lu.feschhaff.ritampc.models.dtos.response.Response;
import lu.feschhaff.ritampc.models.objects.BoosterModel;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
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
    private final TrainingService trainingService;

    Set<String> predictionTargets = new HashSet<>(Arrays.asList(
            "awair_element_82522_score",
            "awair_element_82522_temperature",
            "awair_element_82522_vocs"
    ));

    public PredictionService(
            EntityRegistry entityRegistry,
            BoosterRegistry boosterRegistry,
            TrainingService trainingService
    ) {
        this.entityRegistry = entityRegistry;
        this.boosterRegistry = boosterRegistry;
        this.trainingService = trainingService;
    }

    /**
     * Scheduled job that periodically runs predictions for all configured targets.
     */
    @Scheduled(initialDelay = 10000, fixedRate = 10000)
    private void predictionSchedule() {
        Map<String, Response> entityRegistrySnapshot = entityRegistry.getEntityRegistrySnapshot();

        for (String predictionTarget : predictionTargets) {
            Optional<BoosterModel> bestBooster = boosterRegistry.findBestBooster(predictionTarget);

            if (bestBooster.isEmpty()) {
                log.warn("No booster found for prediction target [{}]", predictionTarget);
                continue;
            }

            BoosterModel boosterModel = bestBooster.get();
            List<String> usedFeatures = boosterModel.getModelMetaData().getUsedFeatures();

            try {
                float[] featureValues = extractValues(entityRegistrySnapshot, usedFeatures);
                DMatrix dMatrix = trainingService.convertFloatsToDMatrix(featureValues);

                Booster booster = boosterModel.getBooster();
                float[][] predictionResult = makePrediction(booster, dMatrix);
            } catch (Exception e) {
                log.warn("Converting array to DMatrix failed!", e);
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
    public float[][] makePrediction(Booster booster, DMatrix dMatrix) throws Exception {
        return booster.predict(dMatrix);
    }


    /**
     * Extracts feature values from an entity registry in provided order.
     *
     * @param entitySnapshot snapshot of the entity registry
     * @param featuresToExtract ordered list of required feature IDs
     * @return array of extracted feature values from the entity registry
     *
     */
    private float[] extractValues(Map<String, Response> entitySnapshot, List<String> featuresToExtract) {
        float[] result = new float[featuresToExtract.size()];
        int index = 0;

        for (String feature : featuresToExtract) {
            Response response = entitySnapshot.get(feature);

            var state = response.getEvent().getData().getNew_state().getState();
            result[index++] = Float.parseFloat(state);
        }

        return result;
    }
}
