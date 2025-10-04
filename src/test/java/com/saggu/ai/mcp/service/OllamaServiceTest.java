package com.saggu.ai.mcp.service;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OllamaServiceTest {
    @Mock
    private OllamaChatModel ollamaChatModel;
    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Explicitly cast to ToolCallbackProvider to resolve ambiguity
        when(chatClientBuilder.defaultToolCallbacks((org.springframework.ai.tool.ToolCallbackProvider) any())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);
    }

    @Test
    @DisplayName("Test getOllamaResponse returns expected response")
    void testGetOllamaResponse() {
        String prompt = "Hello";
        String expectedText = "Hi there!";
        // Mock the response chain: getResult().getOutput().getText()
        ChatResponse chatResponse = mock(ChatResponse.class, RETURNS_DEEP_STUBS);
        when(chatResponse.getResult().getOutput().getText()).thenReturn(expectedText);
        when(ollamaChatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        OllamaService service = new OllamaService(ollamaChatModel, chatClientBuilder);
        OllamaService.OllamaResponse response = service.getOllamaResponse(prompt);

        assertEquals(expectedText, response.response());
        assertTrue(response.timeTaken().endsWith("seconds"));
    }
}
