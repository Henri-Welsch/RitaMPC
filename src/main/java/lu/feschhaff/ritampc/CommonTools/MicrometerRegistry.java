package lu.feschhaff.ritampc.CommonTools;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Henri-Welsch
 * @sources {
 *     <a href="https://docs.micrometer.io/micrometer/reference/">Micrometers</a>
 * }
 */
@Component @Getter @Log4j2
public class MicrometerRegistry {

    private final MeterRegistry meterRegistry;
    private final Map<String, AtomicReference<Float>> micrometerRegistry = new ConcurrentHashMap<>();

    public MicrometerRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void updateGauge(String feature, String horizon, float newValue) {
        String identifier = feature + "_" + horizon;

        AtomicReference<Float> value = micrometerRegistry.computeIfAbsent(identifier, f -> {
            AtomicReference<Float> floatAtomicReference = new AtomicReference<>((float) 0.0);

            Gauge.builder(feature, floatAtomicReference, AtomicReference<Float>::get)
                    .description("Metric for: " + feature)
                    .tag("horizon", horizon)
                    .register(meterRegistry);

            return floatAtomicReference;
        });

        value.set(newValue);
    }
}
