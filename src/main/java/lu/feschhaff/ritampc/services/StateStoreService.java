package lu.feschhaff.ritampc.services;

import lombok.Getter;
import lombok.Setter;
import lu.feschhaff.ritampc.models.dtos.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component @Getter @Setter
public class StateStoreService {
    private final Map<String, Response> stateStore = new ConcurrentHashMap<>();

    /**
     * Provides a safe, read-only copy, snapshot of the current state store.
     *
     * @return a copy of the current state store
     */
    public Map<String, Response> getStateStoreSnapshot() {
        return new HashMap<>(stateStore);
    }

    /**
     * Checks whether a feature is currently present in the state store.
     *
     * @param feature the feature identifier to check
     * @return {@code true} if the feature exists in the state store, {@code false} otherwise
     */
    public boolean isFeaturePresent(String feature) {
        return stateStore.containsKey(feature);
    }

    /**
     * Returns the subset of the given features that are currently present
     * in the state store.
     *
     * @param featuresToCheck features to check
     * @return features that exist in the state store
     */
    public Set<String> getPresentFeatures(Set<String> featuresToCheck) {
        return featuresToCheck.stream()
                .filter(this::isFeaturePresent)
                .collect(Collectors.toSet());
    }
}
