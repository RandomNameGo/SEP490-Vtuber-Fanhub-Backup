package com.sep490.vtuber_fanhub.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final StringRedisTemplate redisTemplate;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configure(http))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**"
                        ).permitAll()
                        .requestMatchers("/vhub/api/v1/auth/**").permitAll()
                        .requestMatchers("/vhub/api/v1/user/verify").permitAll()
                        .requestMatchers("/vhub/api/v1/user/register").permitAll()
                        .requestMatchers(HttpMethod.GET,"/vhub/api/v1/user/*").permitAll()
                        .requestMatchers("/vhub/api/v1/posts/feed").permitAll()
                        .requestMatchers("/vhub/api/v1/user/user-name/*").permitAll()
                        .requestMatchers("/vhub/api/v1/fan-hub/top").permitAll()
                        .requestMatchers(HttpMethod.GET,"/vhub/api/v1/posts/*").permitAll()
                        .requestMatchers(HttpMethod.GET,"/vhub/api/v1/posts/fan-hub/{fanHubId}/announcements-events").permitAll()
                        .requestMatchers(HttpMethod.GET,"/vhub/api/v1/posts/{postId}/comments").permitAll()
                        .requestMatchers(HttpMethod.GET,"/vhub/api/v1/posts/comments/{parentCommentId}/replies").permitAll()
                        .requestMatchers(HttpMethod.GET,"/vhub/api/v1/fan-hub/subdomain/{subdomain}").permitAll()
                        .requestMatchers(HttpMethod.GET,"/vhub/api/v1/posts/fan-hub/*").permitAll()
                        .requestMatchers("/vhub/api/v1/search/**").permitAll()
                        .requestMatchers("/vhub/api/v1/webhooks/sightengine/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder()).
                                jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String key = "aec5162f0ed647d4bb3cc9c926b2fb6af809992b56055bed2130adb9a1c3de8da55bfd2ef287029cb7a7e36b0d15b14b04d68cde8b060c3e5fbc6fcc7891bbe9";
        SecretKey secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HS512");
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();

        return token -> {
            if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
                throw new BadJwtException("Token is blacklisted");
            }
            return nimbusJwtDecoder.decode(token);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
