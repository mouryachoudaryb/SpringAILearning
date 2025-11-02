package com.github.mourya.springailearning.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class OpenAIController {

    private static final Logger log = LoggerFactory.getLogger(OpenAIController.class);

    private ChatClient chatClient;

   public OpenAIController(OpenAiChatModel openAIChatModel) {
        this.chatClient = ChatClient.create(openAIChatModel);
    }

    ChatMemory chatMemory =  MessageWindowChatMemory.builder().build();


/*    public OpenAIController(ChatClient.Builder builder) {
        this.chatClient = builder.
                     defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                     .build();
    }*/

    @GetMapping("/openapi/{message}")
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

    @PostMapping("/openapi/recommend")
    public ResponseEntity<String> recommend(@RequestParam String type,@RequestParam String year,
                                            @RequestParam String lang) {

        String template = """
                           I want to watch a {type} movie released in {year} in {lang} language with a good rating
                           Can you recommend me one movie along with cast,length of the movie and a brief description?

                           Response should be in below format:
                            Movie Name:
                            Cast:
                            Length:
                            Imdb Rating:
                            Description:
                           """;

        PromptTemplate promptTemplate = new PromptTemplate(template);

        Prompt prompt = promptTemplate.create(Map.of(
                "type", type,
                "year", year,
                "lang", lang
        ));

        String response = chatClient
                .prompt(prompt)
                .call()
                .content();


        return ResponseEntity.ok(response);
    }
}
