# Chat Project

![AC_ 20230405-010147](https://user-images.githubusercontent.com/74857364/229852178-a4c36349-6df4-4af3-b128-e4e5d0cd8a78.gif)

## 주요 기능
- 고객센터 1:1 채팅 / 랜덤 채팅 / 채팅 알림

## 성능 최적화 요약
아래 수치는 모두 JMH 벤치마크(`fork=2`, warmup 10회 × 2초, 측정 10회 × 3초)로 측정한 값이다.

<br>

### [채팅 기록 파일 조회 속도 개선](https://github.com/haedal-uni/socket/wiki/Refactoring#%ED%8C%8C%EC%9D%BC-%EC%A0%80%EC%9E%A5-%EB%B0%A9%EC%8B%9D-%EB%B3%80%EA%B2%BD) 

줄바꿈 포함 저장 방식 → **한 줄 저장 방식**으로 변경하여 조회 속도 개선

| 방식 | 조회 시간 | 오차 비율 |
|:---:|:---:|:---:|
| 7줄 스캔 (기존) | 6.062 ± 0.218 ms | 3.6% |
| **1줄 읽기 (개선)** | **2.184 ± 0.059 ms** | 2.7% |

평균 기준 약 **2.8배** 빨라졌다.

기존 방식은 마지막 줄이 길수록 더 많이 읽어야 해서 실행마다 시간이 달라지고, 개선한 방식은 읽는 양이 거의 고정이다.

<br>

### [채팅 기록 파일 조회 횟수 감소](https://haedal-uni.github.io/posts/%EC%84%B1%EB%8A%A5-%EC%B5%9C%EC%A0%81%ED%99%94-%EC%9A%94%EC%95%BD/#2-%EC%B1%84%ED%8C%85-%EA%B8%B0%EB%A1%9D-%ED%8C%8C%EC%9D%BC-%EC%A1%B0%ED%9A%8C-%ED%9A%9F%EC%88%98-%EA%B0%90%EC%86%8C)  

채팅창 열고 닫을 때마다 파일 조회 → **ConcurrentHashMap 캐싱**으로 변경

| 방식 | 조회 시간 |
|:---:|:---:|
| 파일 조회 | 2.380 ± 0.054 ms |
| **맵 조회** | **6.214 ± 0.207 ns** |

맵 조회는 `get` 한 번이라 나노초 단위이고 파일 조회는 파일 열기 + 역방향 탐색 + JSON 파싱이 들어가서 밀리초 단위다.

단위 자체가 다르기 때문에 몇 배라고 말하기보다 **디스크 I/O를 제거했다**고 보는 것이 맞다.

<br>

### RabbitMQ Broker 도입

In-Memory Broker의 용량 제한·메시지 유실·모니터링 문제를 해결하기 위해 **외부 메시지 브로커 RabbitMQ** 적용

STOMP 프로토콜을 그대로 지원해서 클라이언트 코드는 두고 브로커만 분리할 수 있었다.

<br>

### [Redis Cache로 DB 조회 횟수 감소](https://haedal-uni.github.io/posts/%EC%84%B1%EB%8A%A5-%EC%B5%9C%EC%A0%81%ED%99%94-%EC%9A%94%EC%95%BD/#4-db-%EC%A1%B0%ED%9A%8C-%ED%9A%9F%EC%88%98-%EA%B0%90%EC%86%8C)  

- 전체 값 캐싱: `@Cacheable` 어노테이션 사용

- 일부 값 캐싱: `RedisTemplate` 직접 사용

방 정보를 매번 DB에서 조회하던 것을 Redis 캐시로 대체했다.

<br>

| 방식 | 처리량 |
|:---:|:---:|
| DB 조회 | 624.8 ± 50.7 ops/s |
| **Redis 조회** | **1,265.3 ± 31.5 ops/s** |

처리량이 약 **2배** 올라갔고 그만큼 DB 부하도 줄었다.

<br>

### 종합 : 단계별 조회 시간

세 최적화가 각각 다른 대상을 재기 때문에

**"채팅창 열기 1회 = 마지막 메시지 조회 + 방 정보 조회"** 라는 같은 작업에 캐시를 한 단계씩 누적 적용해서 측정했다.

| 설정 | 조회 시간 | 캐시 없음 대비 |
|:---:|:---:|:---:|
| 캐시 없음 (파일 + DB) | 4.610 ± 0.292 ms | — |
| Map 적용 (맵 + DB) | 2.032 ± 0.398 ms | 2.3배 |
| **Map + Redis** | **1.198 ± 0.181 ms** | **3.8배** |

<br>

Map만 적용하면 마지막 메시지의 파일 I/O만 제거되고 방 정보는 여전히 DB에서 읽기 때문에 병목이 남는다 (4.61 → 2.03ms)

Redis까지 적용하면 방 정보 조회도 캐시로 대체되면서 4.61 → 1.20ms까지 줄어든다

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
