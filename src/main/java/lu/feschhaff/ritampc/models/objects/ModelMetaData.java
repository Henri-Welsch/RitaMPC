package lu.feschhaff.ritampc.models.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author Henri-Welsch
 * @sources {}
 */

@AllArgsConstructor @Getter
public class ModelMetaData {
    private final String target;
    private final List<String> usedFeatures;
}
