@Slf4j
@RestController
public class SseController {
	private static final Map<String, SseEmitter> CLIENTS = new ConcurrentHashMap<>();
	private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
	@GetMapping("/api/subscribe")
	public SseEmitter subscribe(String id) {
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
		CLIENTS.put(id, emitter);
		emitter.onTimeout(() -> {
			log.info("onTimeout callback");
			//CLIENTS.remove(id);
			emitter.complete();
		});
		emitter.onCompletion(() -> CLIENTS.remove(id));
		return emitter;
	}

	@GetMapping("/api/publish")
	public void publish(String message) {
		Set<String> deadIds = new HashSet<>();
		CLIENTS.forEach((id, emitter) -> {
			try {
				emitter.send(message, MediaType.APPLICATION_JSON);
			} catch (Exception e) {
				e.getStackTrace();
				deadIds.add(id);
				log.warn("disconnected id : {}", id);
			}
		});

		deadIds.forEach(CLIENTS::remove);
	}
}