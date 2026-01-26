package lu.feschhaff.ritampc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ml.dmlc.xgboost4j.java.XGBoostError;
import ml.dmlc.xgboost4j.java.DMatrix;


import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class DataManager {
    public static void main(String[] args) throws IOException {
        DataManager dataManager = new DataManager();
        CsvManager csvManager = new CsvManager();

        String directory = "C:/Users/WelJo/Desktop/TrainingDataFolder";
        Set<Path> paths = dataManager.listFilesUsingDirectoryStream(directory);

        Map<String, List<Float>> featuresByFile = paths.stream()
                .collect(Collectors.toMap(
                        DataManager::getEntityId,
                        csvManager::readFeatureColumn
                ));

        List<FeatureSubSet> subSets = dataManager.getSubSets(featuresByFile, 5);
    }

    private List<FeatureSubSet> getSubSets(Map<String, List<Float>> featuresByFile, int offset) {
        List<FeatureSubSet> featureSubSetList = new ArrayList<>();

        for (Map.Entry<String, List<Float>> label : featuresByFile.entrySet()) {
            List<FeaturePoint> list =  new ArrayList<>();

            for (Map.Entry<String, List<Float>> entries : featuresByFile.entrySet()) {
                if (entries.getKey().equals(label.getKey())) { continue; }

                String entry_id = entries.getKey();
                List<Float> featuresSubSet = entries.getValue().subList(0, entries.getValue().size() - offset);
                FeaturePoint featurePoint = new FeaturePoint(entry_id, featuresSubSet);

                list.add(featurePoint);
            }

            List<Float> labelSubset = label.getValue().subList(offset, label.getValue().size());
            FeaturePoint featurePointLabel = new FeaturePoint(label.getKey(), labelSubset);
            FeatureSubSet featureSubSet = new FeatureSubSet(featurePointLabel, list);

            featureSubSetList.add(featureSubSet);
        }

        return featureSubSetList;
    }

    public static String getEntityId(Path path) {
        return path.getFileName().toString().split("\\.")[0];
    }



    // https://www.baeldung.com/java-list-directory-files
    public Set<Path> listFilesUsingDirectoryStream(String dir) throws IOException {
        Set<Path> fileSet = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
            for (Path path : stream) {
                if (Files.isDirectory(path))  {
                    String message = "Directories ar not read, skipping: {}";
                    log.warn(message, path.toString());
                }
                fileSet.add(path);
            }
        }
        return fileSet;
    }

    public DMatrix createDMatrix(float[] features, float[] labels, String[] featureNames) throws XGBoostError {
        int rowLength = labels.length;
        int colLength = labels.length;
        float missingValue = 0.0f;

        DMatrix dMatrix = new DMatrix(features, rowLength, colLength, missingValue);
        dMatrix.setFeatureNames(featureNames);

        return dMatrix;
    }
}

@Getter @Setter @AllArgsConstructor
class FeatureSubSet {
    public FeaturePoint label;
    public List<FeaturePoint> features;
}

@Getter @Setter @AllArgsConstructor
class FeaturePoint {
    public String entity_id;
    public List<Float> features;
}




