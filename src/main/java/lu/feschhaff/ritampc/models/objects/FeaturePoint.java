package lu.feschhaff.ritampc.models.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter @Setter @AllArgsConstructor
public class FeaturePoint {
    private String entity_id;
    private List<Float> features;

    public FeaturePoint(String entity_id, Float feature) {
        this.entity_id = entity_id;
        this.features = Collections.singletonList(feature);
    }
}
