package lu.feschhaff.ritampc.models.objects;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Henri-Welsch
 * @sources {
 *     <a href="https://www.baeldung.com/java-record-keyword">Record</a>
 * }
 */


@Getter @Setter
public class BoosterSubset {
    private Set<String> possibleFeatures;
    private List<BoosterModel> BoosterModel = new ArrayList<>();

    public BoosterSubset(Set<String> allPossibleFeatures) {
        this.possibleFeatures = allPossibleFeatures;
    }
}