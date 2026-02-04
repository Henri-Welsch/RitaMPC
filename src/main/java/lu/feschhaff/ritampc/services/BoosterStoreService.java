package lu.feschhaff.ritampc.services;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lu.feschhaff.ritampc.DataManager;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Henri-Welsch
 * @sources {
 *     <a href="https://xgboost.readthedocs.io/en/latest/jvm/java_intro.html">...</a>
 *     <a href="https://www.geeksforgeeks.org/springboot/spring-boot-annotations/">...</a>
 *     <a href="https://projectlombok.org/features/">...</a>
 * }
 */

@Service @Log4j2 @Getter
public class BoosterStoreService {

    private final Map<String, Booster> boosterStore = new ConcurrentHashMap<>();
    private final ConfigurableApplicationContext configurableApplicationContext;

    @Value("${booster.model.location}")
    private String boosterModelLocation;

    public BoosterStoreService(ConfigurableApplicationContext configurableApplicationContext) {
        this.configurableApplicationContext = configurableApplicationContext;
    }

    @PostConstruct
    public void initializeBoosterStore() {
        String message = "Trying to initialize booster store from [boosterModelLocation: {}]";
        log.info(message, boosterModelLocation);

        try {
            clearAndLoadBoostersFromFolder();
        } catch (IOException e) {
            log.error("Unable to initialize booster store, shutting system down!", e);
            configurableApplicationContext.close();
        }
    }

    private void clearAndLoadBoostersFromFolder() throws IOException {
        boosterStore.clear();

        Set<Path> paths = DataManager.listFilesUsingDirectoryStream(boosterModelLocation);
        for (Path path : paths) {
            String boosterModelPath = path.toString();

            try {
                Booster booster = XGBoost.loadModel(boosterModelPath);

                String boosterModelName = boosterModelPath.split(".json")[0];
                boosterStore.put(boosterModelName, booster);
            } catch (XGBoostError e) {
                String message = "Unable to load booster model [boosterModelPath: {}]";
                log.warn(message, boosterModelPath, e);
            }
        }

        String message = "Booster store successfully initialized from [boosterModelLocation: {}]";
        log.info(message, boosterModelLocation);
    }
}
