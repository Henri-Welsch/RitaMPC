package lu.feschhaff.ritampc.controllers;

import lombok.extern.log4j.Log4j2;
import lu.feschhaff.ritampc.DataManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@Log4j2
@RequestMapping("/rita")
public class GradientBoostingController {

    @GetMapping
    @RequestMapping("/train")
    public void trainModel() throws IOException {
        log.info("Train model based on folder has been called! ");
        DataManager.trainModelBasedOnFolderData();
    }
}
