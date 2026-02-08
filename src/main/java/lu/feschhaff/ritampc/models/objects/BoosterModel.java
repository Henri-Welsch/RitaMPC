package lu.feschhaff.ritampc.models.objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ml.dmlc.xgboost4j.java.Booster;

import java.util.HashSet;
import java.util.List;

/**
 * @author Henri-Welsch
 * @sources {
 *     <a href="https://www.baeldung.com/java-record-keyword">Record</a>
 * }
 */

@Getter @AllArgsConstructor
public class BoosterModel {
        private ModelMetaData modelMetaData;
        private Booster booster;

        public boolean containsAll(List<String> features) {
            return new HashSet<>(modelMetaData.getUsedFeatures()).containsAll(features);
        }
}
