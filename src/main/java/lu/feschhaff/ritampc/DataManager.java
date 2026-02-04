package lu.feschhaff.ritampc;

import lombok.extern.log4j.Log4j2;
import lu.feschhaff.ritampc.models.objects.FeaturePoint;
import lu.feschhaff.ritampc.models.objects.FeatureSubSet;
import lu.feschhaff.ritampc.services.CsvFeatureService;
import lu.feschhaff.ritampc.services.TrainingService;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class DataManager {
    @Value("${booster.model.location}")
    private static String boosterModelLocation;

    public static void main(String[] args)  {
        try {
            DataManager.trainModelBasedOnFolderData();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static void trainModelBasedOnFolderData() throws Exception {
        String directory = "C:/Users/WelJo/Desktop/Influx Data Refrences/Data";
        Set<Path> paths = DataManager.listFilesUsingDirectoryStream(directory);

        // Read and map the feature values of each csv-file to their identifier
        Map<String, List<Float>> featuresByFile = paths.stream()
                .collect(Collectors.toMap(
                        DataManager::getEntityId,
                        CsvFeatureService::readUsingBufferReader
                ));

        // Create the cross product of ever data point so that every data point
        // becomes the target once while all the other datapoint become the features
        for (FeatureSubSet featureSubSet : DataManager.getSubSets(featuresByFile, 5)) {
            // Create DMatrix and then train a Booster based on the Matrix.
            DMatrix trainingDataMatrix = TrainingService.toDMatrix(featureSubSet);
            Booster trainedModel = TrainingService.trainAndGetModel(trainingDataMatrix, null);

            // Prepare booster filename in the format feature1__featureN__target.
            String combinedFeatureIds = featureSubSet.getFeatures().stream().map(FeaturePoint::getEntity_id).collect(Collectors.joining("__"));
            String targetFeatureId = featureSubSet.getLabel().getEntity_id();
            String modelIdentifier = combinedFeatureIds + "__" + targetFeatureId;

            // Create full path (direction + filename) and save model on disc.
            String modelFilePath = boosterModelLocation + modelIdentifier + ".json";
            trainedModel.saveModel(modelFilePath);
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
                    continue;
                }
                fileSet.add(path);
            }
        }
        return fileSet;
    }
}
