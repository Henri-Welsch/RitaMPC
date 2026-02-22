package lu.feschhaff.ritampc.CommonTools;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Log4j2
public class CsvFeatureService {
    public static final String COMMA_DELIMITER = ",";
    public static final String TARGET_VALUE = "_value";

    public static void main(String[] args)  {
        String stringPath = "C:/Users/WelJo/Desktop/Influx Data Refrences/Data/awair_element/awair_element_82522_score.csv";

        Path path = Paths.get(stringPath);
        List<Float> Floats = CsvFeatureService.readUsingBufferReader(path);
    }

    public static ArrayList<Float> readUsingBufferReader(Path csvFilePath)  {
        String csvFilePathAsString = csvFilePath.toString();

        ArrayList<Float> targetValueList = new ArrayList<>();

        try (FileReader fileReader = new FileReader(csvFilePathAsString)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                int targetValueIndex = -1; String line;

                // Influx adds metadata (as #) to csv files, we need to
                // skip this metadata otherwise we cant read the file
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.startsWith("#")) { break; }
                }

                if (line == null || line.trim().isEmpty()) {
                    log.warn("Nothing to read, csv file has no entries!");
                    return targetValueList;
                }

                // Find position of TARGET_VALUE and save it
                String[] header = line.split(COMMA_DELIMITER);
                for (int i = 0; i < header.length; i++) {
                     if (header[i].equals(TARGET_VALUE)) {
                        targetValueIndex = i; break;
                    }
                }

                // Catch the case that the TARGET_VALUE is missing
                if (targetValueIndex == -1) {
                    log.error("Target value {} not found in {}", TARGET_VALUE, header);
                    return targetValueList;
                }

                // Go over the rest and save only the TARGET_VALUE
                while ((line = bufferedReader.readLine()) != null && !line.trim().isEmpty()) {
                    try {
                        String targetValue = line.split(COMMA_DELIMITER)[targetValueIndex];

                        if (targetValue.trim().isEmpty()) {
                            log.debug("Target value is missing!");
                            targetValueList.add(null);
                        } else {
                            Float v = Float.parseFloat(targetValue);
                            targetValueList.add(v);
                        }
                    } catch (Exception e) {
                        log.error("Malformatted csv file{}", line, e);
                        return targetValueList;
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to read {}", csvFilePath, e);
        }

        return targetValueList;
    }
}
