package com.saggu.ai.mcp.service;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.nio.file.Paths;
import java.time.Duration;

@RestController
@Data
public class OllamaService {

    public record OllamaResponse(String response, String timeTaken) {
    }

    private final OllamaChatModel ollamaChatModel;
    private final ChatClient chatClient;

    public OllamaService(OllamaChatModel ollamaChatModel, ChatClient.Builder chatClientBuilder) {
        this.ollamaChatModel = ollamaChatModel;

        this.chatClient = chatClientBuilder
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpClient()))
                .build();
    }

    @GetMapping("/ollama")
    public OllamaResponse getOllamaResponse(@RequestParam String prompt) {
        long startTime = System.currentTimeMillis();
        System.out.println("Received prompt: " + prompt);
        ChatResponse response = ollamaChatModel.call(
                new Prompt(
                        prompt,
                        OllamaOptions.builder()
                                .temperature(0.4)
                                .build()
                ));

        long endTime = System.currentTimeMillis();
        long timeTakenInSeconds = (endTime - startTime) / 1000;

        OllamaResponse ollamaResponse = new OllamaResponse(response.getResult().getOutput().getText(), timeTakenInSeconds + " seconds");
        System.out.println("Ollama response: " + ollamaResponse);
        return ollamaResponse;
    }

    //MCP Tool
    @GetMapping("/ollama-file")
    public OllamaResponse predefinedQuestions(@RequestParam String prompt) {
        System.out.println("Received prompt: " + prompt);
        long startTime = System.currentTimeMillis();
        String response = chatClient.prompt("Check the content of the target/news.txt file to answer. " + prompt).call().content();
        long endTime = System.currentTimeMillis();
        long timeTakenInSeconds = (endTime - startTime) / 1000;
        OllamaResponse ollamaResponse = new OllamaResponse(response, timeTakenInSeconds + " seconds");
        System.out.println("Response: " + ollamaResponse);
        return ollamaResponse;
    }

    @GetMapping("/ollama-file-system")
    public OllamaResponse ollamaFileSystem(@RequestParam String prompt) {
        System.out.println("Received prompt: " + prompt);
        long startTime = System.currentTimeMillis();
        String response = chatClient.prompt(prompt).call().content();
        long endTime = System.currentTimeMillis();
        long timeTakenInSeconds = (endTime - startTime) / 1000;
        OllamaResponse ollamaResponse = new OllamaResponse(response, timeTakenInSeconds + " seconds");
        System.out.println("Response: " + ollamaResponse);
        return ollamaResponse;
    }

    @GetMapping("/ollama-stream")
    public OllamaResponse ollamaStream(@RequestParam String prompt) {
        System.out.println("Received prompt: " + prompt);
        long startTime = System.currentTimeMillis();
        System.out.println("Streaming response");
        Flux<ChatResponse> responses = chatClient.prompt(prompt).stream().chatResponse();
        System.out.println("Blocking after responses: " + responses);
        responses.blockLast(Duration.ofSeconds(20));
        System.out.println("Responses received, processing chunks...");
        responses.subscribe(chatResponse -> {
             System.out.println("Chunk: " + chatResponse);
        });

        System.out.println("Streaming completed");

        long endTime = System.currentTimeMillis();
        long timeTakenInSeconds = (endTime - startTime) / 1000;
        OllamaResponse ollamaResponse = new OllamaResponse("response", timeTakenInSeconds + " seconds");
        System.out.println("Response: " + ollamaResponse);
        return ollamaResponse;
    }

    public McpSyncClient mcpClient() {

        // based on https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem
        var stdioParams = ServerParameters.builder("npx")
                .args("-y", "@modelcontextprotocol/server-filesystem", getDbPath())
                .build();

        var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
                .requestTimeout(Duration.ofSeconds(10)).build();

        var init = mcpClient.initialize();

        System.out.println("MCP Initialized: " + init);

        return mcpClient;

    }

    private static String getDbPath() {
        return Paths.get(System.getProperty("user.dir"), "target").toString();
    }
}
