package lu.feschhaff.ritampc.PredictionModel.Registries;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import lu.feschhaff.ritampc.PredictionModel.Models.Objects.ModelBundle;
import lu.feschhaff.ritampc.PredictionModel.Models.Objects.ModelConfig;
import lu.feschhaff.ritampc.PredictionModel.Models.Objects.ModelFamily;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Henri-Welsch
 * @sources {
 *     <a href="https://www.geeksforgeeks.org/system-design/registry-pattern/">Registry Pattern</a>
 *     <a href="https://medium.com/@sarathesid/strategy-pattern-with-a-registry-pattern-using-springboot-0aa46b1743e9">Registry in Spring Boot</a>
 * }
 */

@Component @Log4j2
public class BoosterRegistry {

    private final Map<String, ModelFamily> boosterRegistry = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConfigurableApplicationContext configurableApplicationContext;

    @Value("${booster.model.location}")
    private String boosterModelRootPath = "C:/Users/WelJo/Desktop/testFolder";

    public BoosterRegistry(ConfigurableApplicationContext configurableApplicationContext) {
        this.configurableApplicationContext = configurableApplicationContext;
    }

    @PostConstruct
    public void initializeBoosterStore() {
        String message = "Trying to initialize booster registry from [boosterModelLocation: {}]";
        log.info(message, boosterModelRootPath);

        try {
            clearAndRegisterBoostersFromFolder();
        } catch (IOException e) {
            log.error("Unable to initialize booster registry, shutting system down!", e);
            configurableApplicationContext.close();
        }
    }

    private void clearAndRegisterBoostersFromFolder() throws IOException {
        boosterRegistry.clear();

        Set<Path> targetFolders = getSubsetFolders(boosterModelRootPath);

        for (Path targetFolder : targetFolders) {
            ModelFamily modelFamily = getModelFamily(targetFolder);
            boosterRegistry.put(modelFamily.getTarget(), modelFamily);
        }

        log.info("Booster registry has been initialized");
    }

    private ModelFamily getModelFamily(Path targetFolder) throws IOException {
        Set<Path> strategyFolders = getSubsetFolders(targetFolder.toString()); // TODO
        ModelFamily modelFamily = new ModelFamily();

        for (Path strategyFolder : strategyFolders) {
            if (!Files.isDirectory(strategyFolder)) continue;

            try {
                ModelBundle modelBundle = getModelBundle(strategyFolder);
                String target = modelBundle.getModelConfig().getTarget();

                if (modelFamily.getTarget() == null) {
                    modelFamily.setTarget(target);
                }

                modelFamily.addNewBundle(modelBundle);
            } catch (XGBoostError e) {
                log.error("Unable to load booster from folder: {}", strategyFolder, e);
            }
        }

        return modelFamily;
    }

    private ModelBundle getModelBundle(Path targetFolder) throws XGBoostError {
        Path modelConfigPath = targetFolder.resolve("modelConfig.json");
        ModelConfig modelConfig = objectMapper.readValue(modelConfigPath.toFile(), ModelConfig.class);

        Path modelDefinitionPath = targetFolder.resolve("modelDefinition.json");
        Booster modelDefinition = XGBoost.loadModel(modelDefinitionPath.toString());

        return new  ModelBundle(modelConfig, modelDefinition);
    }

//    private BoosterModel loadBoosterModel(Path boosterFolder) throws XGBoostError {
//        Path boosterMetaPath = boosterFolder.resolve("meta.json");
//        ModelMetaData modelMetaData = objectMapper.readValue(boosterMetaPath, ModelMetaData.class);
//
//        Path boosterModelPath = boosterFolder.resolve("model.json");
//        Booster booster = XGBoost.loadModel(boosterModelPath.toString());
//
//        return new BoosterModel(modelMetaData, booster);
//    }

    private static Set<Path> getSubsetFolders(String dir) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream.collect(Collectors.toSet());
        }
    }

    public ModelBundle getModelBundle(String target, int stepsAhead) {
        ModelFamily modelFamily = boosterRegistry.get(target);
        if (modelFamily == null) return null;

        return modelFamily.getForStepsAhead(stepsAhead);
    }


//    public Optional<BoosterModel> findBestBooster(String target) {
//        BoosterSubset boosterSubset = this.boosterRegistry.get(target);
//        List<String> featuresToCheck = boosterSubset.getGeneralMetaData().getAvailableFeatures();
//        List<String> availableFeatures = entityRegistry.getPresentFeatures(featuresToCheck);
//
//        return findBooster(target, availableFeatures);
//    }

//    private Optional<BoosterModel> findBooster(String target, List<String> features) {
//        var registryEntry = boosterRegistry.get(target);
//        if (registryEntry == null || features.isEmpty()) {
//            return Optional.empty();
//        }
//
//        return registryEntry.getBoosterModels().stream()
//                .filter(model -> model.getModelMetaData()
//                        .getUsedFeatures()
//                        .equals(features))
//                .findFirst();
//    }
}
