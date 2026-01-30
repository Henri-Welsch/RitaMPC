package lu.feschhaff.ritampc.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Event {
    private String event_type;
    private Data data;
    private String origin;
    private String time_fired;
    private Context context;
}
