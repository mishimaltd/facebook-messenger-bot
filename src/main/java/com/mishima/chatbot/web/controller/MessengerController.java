package com.mishima.chatbot.web.controller;

import com.mishima.chatbot.service.FacebookService;
import flexjson.JSONDeserializer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class MessengerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessengerController.class);

    @Autowired
    private FacebookService facebookService;

    @Value("#{environment.VERIFY_TOKEN}")
    private String verifyToken;

    private static final String SIGNATURE_HEADER_NAME = "X-Hub-Signature";

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
    public ResponseEntity<String> handleCallback(@RequestBody final String payload, @RequestHeader(SIGNATURE_HEADER_NAME) final String signature) throws Exception {
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
                            Boolean isEcho = (Boolean)messageDetails.get("is_echo");
                            if( isEcho != null && isEcho) {
                                LOGGER.info("Received echo of message {} from sender {}", text, senderId);
                            } else {
                                LOGGER.info("Received new message {} from sender {}", text, senderId);
                                Map<String,Object> person = facebookService.person(senderId);
                                facebookService.sendMessage(senderId, String.format("Hello %s!!", person.get("first_name")));
                            }
                        }
                    }
                }
            }

        }
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

}
