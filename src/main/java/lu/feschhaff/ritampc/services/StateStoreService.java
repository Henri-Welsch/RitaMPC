package lu.feschhaff.ritampc.services;

import lombok.Getter;
import lombok.Setter;
import lu.feschhaff.ritampc.models.dtos.response.Response;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component @Getter @Setter
public class StateStoreService {
    private final Map<String, Response> stateStore = new ConcurrentHashMap<>();
}
