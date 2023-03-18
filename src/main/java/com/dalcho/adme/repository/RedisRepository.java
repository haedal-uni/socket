package com.dalcho.adme.repository;

import com.dalcho.adme.model.Redis;
import org.springframework.data.repository.CrudRepository;

public interface RedisRepository extends CrudRepository<Redis, String> {
	Redis findByNickname(String nickname);
	Redis findByEmail(String email);

}
