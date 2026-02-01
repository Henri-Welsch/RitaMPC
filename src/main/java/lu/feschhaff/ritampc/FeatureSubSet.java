package lu.feschhaff.ritampc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @AllArgsConstructor
public class FeatureSubSet {
    private FeaturePoint label;
    private List<FeaturePoint> features;
}
