package lu.feschhaff.ritampc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
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
