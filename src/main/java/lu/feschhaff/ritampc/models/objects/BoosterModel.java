package lu.feschhaff.ritampc.models.objects;
import ml.dmlc.xgboost4j.java.Booster;

import java.util.Set;

/**
 * @author Henri-Welsch
 * @sources {
 *     <a href="https://www.baeldung.com/java-record-keyword">Record</a>
 * }
 */

public record BoosterModel(
        Set<String> possibleFeatures,
        Booster booster
) { }
