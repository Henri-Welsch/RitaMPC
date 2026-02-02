package lu.feschhaff.ritampc.services;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Service
public class CsvFeatureService {
    static String[] HEADERS = {"","result","table","_start","_stop","_time","_value","_field","_measurement","domain","entity_id"};
    public static final String COMMA_DELIMITER = ",";
    public static final String TARGET_VALUE = "_value";

    public static void main(String[] args)  {
        String stringPath = "C:/Users/WelJo/Desktop/Influx Data Refrences/Data/awair_element/awair_element_82522_score.csv";

        List<Float> Floats = CsvFeatureService.readUsingBufferReader(stringPath);
        // Path path = Paths.get(stringPath);
        // CsvFeatureService.readFeatureColumn(path);
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
                try {
                    float value = Float.parseFloat(csvRecord.get("_value"));
                    featureArray.add(value);
                } catch (NumberFormatException e) {
                    log.info(e.getMessage());
                }
            }

            return featureArray;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + filePath, e);
        }
    }

    public static List<Float> readUsingBufferReader(String csvFilePath)  {
        List<Float> targetValueList = new ArrayList<>();

        try (FileReader fileReader = new FileReader(csvFilePath)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                int targetValueIndex = -1; String line;

                // Influx adds metadata (as #) to csv files, we need to
                // skip this metadata otherwise we cant read the file
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.startsWith("#")) { break; }
                }

                if (line == null) {
                    log.warn("Nothing to read, csv file has no entries!");
                    return null;
                }

                // Find position of TARGET_VALUE and save it
                String[] header = line.split(COMMA_DELIMITER);
                for (int i = 0; i < header.length; i++) {
                    if (header[i].equals(TARGET_VALUE)) {
                        targetValueIndex = i;
                    }
                }

                // Catch the case that the TARGET_VALUE is missing
                if (targetValueIndex == -1) {
                    log.error("Target value {} not found in {}", TARGET_VALUE, header);
                    return null;
                }

                // Go over the rest and save only the TARGET_VALUE
                while ((line = bufferedReader.readLine()) != null && !line.trim().isEmpty()) {
                    try {
                        String targetValue = line.split(COMMA_DELIMITER)[targetValueIndex];

                        if (targetValue.trim().isEmpty()) {
                            log.debug("Target value is missing!");
                        }

                        float v = Float.parseFloat(targetValue);
                        targetValueList.add(v);
                    } catch (Exception e) {
                        log.error("Malformatted csv file{}", line, e);
                        return null;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to read {}", csvFilePath, e);
        }

        return targetValueList;
    }
}
