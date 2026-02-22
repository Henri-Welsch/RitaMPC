package lu.feschhaff.ritampc.ModelPredictiveController.services;

import org.springframework.stereotype.Service;

/**
 * @author Henri-Welsch
 * @sources {}
 */
@Service
public class MpcService {
    public void executeControlLoop() {

        // 1. Read system state (DB, sensors, API, etc.)
        // 2. Build prediction model
        // 3. Solve optimization problem
        // 4. Apply first control input
        // 5. Store results / log
    }
}
