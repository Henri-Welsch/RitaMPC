package lu.feschhaff.ritampc.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class State {
    private String entity_id;
    private String state;
    private Attributes attributes;
    private String last_changed;
    private String last_reported;
    private String last_updated;
    private Context context;
}
