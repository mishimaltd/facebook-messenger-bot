package com.mishima.chatbot.web.controller;

import flexjson.JSONDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class MessengerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessengerController.class);

    @Value("#{environment.VERIFY_TOKEN}")
    private String verifyToken;

    @Value("#{environment.PAGE_ACCESS_TOKEN}")
    private String pageAccessToken;

    private static final String SIGNATURE_HEADER_NAME = "X-Hub-Signature";

    private final RestTemplate restTemplate = new RestTemplate();

    private final JSONDeserializer<Map<String,Object>> jsonDeserializer = new JSONDeserializer<>();

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> verify(@RequestParam(value="hub.mode", required = false) final String hubMode,
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
    @RequestMapping(method = RequestMethod.POST)
    @SuppressWarnings("unchecked")
    public ResponseEntity<String> handleCallback(@RequestBody final String payload, @RequestHeader(SIGNATURE_HEADER_NAME) final String signature) {
        LOGGER.info("Received request -> {} with signature -> {}", payload, signature);
        Map<String,Object> request = jsonDeserializer.deserialize(payload);
        if("page".equals(request.get("object"))) {
            List<Map<String,Object>> messageEntries = (List<Map<String,Object>>)request.get("entry");
            for(Map<String,Object> messageEntry: messageEntries) {
                List<Map<String,Object>> messages = (List<Map<String,Object>>)messageEntry.get("messaging");
                if( messages != null ) {
                    for(Map<String,Object> message: messages ) {
                        Map<String, Object> senderDetails = (Map<String, Object>) message.get("sender");
                        String senderId = (String) senderDetails.get("id");
                        Map<String, Object> messageDetails = (Map<String, Object>) message.get("message");
                        if (messageDetails != null) {
                            String text = (String) messageDetails.get("text");
                            LOGGER.info("Received message {} from sender {}", text, senderId);
                            sendMessage(senderId, "Thanks very much!!!");
                        }
                    }
                }
            }

        }
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }


    private void sendMessage(String recipientId, String text) {
        LOGGER.info("Sending message {} to recipient {}", text, recipientId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "https://graph.facebook.com/v2.6/me/messages?access_token={}%recipient={}&message={}";
        String recipient = String.format("{'id':'%s'}", recipientId);
        String message = String.format("{'text':'%s'}", text);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class, pageAccessToken, recipient, message);
        LOGGER.info("Received response code {}, message {}", response.getStatusCode(), response.getBody());
    }

}
