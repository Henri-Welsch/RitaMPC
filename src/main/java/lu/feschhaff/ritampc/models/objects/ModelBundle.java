package lu.feschhaff.ritampc.models.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ml.dmlc.xgboost4j.java.Booster;

/**
 * @author Henri-Welsch
 * @sources {}
 */
@AllArgsConstructor @Getter
public class ModelBundle {
    private ModelConfig modelConfig;
    private Booster booster;
}
