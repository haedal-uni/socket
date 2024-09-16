package com.dalcho.adme.config.schedule;

import com.dalcho.adme.model.Stats;
import com.dalcho.adme.repository.StatsRepository;
import com.dalcho.adme.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@EnableScheduling  // 스케줄링 활성화
@Service
public class StatsScheduler {
    private final RedisService redisService;
    private final RedisTemplate<String, String> redisTemplate;
    private final StatsRepository repository;


    @Scheduled(cron = "0 5 0 * * *")  // 매일 새벽 12시 5분에 실행
    public void calculateDailyStats() {
        log.info("[stats scheduler] 스케줄러 실행");
        try{
            LocalDate now = LocalDate.now();
            Long totalLoginUser = Optional.ofNullable(redisTemplate.opsForSet().size(now.minusDays(1) + "-LoginUser")).orElse(0L);
            Long totalChatUser = Optional.ofNullable(redisTemplate.opsForSet().size(now.minusDays(1) + "-ChatUser")).orElse(0L);
            double participationRate = 0;
            if(totalLoginUser>0){
                participationRate = ((double) totalChatUser / totalLoginUser) * 100;
            }
            Stats stats = Stats.builder()
                    .date(now.minusDays(1))
                    .users(totalLoginUser)
                    .chat(totalChatUser)
                    .participationRate(participationRate)
                    .build();
            repository.save(stats);
            redisService.deleteLoginUserCount(now.minusDays(1) + "-LoginUser");
            redisService.deleteChatUserCount(now.minusDays(1) + "-ChatUser");
            log.info("성공적으로 통계 저장 완료. 날짜: {}", stats.getStatDate());
        }catch (Exception e){
            log.info("일일 통계 계산 중 오류 발생 ", e);
        }
    }
}
