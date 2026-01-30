package lu.feschhaff.ritampc.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Attributes {
    private String state_class;
    private String unit_of_measurement;
    private String device_class;
    private String friendly_name;
}
