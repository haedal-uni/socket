package com.dalcho.adme.config.security;

import com.dalcho.adme.oauth2.CustomOAuthService;
import com.dalcho.adme.oauth2.OAuth2SuccessHandler;
import com.dalcho.adme.oauth2.Oauth2FailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity //웹보안 활성화를위한 annotation
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuthService customOAuth2UserService;
    private final OAuth2SuccessHandler successHandler;
    private final Oauth2FailureHandler failureHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    public static final String[] VIEW_LIST = {
            "/favicon.ico/**",
            "/adme",
            "/taste",
            "/ws/**",
            "/oauth2/**",
            "/alarm/**",
            "/webjars/**", "/templates/**"
    };

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> {
			web.ignoring()
					.requestMatchers(
							 "/css/**","/fonts/**", "/js/**","/webjars/**","/user/**","/error"
					);
		};
	}


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().configurationSource(corsConfigurationSource());
        http.csrf().disable() // rest api 에서는 csrf 공격으로부터 안전하고 매번 api 요청으로부터 csrf 토큰을 받지 않아도 되어 disable로 설정
                .sessionManagement(); // Rest Api 기반 애플리케이션 동작 방식 설정
//				.sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션을 사용하지 않음
//				.and().headers().frameOptions().disable().and().formLogin().disable() // 로그인 폼 미사용
//					.httpBasic().disable() // Http basic Auth 기반으로 로그인 인증창이 열림(disable 시 인증창 열리지 않음)
//					.exceptionHandling().authenticationEntryPoint(new RestAuthenticationEntryPoint())// 인증,인가가 되지 않은 요청 시 발생
//					.and()
        http.authorizeHttpRequests()
                .requestMatchers(VIEW_LIST).permitAll()
                .requestMatchers("/sign-up").permitAll()
                .requestMatchers("/sign-in").permitAll()
                .requestMatchers("/admin").hasAuthority("ADMIN")
                .anyRequest().authenticated();

        http.formLogin()
                .loginPage("/user/login")
                .defaultSuccessUrl("/adme")
                .and()
                    .logout()
                    .logoutUrl("/user/logout") // 로그아웃 처리 URL
                    .logoutSuccessUrl("/user/login")
                .and()
                    .oauth2Login()
                    .loginPage("/user/login")
                    .userInfoEndpoint().userService(customOAuth2UserService)
                .and()
                    .successHandler(successHandler)
                    .failureHandler(failureHandler)
                .and()
                    .exceptionHandling()
                    //.accessDeniedHandler(new CustomAccessDeniedHandler())
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                .and()
                    .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setMaxAge(3600L);
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}