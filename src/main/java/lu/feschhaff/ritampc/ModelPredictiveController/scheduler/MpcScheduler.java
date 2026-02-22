package lu.feschhaff.ritampc.ModelPredictiveController.scheduler;

import lu.feschhaff.ritampc.ModelPredictiveController.services.MpcService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author Henri-Welsch
 * @sources {}
 *
 * Application (main)
 *    ↓
 * Scheduler (timing)
 *    ↓
 * MpcService
 *    ↓
 * OptimizationSolver
 *    ↓
 * DataService (state access)
 */
@Service
public class MpcScheduler {

    private final MpcService mpcService;

    public MpcScheduler(MpcService mpcService) {
        this.mpcService = mpcService;
    }

    @Scheduled(fixedRate = 600000) // 10 minutes in milliseconds
    public void runMpc() {
        mpcService.executeControlLoop();
    }
}
