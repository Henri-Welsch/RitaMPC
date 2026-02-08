package lu.feschhaff.ritampc.models.objects;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Henri-Welsch
 * @sources {
 *     <a href="https://www.baeldung.com/java-record-keyword">Record</a>
 * }
 */

@Getter @Setter
public class BoosterSubset {
    private GeneralMetaData generalMetaData;
    private List<BoosterModel> BoosterModels = new ArrayList<>();

    public BoosterSubset(GeneralMetaData generalMetaData) {
        this.generalMetaData = generalMetaData;
    }
}