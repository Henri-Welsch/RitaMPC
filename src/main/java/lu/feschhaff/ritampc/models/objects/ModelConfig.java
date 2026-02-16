package lu.feschhaff.ritampc.models.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author Henri-Welsch
 * @sources {}
 */
@AllArgsConstructor @Getter
public class ModelConfig {
    private String target;
    private List<String> features;
    private int stepsAhead;
    private double dropoutProb;
    private double validationFraction;
    private ModelMetrics modelMetrics;
}