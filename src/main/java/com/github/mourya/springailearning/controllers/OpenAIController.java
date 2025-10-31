package com.github.mourya.springailearning.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenAIController {

    private static final Logger log = LoggerFactory.getLogger(OpenAIController.class);

    private ChatClient chatClient;

/*    public OpenAIController(OpenAiChatModel openAIChatModel) {
        this.chatClient = ChatClient.create(openAIChatModel);
    }*/

    public OpenAIController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/api/{message}")
    public ResponseEntity<String> chatAnswer(@PathVariable String message) {

        try{

            ChatResponse chatResponse = chatClient.prompt(message)
                    .call()
                    .chatResponse();

            System.out.println(chatResponse.getMetadata().getModel());

            String response = chatResponse.getResult()
                    .getOutput()
                    .getText();

            return ResponseEntity.ok( response);
        }catch(Exception e){
            log.error("Exception occurred while calling OpenAI API: ", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Error calling external OpenAI service.");
        }

    }
}
