# Chat Project

![AC_ 20230405-010147](https://user-images.githubusercontent.com/74857364/229852178-a4c36349-6df4-4af3-b128-e4e5d0cd8a78.gif)

## 주요 기능
- 고객센터 1:1 채팅 / 랜덤 채팅 / 채팅 알림

## 성능 최적화 요약

### [채팅 기록 파일 조회 속도 개선](https://github.com/haedal-uni/socket/wiki/Refactoring#%ED%8C%8C%EC%9D%BC-%EC%A0%80%EC%9E%A5-%EB%B0%A9%EC%8B%9D-%EB%B3%80%EA%B2%BD) 

줄바꿈 포함 저장 방식 → **한 줄 저장 방식**으로 변경하여 조회 속도 개선

<br>

### [채팅 기록 파일 조회 횟수 감소](https://haedal-uni.github.io/posts/%EC%84%B1%EB%8A%A5-%EC%B5%9C%EC%A0%81%ED%99%94-%EC%9A%94%EC%95%BD/#2-%EC%B1%84%ED%8C%85-%EA%B8%B0%EB%A1%9D-%ED%8C%8C%EC%9D%BC-%EC%A1%B0%ED%9A%8C-%ED%9A%9F%EC%88%98-%EA%B0%90%EC%86%8C)  

채팅창 열고 닫을 때마다 파일 조회 → **HashMap 캐싱**으로 변경

<img src="https://github.com/user-attachments/assets/67a5222b-0097-4c76-b940-bbfccec093d9" width="60%" />  

| 방식 | 응답 시간 |
|:---:|:---:|
| Map 미사용 | 0.283초 |
| Map 사용 | 0.017초 |

<img src="https://github.com/user-attachments/assets/0b9e3e40-418f-4b8b-b82f-d4eaced8d254" />

<img src="https://github.com/user-attachments/assets/c855b689-115f-4613-b875-f3e167dac6b0" />

<br>

### RabbitMQ Broker 도입
In-Memory Broker의 용량 제한·메시지 유실·모니터링 문제를 해결하기 위해 **외부 메시지 브로커 RabbitMQ** 적용

<br>

### [Redis Cache로 DB 조회 횟수 감소](https://haedal-uni.github.io/posts/%EC%84%B1%EB%8A%A5-%EC%B5%9C%EC%A0%81%ED%99%94-%EC%9A%94%EC%95%BD/#4-db-%EC%A1%B0%ED%9A%8C-%ED%9A%9F%EC%88%98-%EA%B0%90%EC%86%8C)  

- 전체 값 캐싱: `@Cacheable` 어노테이션 사용
- 일부 값 캐싱: `RedisTemplate` 직접 사용

LastMessage처럼 항상 최신값이 필요한 데이터는 `@CachePut` vs `RedisTemplate` 성능 비교 후 채택

| 방식 | 응답 시간 |
|:---:|:---:|
| Map x + @CachePut | 0.089초 |
| Map o + @CachePut | 0.018초 |
| Map x + RedisTemplate | 0.051초 |
| **Map o + RedisTemplate** | **0.009초** |

→ **Map + RedisTemplate** 조합 채택: 고정 데이터는 캐시에 유지하고 변동 데이터(LastMessage)만 별도 조회

<br><br>

## [API 설계](https://github.com/haedal-uni/socket/wiki/API-%EC%84%A4%EA%B3%84)    

### 고객센터 채팅
| 기능 | Method | URI |
|:---:|:---:|:---:|
| 전체 채팅방 조회 | GET | /rooms |
| 유저 채팅방 조회 | GET | /room/one/{nickname} |
| 채팅방 생성 | POST | /room |
| 채팅방 삭제 | DELETE | /room/one/{roomId} |
| 채팅방 기록 저장 | POST | /room/enter/{roomId}/{roomName} |
| 채팅방 기록 조회 | GET | /room/enter/{roomId}/{roomName} |

<br>

### 고객센터 알림
| 기능 | Method | URI |
|:---:|:---:|:---:|
| 채팅방 구독 | GET | /room/subscribe |
| 채팅방 알림 | GET | /room/publish |

<br><br>

## 부하 테스트 [(JMeter)](https://github.com/haedal-uni/socket/wiki/JMeter)  

<img src="https://github.com/user-attachments/assets/36f277e2-40d2-4c34-a258-c9942e46ad2a" />

Apache JMeter로 랜덤 채팅(`/join`) 엔드포인트 테스트
- Threads: 5 / Ramp-up: 1초 / Loop: 1회
- 결과: 2명씩 매칭 성공, 미매칭 1명은 20초 대기 후 timeout 처리 확인
