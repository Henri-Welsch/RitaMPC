package lu.feschhaff.ritampc.services;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Log4j2
@Service
public class CsvFeatureService {
    static String[] HEADERS = {"","result","table","_start","_stop","_time","_value","_field","_measurement","domain","entity_id"};

    public static void main(String[] args)  {
        String sting = "C:/Users/WelJo/Desktop/Influx Data Refrences/Data/awair_element/test.csv";
        Path path = Paths.get(sting);
        CsvFeatureService.readFeatureColumn(path);
    }

    public static ArrayList<Float> readFeatureColumn(Path filePath) {
        try (Reader reader = Files.newBufferedReader(filePath)) {
            CSVParser csvRecords = CSVFormat.DEFAULT.builder()
                    .setHeader(CsvFeatureService.HEADERS)
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
