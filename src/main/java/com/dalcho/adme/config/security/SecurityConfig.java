package com.dalcho.adme.config.security;

import com.dalcho.adme.oauth2.OAuth2SuccessHandler;
import com.dalcho.adme.oauth2.Oauth2FailureHandler;
import com.dalcho.adme.oauth2.PracOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity //웹보안 활성화를위한 annotation
public class SecurityConfig {
	private final JwtTokenProvider jwtTokenProvider;
	private final PracOAuthService customOAuth2UserService;
	private final OAuth2SuccessHandler successHandler;
	private final Oauth2FailureHandler failureHandler;
// 리디렉션 에러는 seesion 문제였음
	// .sessionCreationPolicy(SessionCreationPolicy.STATELESS); 이거 써서 그런거였으
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http)throws Exception {
		http.csrf().disable() // rest api 에서는 csrf 공격으로부터 안전하고 매번 api 요청으로부터 csrf 토큰을 받지 않아도 되어 disable로 설정
				.sessionManagement(); // Rest Api 기반 애플리케이션 동작 방식 설정
//				.sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션을 사용하지 않음
//				.and().headers().frameOptions().disable().and().formLogin().disable() // 로그인 폼 미사용
//					.httpBasic().disable() // Http basic Auth 기반으로 로그인 인증창이 열림(disable 시 인증창 열리지 않음)
//					.exceptionHandling().authenticationEntryPoint(new RestAuthenticationEntryPoint())// 인증,인가가 되지 않은 요청 시 발생
//					.and()
		http.authorizeRequests()
				.antMatchers("/css/**", "/login/**", "/oauth2/**").permitAll()

				.anyRequest().authenticated();


//				.oauth2Login() // OAuth2 로그인 설정 시작점
//				.userInfoEndpoint() // OAuth2 로그인 성공 이후 사용자 정보를 가져올 때 설정 담당
//				.userService(customOAuth2UserService) // OAuth2 로그인 성공 시, 후작업을 진행할 UserService 인터페이스 구현체 등록
//				.and()
//				.successHandler(successHandler)
//				.failureHandler(failureHandler)
//				//.failureUrl("/fun")
//				.authorizationEndpoint().baseUri("/oauth2/authorize") // 소셜 로그인 Url
//				.and()
//				.redirectionEndpoint().baseUri("/oauth2/callback/**");// 소셜 인증 후 Redirect Url
		http.oauth2Login().userInfoEndpoint().userService(customOAuth2UserService)
				.and()
				.successHandler(successHandler)
				.failureHandler(failureHandler);
		http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
