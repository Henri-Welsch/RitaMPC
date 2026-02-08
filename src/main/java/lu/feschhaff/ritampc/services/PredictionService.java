package lu.feschhaff.ritampc.services;

import lombok.extern.log4j.Log4j2;
import lu.feschhaff.ritampc.Registries.BoosterRegistry;
import lu.feschhaff.ritampc.Registries.EntityRegistry;
import lu.feschhaff.ritampc.models.dtos.response.Response;
import lu.feschhaff.ritampc.models.objects.BoosterModel;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Henri-Welsch
 * @sources {
 *     <a href="https://xgboost.readthedocs.io/en/latest/jvm/java_intro.html">...</a>
 *     <a href="https://www.geeksforgeeks.org/springboot/spring-boot-annotations/">...</a>
 * }
 */

@Service @Log4j2
public class PredictionService {
    private final EntityRegistry entityRegistry;
    private final BoosterRegistry boosterRegistry;
    private final TrainingService trainingService;

    Set<String> targets = new HashSet<>(Arrays.asList(
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

    @Scheduled(initialDelay = 10000, fixedRate = 10000)
    private void predictionSchedule() {
        Map<String, Response> entityRegistrySnapshot = entityRegistry.getEntityRegistrySnapshot();

        for (String target : targets) {
            Optional<BoosterModel> bestBooster = boosterRegistry.findBestBooster(target);

            if (bestBooster.isEmpty()) {
                log.warn("Booster for [targets: {}] not found!",  target);
                continue;
            }

            BoosterModel boosterModel = bestBooster.get();
            List<String> usedFeatures = boosterModel.getModelMetaData().getUsedFeatures();

            // TODO: This should be in a loop, stream is bad
            List<Float> collect = usedFeatures.stream()
                    .map(entityRegistrySnapshot::get)
                    .filter(Objects::nonNull)
                    .map(x -> x.getEvent().getData().getNew_state().getState())
                    .filter(Objects::nonNull)
                    .map(Float::parseFloat)
                    .toList();

            float[] array =  new float[collect.size()];
            for (int i = 0; i < collect.size(); i++) {
                array[i] = collect.get(i);
            }

            try {
                Booster booster = boosterModel.getBooster();
                DMatrix dMatrix = trainingService.convertFloatToDMatrix(array);
                float[][] floats = makePrediction(booster, dMatrix);
            } catch (Exception e) {
                log.warn("Converting array to DMatrix failed!", e);
            }
        }

        int stateStoreServiceSize = entityRegistry.getEntityRegistry().size();
        log.info("State Store contains {} elements!", stateStoreServiceSize);
    }

    public float[][] makePrediction(Booster booster, DMatrix dMatrix) throws Exception {
        return booster.predict(dMatrix);
    }
}
