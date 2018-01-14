package com.mishima.chatbot.client.watson;

import com.ibm.watson.developer_cloud.conversation.v1.Conversation;
import com.ibm.watson.developer_cloud.conversation.v1.model.Context;
import com.ibm.watson.developer_cloud.conversation.v1.model.InputData;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

@Service
public class WatsonClient {

    private static final Logger log = LoggerFactory.getLogger(WatsonClient.class);

    @Value("#{environment.WORKSPACE_ID}")
    private String workspaceId;

    @Value("#{environment.WORKSPACE_USERNAME}")
    private String username;

    @Value("#{environment.WORKSPACE_PASSWORD}")
    private String password;

    private Conversation conversation;

    @PostConstruct
    private void init() {
        conversation = new Conversation(Conversation.VERSION_DATE_2017_05_26, username, password);
        log.info("Initialized conversation to workspaceId {}", workspaceId);
    }

    public MessageResponse request(String message) {
        return request(message, newHashMap());
    }

    public MessageResponse request(String message, Map<String,Object> requestContext) {
        Context context = new Context();
        context.putAll(requestContext);
        MessageOptions messageOptions = new MessageOptions.Builder()
                .input(new InputData.Builder().text(message).build())
                .context(context)
                .workspaceId(workspaceId)
                .build();
        log.info("Sending request {}", messageOptions);
        MessageResponse response = conversation.message(messageOptions).execute();
        log.info("Received response {}", response);
        return response;
    }


}
