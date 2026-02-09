package lu.feschhaff.ritampc.Registries;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import lu.feschhaff.ritampc.models.objects.BoosterModel;
import lu.feschhaff.ritampc.models.objects.BoosterSubset;
import lu.feschhaff.ritampc.models.objects.GeneralMetaData;
import lu.feschhaff.ritampc.models.objects.ModelMetaData;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    private final Map<String, BoosterSubset> boosterRegistry = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EntityRegistry entityRegistry;
    private final ConfigurableApplicationContext configurableApplicationContext;

    @Value("${booster.model.location}")
    private String boosterModelRootPath = "C:/Users/WelJo/Desktop/testFolder";

    public BoosterRegistry(
            EntityRegistry entityRegistry,
            ConfigurableApplicationContext configurableApplicationContext
    ) {
        this.entityRegistry = entityRegistry;
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
            BoosterSubset boosterSubset = loadBoosterSubset(targetFolder);
            String targetKey = targetFolder.getFileName().toString();
            boosterRegistry.put(targetKey, boosterSubset);
        }

        log.info("Booster registry has been initialized");
    }

    private BoosterSubset loadBoosterSubset(Path targetFolder) throws IOException {
        Path metaPath = targetFolder.resolve("meta.json");

        GeneralMetaData generalMetaData = objectMapper.readValue(metaPath, GeneralMetaData.class);

        BoosterSubset boosterSubset = new BoosterSubset(generalMetaData);
        Set<Path> strategyFolders = getSubsetFolders(targetFolder.toString());

        for (Path strategyFolder : strategyFolders) {
            if (!Files.isDirectory(strategyFolder)) continue;

            try {
                BoosterModel boosterModel = loadBoosterModel(strategyFolder);
                boosterSubset.getBoosterModels().add(boosterModel);
            } catch (XGBoostError e) {
                log.error("Unable to load booster from folder: {}", strategyFolder, e);
            }
        }

        return boosterSubset;
    }

    private BoosterModel loadBoosterModel(Path boosterFolder) throws XGBoostError {
        Path boosterMetaPath = boosterFolder.resolve("meta.json");
        ModelMetaData modelMetaData = objectMapper.readValue(boosterMetaPath, ModelMetaData.class);

        Path boosterModelPath = boosterFolder.resolve("model.json");
        Booster booster = XGBoost.loadModel(boosterModelPath.toString());

        return new BoosterModel(modelMetaData, booster);
    }

    public static Set<Path> getSubsetFolders(String dir) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream.collect(Collectors.toSet());
        }
    }

    public Optional<BoosterModel> findBestBooster(String target) {
        BoosterSubset boosterSubset = this.boosterRegistry.get(target);
        List<String> featuresToCheck = boosterSubset.getGeneralMetaData().getAvailableFeatures();
        List<String> availableFeatures = entityRegistry.getPresentFeatures(featuresToCheck);

        return findBooster(target, availableFeatures);
    }

    private Optional<BoosterModel> findBooster(String target, List<String> features) {
        return this.boosterRegistry.get(target).getBoosterModels().stream()
                .filter(boosterModel -> boosterModel.containsAll(features))
                .findFirst();
    }
}
