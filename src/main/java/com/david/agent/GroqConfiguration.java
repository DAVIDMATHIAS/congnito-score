package com.david.agent;

import com.embabel.agent.api.models.OpenAiModels;
import com.embabel.agent.openai.OpenAiChatOptionsConverter;
import com.embabel.agent.openai.OpenAiCompatibleModelFactory;
import com.embabel.common.ai.model.Llm;
import com.embabel.common.ai.model.PricingModel;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class GroqConfiguration extends OpenAiCompatibleModelFactory {

    public GroqConfiguration(@Value("${OPENAI_BASE_URL}") String baseUrl, @Value("${OPENAI_API_KEY}") String apiKey) {
        super(baseUrl, apiKey, null, null, ObservationRegistry.NOOP);
    }

    @Bean
    Llm gpt5preview() {
        return openAiCompatibleLlm("moonshotai/kimi-k2-instruct", PricingModel.getALL_YOU_CAN_EAT(), OpenAiModels.PROVIDER, LocalDate.of(2024, 10, 1), OpenAiChatOptionsConverter.INSTANCE, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

}
