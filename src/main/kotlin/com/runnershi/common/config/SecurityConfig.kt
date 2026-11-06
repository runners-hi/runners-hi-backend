package com.runnershi.common.config

import com.runnershi.common.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.runnershi.common.exception.ErrorCode
import com.runnershi.common.response.ApiResponse
import jakarta.servlet.http.HttpServletResponse

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/logout").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/terms").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/regions").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/regions/*/districts").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/notices/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/app/version/check").permitAll()
                    .requestMatchers(
                        "/api/auth/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        "/actuator/health"
                    ).permitAll()
                    // 인증은 API 경로에만 건다. 없는 주소는 JSON 404로 돌려준다
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ -> writeError(response, ErrorCode.UNAUTHORIZED) }
                it.accessDeniedHandler { _, response, _ -> writeError(response, ErrorCode.ACCESS_DENIED) }
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    private fun writeError(response: HttpServletResponse, errorCode: ErrorCode) {
        response.status = errorCode.status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(ObjectMapper().writeValueAsString(ApiResponse.error(errorCode.code, errorCode.message)))
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
