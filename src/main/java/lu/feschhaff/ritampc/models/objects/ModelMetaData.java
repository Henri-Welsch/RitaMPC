package lu.feschhaff.ritampc.models.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * @author Henri-Welsch
 * @sources {}
 */

@AllArgsConstructor @Getter
public class ModelMetaData {
    private final String target;
    private final Set<String> usedFeatures;
}
