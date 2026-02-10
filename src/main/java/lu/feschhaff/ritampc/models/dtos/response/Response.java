package lu.feschhaff.ritampc.models.dtos.response;

import lombok.Getter;
import lombok.Setter;


/**
 * @author Joé Welsch
 * @references {
 *     <a href="https://developers.home-assistant.io/docs/api/websocket/#subscribe-to-events">Response Structure</a>
 * }
 */
@Getter @Setter
public final class Response {
    private String type;
    private String ha_version;
    private String message;
    private Event event;
    private int id;
}
