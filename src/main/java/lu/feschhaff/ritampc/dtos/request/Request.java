package lu.feschhaff.ritampc.dtos.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {
    private Integer id;
    private String type;
    private Features Features;
    private String access_token;
    private String event_type;
    private Object service_data;
}
