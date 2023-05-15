package com.dalcho.adme;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@BenchmarkMode(Mode.AverageTime)
//@BenchmarkMode : 벤치마크 결과를 나타내는 모드 (Mode.AverageTime으로 설정했으므로 평균 실행 시간이 결과로 나타남)
@OutputTimeUnit(TimeUnit.MILLISECONDS) // @OutputTimeUnit : 결과의 시간 단위를 설정
@State(Scope.Benchmark) // @State : 벤치마크할 상태
public class ReadFileBenchmark {
	private String chatUploadLocation;

	@Setup
	public void setup() {
		chatUploadLocation = "./src/main/resources/static/files/";
	}

	@Benchmark // 벤치마크 대상 메소드
	public Object readFileBefore() throws IOException, ParseException {
		String roomId = "3afa20e1-39a0-4237-be14-f2bdcfb75949";
		String str = Files.readString(Paths.get(chatUploadLocation + "/" + roomId + ".txt"));
		JSONParser parser = new JSONParser();
		return parser.parse("[" + str + "]");
	}

	@Benchmark // 벤치마크 대상 메소드
	public Object readFileAfter() throws IOException, ParseException {
		String roomId = "3afa20e1-39a0-4237-be14-f2bdcfb75949";
		List<String> lines = Files.lines(Paths.get(chatUploadLocation, roomId + ".txt")).collect(Collectors.toList());
		String jsonString = "[" + String.join(",", lines) + "]";
		JSONParser parser = new JSONParser();
		return parser.parse(jsonString);
	}
}

