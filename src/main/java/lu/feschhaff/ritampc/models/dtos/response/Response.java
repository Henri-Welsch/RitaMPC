package lu.feschhaff.ritampc.models.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class Response {
    private String type;
    private String ha_version;
    private String message;
    private Event event;
    private int id;
}
