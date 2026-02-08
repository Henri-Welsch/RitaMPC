package lu.feschhaff.ritampc.models.objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ml.dmlc.xgboost4j.java.Booster;

import java.util.Set;

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

        public boolean containsAll(Set<String> features) {
            return modelMetaData.getUsedFeatures().containsAll(features);
        }
}
