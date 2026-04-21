package com.sep490.vtuber_fanhub.configurations;

import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SseConfig implements WebMvcConfigurer {


    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(300000);
    }

    @Bean
    public SseTimeoutConfig sseTimeoutConfig() {
        return new SseTimeoutConfig();
    }

    @Getter
    public static class SseTimeoutConfig {
        private final long timeout = 300000;
        
        private final long reconnectionTime = 3000;

    }
}
