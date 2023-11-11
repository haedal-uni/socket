package com.dalcho.adme.config.security;

import com.dalcho.adme.model.User;
import com.dalcho.adme.model.UserRole;
import com.dalcho.adme.service.UserDetailService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

	private final UserDetailService userDetailsService;

	@Value("${springboot.jwt.secret}")
	private String secretKey; // 토큰 생성에 필요한 key
	private final long TOKEN_VALID_MILISECOND = 1000L * 60 * 60 * 10; // token 유효시간 : 10시간
	long refreshPeriod = 1000L * 60L * 60L * 24L * 30L * 3L;

	// SecretKey 초기화
	@PostConstruct // Bean 객체로 주입된 후 수행
	protected void init() {
		log.info("[init] JwtTokenProvider 내 secretKey 초기화 시작");

		// secretKey 를 base64 형식으로 인코딩
		secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));

		log.info("[init] JwtTokenProvider 내 secretKey 초기화 완료");
	}

	public String generateToken(User user) {
		log.info("[createToken] 토큰 생성 시작");
		// Claims 객체에 담아 Jwt Token 의 내용에 값 넣기, sub 속성에 값 추가(Uid 사용)
		Claims claims = Jwts.claims().setSubject(user.getNickname());  // JWT payload 에 저장되는 정보단위
//		claims.put("nickname", user.getNickname());
		claims.put("roles", user.getRole().name()); // 사용자 권한확인용 추가
		Date now = new Date();

		// Token 생성
		String token = Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + TOKEN_VALID_MILISECOND))
				.signWith(SignatureAlgorithm.HS256, secretKey)
				.compact();

		log.info("[createToken] 토큰 생성 완료");
		return token;
	}

	// 필터에서 인증에 성공시 SecurityContextHolder 에 저장할 Authentication 생성
	public Authentication getAuthentication(String token) {
		log.info("[getAuthentication] 토큰 인증 정보 조회 시작");
		Claims claims = getClaims(token).getBody();
		String role = claims.get("roles").toString();
		User user = User.builder()
				.nickname(claims.getSubject())
				.role(UserRole.of(role))
				.build();
		UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
		log.info("[getAuthentication] 토큰 인증 정보 조회 완료 : " + user);
		return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
		//return new UsernamePasswordAuthenticationToken(userDetails, " ", userDetails.getAuthorities());
	}

	public String getNickname(String token) { 	// jwt token을 복화하 하여 이름을 얻는다.
		log.info("[getUsername] 토큰 기반 회원 구별 정보 추출");
		// 토큰을 생성할때 넣었던 sub 값 추출
		String info = getClaims(token).getBody().getSubject();
		log.info("[getUsername] 토큰 기반 회원 구별 정보 추출 완료 : " + info);
		return info;
	}


	// Token 유효기간 체크
	public boolean validateToken(String token) {
		log.info("[validateToken] 토큰 유효 체크 시작");

		try {
			Jws<Claims> claims = Jwts.parser().
					setSigningKey(secretKey)
					.parseClaimsJws(token);

			return !claims.getBody().getExpiration().before(new Date());

		} catch (Exception e) {
			log.warn("[validateToken] 토큰 유효 체크 예외 발생");
			return false;
		}
	}

	private Jws<Claims> getClaims(String jwt){
		try{
			return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt);
		}catch (SignatureException e){
			log.error("Invalid JWT signature");
			throw e;
		} catch (MalformedJwtException e){
			log.error("Invalid JWT token");
			throw e;
		} catch (ExpiredJwtException e){
			log.error("Expired JWT token");
			throw e;
		} catch (UnsupportedJwtException e){
			log.error("Unsupported JWT token");
			throw e;
		} catch (IllegalArgumentException e){
			log.error("JWT claims string is empty");
			throw e;
		}
	}

	public User getUserFromToken(String token) {
		Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
		String nickname = claims.getSubject();
		String role = claims.get("roles", String.class);
		log.info("[chat] token - user 전체 정보 확인");
		UserRole userRole = UserRole.of(role);
		return User.builder()
				.nickname(nickname)
				.role(userRole)
				.build();
	}

	public String resolveToken(HttpServletRequest request) {
		log.info("[resolveToken] Header 에서 Token 추출 완료");
		return request.getHeader("Authorization");
	}

}

