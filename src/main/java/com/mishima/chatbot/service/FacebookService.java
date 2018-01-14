package com.mishima.chatbot.service;

import flexjson.JSONDeserializer;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;

@Service
public class FacebookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookService.class);

    @Value("#{environment.PAGE_ACCESS_TOKEN}")
    private String pageAccessToken;

    private final CloseableHttpClient httpclient = HttpClients.createDefault();

    private final JSONDeserializer<Map<String,Object>> jsonDeserializer = new JSONDeserializer<>();

    public int sendMessage(String recipientId, String text) throws Exception {
        LOGGER.info("Sending message {} to recipient {}", text, recipientId);
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost("graph.facebook.com")
                .setPath("/v2.6/me/messages")
                .setParameter("access_token", pageAccessToken)
                .setParameter("recipient", String.format("{'id':'%s'}", recipientId))
                .setParameter("message", String.format("{'text':'%s'}", text))
                .build();
        HttpPost post = new HttpPost(uri);
        post.setHeader("content-type", "application/json");
        try (CloseableHttpResponse response = httpclient.execute(post)) {
            StatusLine statusLine = response.getStatusLine();
            LOGGER.info("Received response code {}, reason {}", statusLine.getStatusCode(), statusLine.getReasonPhrase());
            return statusLine.getStatusCode();
        }
    }

    public Map<String,Object> person(String userId) throws Exception {
        LOGGER.info("Lookup up user with id {}", userId);
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost("graph.facebook.com")
                .setPath("/v2.6/" + userId)
                .setParameter("access_token", pageAccessToken)
                .setParameter("fields", "first_name,last_name")
                .build();
        HttpGet get = new HttpGet(uri);
        try (CloseableHttpResponse response = httpclient.execute(get)) {
            StatusLine statusLine = response.getStatusLine();
            LOGGER.info("Received response code {}, reason {}", statusLine.getStatusCode(), statusLine.getReasonPhrase());
            HttpEntity entity = response.getEntity();
            String json = IOUtils.toString(entity.getContent(), "utf-8");
            LOGGER.info("Received response json -> {}", json);
            return jsonDeserializer.deserialize(json);
        }

    }
}
