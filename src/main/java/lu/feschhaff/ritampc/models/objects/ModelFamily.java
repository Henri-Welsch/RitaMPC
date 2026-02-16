package lu.feschhaff.ritampc.models.objects;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Henri-Welsch
 * @sources {}
 */
@Getter @Setter
public class ModelFamily {
    private String target; // TODO, should be simplified, this class is not needed in the map
    private Map<Integer, ModelBundle> modelsByStepsAhead =  new HashMap<Integer, ModelBundle>();

    public ModelBundle getForStepsAhead(int stepsAhead) {
        return modelsByStepsAhead.get(stepsAhead);
    }

    public void addNewBundle(ModelBundle modelBundle) {
        int stepsAhead = modelBundle.getModelConfig().getStepsAhead();

        modelsByStepsAhead.put(stepsAhead,  modelBundle);
    }
}
