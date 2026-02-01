package lu.feschhaff.ritampc;

import lombok.extern.log4j.Log4j2;
import lu.feschhaff.ritampc.services.CsvFeatureService;
import lu.feschhaff.ritampc.services.GradientBoostingService;
import lu.feschhaff.ritampc.services.StateStoreService;
import ml.dmlc.xgboost4j.java.Booster;
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
    private final GradientBoostingService gradientBoostingService;

    public DataManager(GradientBoostingService gradientBoostingService) {
        this.gradientBoostingService = gradientBoostingService;
    }

    public static void main(String[] args)  {
        try {
            DataManager.trainModelBasedOnFolderData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void trainModelBasedOnFolderData() throws IOException {
        String directory = "C:/Users/WelJo/Desktop/TrainingDataFolder";
        Set<Path> paths = DataManager.listFilesUsingDirectoryStream(directory);

        Map<String, List<Float>> featuresByFile = paths.stream()
                .collect(Collectors.toMap(
                        DataManager::getEntityId,
                        CsvFeatureService::readFeatureColumn
                ));

        List<FeatureSubSet> subSets = DataManager.getSubSets(featuresByFile, 5);

        ArrayList<Booster> boosters = new ArrayList<>();
        for (FeatureSubSet subSet : subSets) {
            try {
                DMatrix dMatrix = GradientBoostingService.toDMatrix(subSet);
                Booster booster = GradientBoostingService.trainAndGetModel(dMatrix, null);

                String featureNames = subSet.getFeatures().stream().map(FeaturePoint::getEntity_id).collect(Collectors.joining("__"));
                String modelName = featureNames + "__" + subSet.getLabel().getEntity_id();

                booster.saveModel("C:/Users/WelJo/IdeaProjects/RitaMPC/src/main/resources/" + modelName + ".json");


            } catch (XGBoostError error) {
                throw new RuntimeException(error);
            }
        }
    }



    private static List<FeatureSubSet> getSubSets(Map<String, List<Float>> featuresByFile, int offset) {
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
        String filename = path.getFileName().toString();
        int lastDot = filename.lastIndexOf('.');

        return (lastDot == -1) ? filename : filename.substring(0, lastDot);
    }

    // https://www.baeldung.com/java-list-directory-files
    public static Set<Path> listFilesUsingDirectoryStream(String dir) throws IOException {
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




