package lu.feschhaff.ritampc.HomeAssistant.Models.DTOs.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class Data {
    private String entity_id;
    private State old_state;
    private State new_state;
}
