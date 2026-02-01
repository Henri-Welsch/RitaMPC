package lu.feschhaff.ritampc.services;

import lombok.Getter;
import lombok.Setter;
import lu.feschhaff.ritampc.dtos.response.Response;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service @Getter @Setter
public class StateStoreService {
    private final Map<String, Response> stateStore = new ConcurrentHashMap<>();
}
