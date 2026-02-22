package lu.feschhaff.ritampc.mpc.models;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * @author Henri-Welsch
 * @sources {}
 */
@Component @Getter
public class ConfigLoader {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Configuration configuration;

    public ConfigLoader(@Value("${configuration.file.location}") String configurationFileLocation) {
        ObjectMapper objectMapper = new ObjectMapper();
        this.configuration = objectMapper.readValue(configurationFileLocation, Configuration.class);
    }
}
