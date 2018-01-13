package com.mishima.chatbot.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.Map;

@Controller
public class MessengerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessengerController.class);

    @Value("#{environment.VERIFY_TOKEN}")
    private String verifyToken;

    @Value("#{environment.PAGE_ACCESS_TOKEN}")
    private String pageAccessToken;

    private final RestTemplate restTemplate = new RestTemplate();

    @ResponseBody
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public ResponseEntity<String> validate(@RequestParam(value="hub.mode", required = false) final String hubMode,
                                           @RequestParam(value="hub.challenge", required = false) final String hubChallenge,
                                           @RequestParam(value="hub.verify_token", required = false) final String hubVerifyToken) {
        if("subscribe".equals(hubMode)) {
            LOGGER.info("Received subscription request with hub challenge {}", hubChallenge);
            if(!verifyToken.equals(hubVerifyToken)) {
                return new ResponseEntity<>("Verification token mismatch", HttpStatus.FORBIDDEN);
            } else{
                return new ResponseEntity<>(hubChallenge, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("Hello world", HttpStatus.OK);
    }

    @ResponseBody
    @RequestMapping(path = "/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> process(@RequestBody final Map<String,Object> payload) {
        LOGGER.info("Received request -> {}", payload);
        if( "page".equals(payload.get("object"))) {


        }
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }


    private void sendMessage(String recipientId, String message) {
        LOGGER.info("Sending message {} to recipient {}", message, recipientId);
        JsonObject json = Json.createObjectBuilder()
                .add("recipient", Json.createObjectBuilder().add("id", recipientId).build())
                .add("message", Json.createObjectBuilder().add("text", message).build())
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange("https://graph.facebook.com/v2.6/me/messages?access_token={}", HttpMethod.POST, entity, String.class, pageAccessToken);
        LOGGER.info("Received response code {}, message {}", response.getStatusCode(), response.getBody());
    }

}
