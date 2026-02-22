package lu.feschhaff.ritampc.HomeAssistant.Registries;

import lombok.Getter;
import lombok.Setter;
import lu.feschhaff.ritampc.HomeAssistant.Models.DTOs.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component @Getter @Setter
public class EntityRegistry {
    private final Map<String, Response> entityRegistry = new ConcurrentHashMap<>();

    /**
     * Provides a safe, read-only copy, snapshot of the current entity registry.
     *
     * @return a copy of the current entity registry
     */
    public Map<String, Response> getEntityRegistrySnapshot() {
        return new HashMap<>(entityRegistry);
    }

    /**
     * Checks whether an entity is currently present in the entity registry.
     *
     * @param feature the entity identifier to check
     * @return {@code true} if the entity exists in the entity registry, {@code false} otherwise
     */
    public boolean isFeaturePresent(String feature) {
        return entityRegistry.containsKey(feature);
    }

    /**
     * Returns the subset of the given features that are currently present
     * in the state store.
     *
     * @param featuresToCheck features to check
     * @return features that exist in the state store
     */
    public List<String> getPresentFeatures(List<String> featuresToCheck) {
        return featuresToCheck.stream()
                .filter(this::isFeaturePresent)
                .toList();

    }
}
