package lu.feschhaff.ritampc.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Response {
    private String type;
    private String ha_version;
    private String message;
    private Event event;
    private int id;
}
