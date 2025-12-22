package com.david.agent;

import com.embabel.agent.api.models.OpenAiModels;
import com.embabel.agent.openai.OpenAiChatOptionsConverter;
import com.embabel.common.ai.model.Llm;
import io.micrometer.observation.ObservationRegistry;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

@Configuration
public class CustomModelConfiguration {

    private final String baseUrl;
    private final String apiKey;

    public CustomModelConfiguration(
            @Value("${GROK_BASE_URL}") String baseUrl,
            @Value("${GROK_API_KEY:#{null}}") String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    @Bean
    Llm grok41fast(RestClient.Builder builder, WebClient.Builder webClientBuilder) {
        return createLlm(builder, webClientBuilder);
    }


    private Llm createLlm(RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder) {
        // 1. Configure RestClient Builder with custom headers
        restClientBuilder = restClientBuilder.baseUrl(baseUrl);
        restClientBuilder = restClientBuilder.requestInterceptor((request, body, execution) -> {
            System.out.println("Here I am");
            return execution.execute(request, body);
        });



        ChatModel chatModel = getChatModel(restClientBuilder, webClientBuilder);

        // 4. Wrap in Embabel Llm
        return new Llm(
                "grok-4-1-fast",
                OpenAiModels.PROVIDER,
                chatModel,
                OpenAiChatOptionsConverter.INSTANCE,
                LocalDate.of(2024, 10, 1));
    }

    private @NotNull ChatModel getChatModel(RestClient.Builder builder, WebClient.Builder webClientBuilder) {
        OpenAiApi openAiApi = new OpenAiApi(
                baseUrl, () -> apiKey,
                new LinkedMultiValueMap<>(), // Additional headers
                "/v1/chat/completions",
                "/embeddings",
                builder,
                webClientBuilder, // WebClient.Builder
                new DefaultResponseErrorHandler());

        // 3. Create OpenAiChatModel
        return new OpenAiChatModel(
                openAiApi,
                OpenAiChatOptions.builder().build(),
                ToolCallingManager.builder().build(), // FunctionCallback
                RetryUtils.DEFAULT_RETRY_TEMPLATE,
                ObservationRegistry.NOOP);
    }
}
