package lu.feschhaff.ritampc.restControllers;

import lombok.extern.log4j.Log4j2;
import lu.feschhaff.ritampc.DataManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Log4j2
@RequestMapping("/rita")
public class GradientBoostingRestController {

    @GetMapping("/train")
    public String trainModel() throws IOException {
        log.info("Train model based on folder has been called! ");
        DataManager.trainModelBasedOnFolderData();

        return "Model training started";
    }
}
