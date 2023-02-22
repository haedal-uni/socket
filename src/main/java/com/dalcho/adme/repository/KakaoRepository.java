package com.dalcho.adme.repository;

import com.dalcho.adme.model.Kakao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KakaoRepository extends JpaRepository<Kakao, Long> {
	Optional<Kakao> findByKakaoIdx(Long kakaoIdx);
}
