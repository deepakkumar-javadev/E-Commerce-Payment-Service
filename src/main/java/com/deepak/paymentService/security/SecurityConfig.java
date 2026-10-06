package com.deepak.paymentService.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http

				// =========================
				// CSRF
				// =========================
				.csrf(csrf -> csrf.disable())

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth

						// =========================
						// RAZORPAY WEBHOOK
						// PUBLIC
						// =========================
						.requestMatchers("/actuator/**").permitAll().requestMatchers("/payments/webhook").permitAll()

						// =========================
						// CUSTOMER ONLY
						// =========================
						.requestMatchers("/payments/createOrder").hasRole("CUSTOMER")
						
						// Swagger
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
						
						// =========================
						// CUSTOMER + ADMIN
						// =========================
						.requestMatchers("/payments/get/**").permitAll()

						// =========================
						// EVERYTHING ELSE
						// JWT REQUIRED
						// =========================
						.anyRequest().authenticated())

				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}