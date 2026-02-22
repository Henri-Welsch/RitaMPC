package lu.feschhaff.ritampc.HomeAssistant.Models.DTOs.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class Attributes {
    private String state_class;
    private String unit_of_measurement;
    private String device_class;
    private String friendly_name;
}
