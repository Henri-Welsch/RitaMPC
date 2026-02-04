package lu.feschhaff.ritampc.models.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @AllArgsConstructor
public class FeatureSubSet {
    private FeaturePoint label;
    private List<FeaturePoint> features;
}
