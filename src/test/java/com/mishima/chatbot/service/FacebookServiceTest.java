package com.mishima.chatbot.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FacebookServiceTest {

    @Autowired
    private FacebookService facebookService;

    private final String userId = "1642366669154566";

    @Test
    public void testGetPerson() throws Exception {
        Map<String,Object> person = facebookService.person(userId);
        assertEquals("Joe", person.get("first_name"));
    }

    @Test
    public void testSendMessage() throws Exception {
        assertEquals(200, facebookService.sendMessage(userId, "Test message"));
    }

}
