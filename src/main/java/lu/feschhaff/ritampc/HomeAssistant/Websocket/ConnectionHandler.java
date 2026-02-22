package lu.feschhaff.ritampc.HomeAssistant.Websocket;

import jakarta.websocket.*;
import lombok.extern.slf4j.Slf4j;
import lu.feschhaff.ritampc.CommonTools.MicrometerRegistry;
import lu.feschhaff.ritampc.HomeAssistant.Models.DTOs.request.Request;
import lu.feschhaff.ritampc.HomeAssistant.Models.DTOs.response.Response;
import lu.feschhaff.ritampc.HomeAssistant.Registries.EntityRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author Joé Welsch
 * @references {
 *     <a href="https://developers.home-assistant.io/docs/api/websocket/">...</a>
 *     <a href="https://developers.home-assistant.io/docs/api/websocket/#authentication-phase">...</a>
 *     <a href="https://www.baeldung.com/java-websockets">...</a>
 *     <a href="https://www.home-assistant.io/docs/configuration/entities_domains/">Entities and Domains</a>
 * }
 */

@Slf4j @ClientEndpoint @Component
public class ConnectionHandler {

    private final EntityRegistry entityRegistry;
    private final MicrometerRegistry micrometerRegistry;
    private int messageId = 0;

    @Value("${home.assistant.websocket.access_token}")
    String access_token;

    public ConnectionHandler(
            EntityRegistry entityRegistry,
            MicrometerRegistry micrometerRegistry
    ) {
        this.entityRegistry = entityRegistry;
        this.micrometerRegistry = micrometerRegistry;
    }

    @OnOpen
    public void onOpen(Session session) {
        // Get session and WebSocket connection
        log.info("Session successfully created");
    }

    @OnMessage
    public void onMessage(Session session, String jsonResponse) throws IOException {
        Response response = new ObjectMapper().readValue(jsonResponse, Response.class);
        String type = response.getType();

        switch (type) {
            case "auth_required": {
                log.info("Authentication request from Home Assistant");

                Request request = Request.builder()
                        .type("auth")
                        .access_token(access_token)
                        .build();

                sendMessage(session, request);
                break;
            }
            case "auth_ok": {
                log.info("Authentication successful, now subscribed to events!");

                Request request = Request.builder()
                        .id(++messageId)
                        .type("subscribe_events")
                        .event_type("state_changed")
                        .build();

                sendMessage(session, request);
                break;
            }
            case "event": {
                String entityId = response.getEvent().getData().getEntity_id();
                String entityIdWithoutPrefix = entityId.split("\\.")[1];
                this.entityRegistry.getEntityRegistry().put(entityIdWithoutPrefix, response);

                String state = response.getEvent().getData().getNew_state().getState();
                try {
                    float stateAsFloat = Float.parseFloat(state);
                    this.micrometerRegistry.updateGauge(entityIdWithoutPrefix, "current", stateAsFloat);
                log.info(entityId);
                } catch (Exception e) {
                    // TODO: Logic to handle non gauges
                    log.warn("Sate is not a gauge!");
                }
                break;
            }
            case "result": {
                log.info("Successfully subscribed to events!");
                break;
            }
            default: {
                log.error("Unrecognized event type {}", jsonResponse);
                break;
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        // WebSocket connection closes
        log.info("Session closed");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        log.error("Error occurred", throwable);
    }

    public void sendMessage(Session session, Request request) throws IOException {
        log.info("Sending message: {}", request.toString());

        String jsonRequest = new ObjectMapper().writeValueAsString(request);
        session.getBasicRemote().sendText(jsonRequest);
    }
}


