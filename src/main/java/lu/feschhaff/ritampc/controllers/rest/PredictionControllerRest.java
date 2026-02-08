package lu.feschhaff.ritampc.controllers.rest;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;
import lu.feschhaff.ritampc.services.PredictionService;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Henri-Welsch
 * @sources {}
 */

@Log4j2
@RestController
@RequestMapping("/rita")
public class PredictionControllerRest {

    private final PredictionService predictionService;

    public PredictionControllerRest(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/predict")
    @Operation(
            summary = "TODO",
            description = "TODO"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "TODO"),
            @ApiResponse(responseCode = "500", description = "TODO")
    })
    public float[][] trainModel(
            @RequestParam("boosterAsJson") MultipartFile boosterAsJson,
            @RequestParam("dMatrixAsCSR") MultipartFile dMatrixAsCSR
    ) throws Exception {
        Booster booster = XGBoost.loadModel(String.valueOf(boosterAsJson));
        DMatrix dMatrix = new DMatrix(String.valueOf(dMatrixAsCSR));

        return predictionService.makePrediction(booster, dMatrix);
    }
}
