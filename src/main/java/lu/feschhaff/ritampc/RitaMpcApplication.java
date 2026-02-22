package lu.feschhaff.ritampc;

import lu.feschhaff.ritampc.ModelPredictiveController.config.GoalCofiguration;
import lu.feschhaff.ritampc.ModelPredictiveController.MpcController;
import lu.feschhaff.ritampc.ModelPredictiveController.optimizers.RandomSearchStrategy;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RitaMpcApplication {

    public static void main(String[] args) {
// SpringApplication.run(RitaMpcApplication.class, args);

        GoalCofiguration goalCofiguration = new GoalCofiguration();
        RandomSearchStrategy optimizer = new RandomSearchStrategy();
        MpcController controller = new MpcController(optimizer, goalCofiguration);

        double currentTemp = 20.0; // TODO: replace with real sensor value / state
        double heaterValue = controller.computeNextAction(currentTemp);

        System.out.println("Heater setting: " + heaterValue);
    }
}
