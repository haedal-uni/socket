package com.dalcho.adme.config.security;

import com.dalcho.adme.oauth2.CustomOAuthService;
import com.dalcho.adme.oauth2.OAuth2SuccessHandler;
import com.dalcho.adme.oauth2.Oauth2FailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity //웹보안 활성화를위한 annotation
public class SecurityConfig {
	private final JwtTokenProvider jwtTokenProvider;
	private final CustomOAuthService customOAuth2UserService;
	private final OAuth2SuccessHandler successHandler;
	private final Oauth2FailureHandler failureHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http)throws Exception {
		http.cors();
		http.csrf().disable() // rest api 에서는 csrf 공격으로부터 안전하고 매번 api 요청으로부터 csrf 토큰을 받지 않아도 되어 disable로 설정
				.sessionManagement(); // Rest Api 기반 애플리케이션 동작 방식 설정
//				.sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션을 사용하지 않음
//				.and().headers().frameOptions().disable().and().formLogin().disable() // 로그인 폼 미사용
//					.httpBasic().disable() // Http basic Auth 기반으로 로그인 인증창이 열림(disable 시 인증창 열리지 않음)
//					.exceptionHandling().authenticationEntryPoint(new RestAuthenticationEntryPoint())// 인증,인가가 되지 않은 요청 시 발생
//					.and()
		http.authorizeRequests()
				.antMatchers("/css/**", "/oauth2/**", "/user/**", "/adme/**", "/taste/**").permitAll()

				.anyRequest().authenticated();

		http.oauth2Login().userInfoEndpoint().userService(customOAuth2UserService)
				.and()
				.successHandler(successHandler)
				.failureHandler(failureHandler);
		http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
