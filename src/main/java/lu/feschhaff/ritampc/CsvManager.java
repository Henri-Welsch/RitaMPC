package lu.feschhaff.ritampc;

import lombok.extern.log4j.Log4j2;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Log4j2
public class CsvManager {
    String[] HEADERS = {"","result","table","_start","_stop","_time","_value","_field","_measurement","domain","entity_id"};

    public static void main(String[] args) throws XGBoostError {
        CsvManager csvManager = new CsvManager();

        String fileName = "C:/Users/WelJo/Desktop/Influx Data Refrences/Data/awair_element/test.csv";
        // ArrayList<Float> features = csvManager.readCsvAndExtractFeatures(fileName);
        // DMatrix dMatrix = csvManager.convertToDmMatrix(features);

        // DMatrix exampleTrainingData = csvManager.getExampleTrainingData();
        // DMatrix exampleEvaluationData = csvManager.getExampleEvaluationData();

        // Booster booster = csvManager.trainAndGetModel(exampleTrainingData, exampleEvaluationData);
    }

    public Map.Entry<String, ArrayList<Float>> readCsvAndExtractFeatures(String fileName) {
        Map.Entry<String, ArrayList<Float>> features;

        try (Reader reader = new FileReader(fileName)) {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .build().withFirstRecordAsHeader();

            CSVParser csvRecords = csvFormat.parse(reader);
            features = extractFeature(csvRecords);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return features;
    }

    public Map.Entry<String, ArrayList<Float>> extractFeature(Iterable<CSVRecord> csvRecords) {
        ArrayList<Float> values = new ArrayList<>();
        String entity_id = "";


        log.info("Starting to read features");
        for (CSVRecord record : csvRecords) {
            if (entity_id.isBlank()) {
                entity_id = record.get("entity_id");
            }
            values.add(Float.parseFloat(record.get("_value")));
        }

        log.info("Finished reading -- found {} features", values.size());
        return new AbstractMap.SimpleEntry<>(entity_id, values);
    }

    public float[] convertArrayListToArray(ArrayList<Float> arrayList) {
        float[] featuresArray = new float[arrayList.size()];

        for (int i = 0; i < arrayList.size(); i++) {
            featuresArray[i] = arrayList.get(i); // auto-unboxing Float -> float
        }

        return featuresArray;
    }

    public DMatrix convertToDmMatrix(ArrayList<Float> features) {
        float[] featuresArray = convertArrayListToArray(features);

        int rows = features.size();
        int cols = 1;
        float missingValue = 0.0f;
        DMatrix dMatrix;

        try {
            dMatrix = new DMatrix(featuresArray, rows, cols, missingValue);
        } catch (XGBoostError e) {
            throw new RuntimeException(e);
        }

        return dMatrix;
    }

    public Booster trainAndGetModel(DMatrix trainingData, DMatrix evaluationData) throws XGBoostError {
        Map<String, Object> params = new HashMap<String, Object>() {
            {
                put("eta", 1.0);
                put("max_depth", 2);
                put("objective", "binary:logistic");
                put("eval_metric", "logloss");
            }
        };

        // Optional: watch list lets you track performance
        Map<String, DMatrix> watches = new HashMap<>();
        watches.put("train", trainingData);
        watches.put("eval", evaluationData);

        int numRounds = 10;  // number of boosting rounds
        return XGBoost.train(trainingData, params, numRounds, watches, null, null);
    }

    public DMatrix getExampleTrainingData() throws XGBoostError {
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

    // Evaluation data (2 new samples)
    public DMatrix getExampleEvaluationData() throws XGBoostError {
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

    public ArrayList<Float> readFeatureColumn(Path filePath) {
        try (Reader reader = Files.newBufferedReader(filePath)) {
            CSVParser csvRecords = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADERS)
                    .build().withFirstRecordAsHeader()
                    .parse(reader);

            // Determine row count for the float array
            ArrayList<Float> featureArray = new ArrayList<>();

            // Fill feature array with read values
            for (CSVRecord csvRecord : csvRecords) {
                float value = Float.parseFloat(csvRecord.get("_value"));
                featureArray.add(value);
            }

            return featureArray;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + filePath, e);
        }
    }
}
