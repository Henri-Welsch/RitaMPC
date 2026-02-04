package lu.feschhaff.ritampc.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;
import lu.feschhaff.ritampc.DataManager;
import lu.feschhaff.ritampc.services.TrainingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping("/rita")
public class TrainingControllerRest {
    private final TrainingService trainingService;

    public TrainingControllerRest(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping("/train")
    @Operation(
            summary = "Train models based on folder csv files",
            description =
                    "This will call the XGBoosting algorithm based on the folder data" +
                    "the server will generate (n * n * m) models, and save them under XXX"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully trained models based on folder data!"),
            @ApiResponse(responseCode = "500", description = "Internal server error, something went wrong! ")
    })
    public String trainModel() throws Exception {
        log.info("Trigger and train models based on folder has been started!");
        DataManager.trainModelBasedOnFolderData();
        log.info("Successfully trained models based on folder data!");

        return "Successfully trained models based on folder data!";
    }
}
