package lu.feschhaff.ritampc.HomeAssistant.Models.DTOs.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class Context {
    private String id;
    private String parent_id;
    private String user_id;
}
