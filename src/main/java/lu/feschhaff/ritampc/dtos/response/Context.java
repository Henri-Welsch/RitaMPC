package lu.feschhaff.ritampc.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Context {
    private String id;
    private String parent_id;
    private String user_id;
}
