package com.dalcho.adme;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class LastLineBenchmark {
	private String chatUploadLocation;

	@Setup
	public void setup() {
		chatUploadLocation = "./src/main/resources/static/files/";
	}
	@Benchmark
	public List<String> testOriginal() throws IOException {
		String roomId = "3afa20e1-39a0-4237-be14-f2bdcfb75949";
		return lastLineOriginal(chatUploadLocation + "/" + roomId + ".txt");
	}

	@Benchmark
	public List<String> testImproved() throws IOException {
		String roomId = "3afa20e1-39a0-4237-be14-f2bdcfb75949";
		return lastLineImproved(chatUploadLocation + "/" + roomId + ".txt");
	}

	@Benchmark
	public List<String> testUpdateJson() throws IOException{
		String roomId = "3afa20e1-39a0-4237-be14-f2bdcfb75949";
		return updateJson(chatUploadLocation + "/" + roomId + ".txt");
	}

	public List lastLineOriginal(String filepath) {
		try{
			RandomAccessFile file = new RandomAccessFile(filepath, "r");
			StringBuilder lastLine = new StringBuilder();
			int lineCount = 7;
			long fileLength = file.length();
			for (long pointer = fileLength - 1; pointer >= 0; pointer--) {
				file.seek(pointer);
				char c = (char) file.read();
				if (c == '\n') {
					lineCount--;
					if (lineCount == 0) {
						break;
					}
				}
				lastLine.insert(0, c);

			}

			StringTokenizer st = new StringTokenizer(lastLine.toString(), ",");
			String roomNum = st.nextToken().trim();
			String type = st.nextToken().trim();
			String sender = st.nextToken().trim();
			String msg = st.nextToken().trim();
			String admin = st.nextToken().trim();
			String user = StringUtils.removeEnd(st.nextToken().trim(), "}");

			String adminChat = admin.substring(admin.indexOf("adminChat")+12);
			String userChat = user.substring(user.indexOf("userChat")+11);
			String message = msg.substring(msg.indexOf("message")+10);
			String messages = new String(message.getBytes("iso-8859-1"), "utf-8");

			List<String> chat = new ArrayList<>();
			chat.add(adminChat.trim());
			chat.add(userChat.trim());
			chat.add(messages.trim());

			return chat;
			// 4. 결과 출력
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public List<String> lastLineImproved(String filepath) {
		try (RandomAccessFile file = new RandomAccessFile(filepath, "r")) {
			long fileLength = file.length();
			file.seek(fileLength);
			long pointer = fileLength - 2;
			while (pointer > 0) {
				file.seek(pointer);
				char c = (char) file.read();
				if (c == '\n') {
					break;
				}
				pointer--;
			}
			file.seek(pointer + 1);
			String line = file.readLine();
			if (line == null || line.trim().isEmpty()) {
				return Collections.emptyList();
			}
			if (line.startsWith(",")) {
				line = line.substring(1);
			}
			JSONObject json = new JSONObject(line);
			int adminChat = json.getInt("adminChat");
			int userChat = json.getInt("userChat");
			String message = json.getString("message").trim();
			String messages = new String(message.getBytes("iso-8859-1"), "utf-8");

			List<String> chat = new ArrayList<>();
			chat.add(Integer.toString(adminChat));
			chat.add(Integer.toString(userChat));
			chat.add(messages);
			return chat;

		} catch (IOException | JSONException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	public List<String> updateJson(String roomId) {
		File file = new File(roomId);
		// 파일의 존재 여부 확인
		if (!file.exists()) {
			try {
				// 파일이 존재하지 않는 경우 새로 생성
				file.createNewFile();
				return Collections.emptyList();
			} catch (IOException e) {
				e.printStackTrace();
				return Collections.emptyList();
			}
		}
		try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {

			long fileLength = file.length();
			if (fileLength <= 0) {
				return Collections.emptyList();
			}
			randomAccessFile.seek(fileLength);
			long pointer = fileLength - 2;
			while (pointer > 0) {
				randomAccessFile.seek(pointer);
				char c = (char) randomAccessFile.read();
				if (c == '\n') {
					break;
				}
				pointer--;
			}
			randomAccessFile.seek(pointer + 1);
			String line = randomAccessFile.readLine();
			if (line == null || line.trim().isEmpty()) {
				return Collections.emptyList();
			}
			if (line.startsWith(",")) {
				line = line.substring(1);
			}

			// Parsing JSON using com.google.gson.JsonObject
			JsonParser parser = new JsonParser();
			JsonObject json = parser.parse(line).getAsJsonObject();
			int adminChat = json.get("adminChat").getAsInt();
			int userChat = json.get("userChat").getAsInt();
			String message = json.get("message").getAsString().trim();
			String messages = new String(message.getBytes("iso-8859-1"), "utf-8");

			List<String> chat = new ArrayList<>();
			chat.add(Integer.toString(adminChat));
			chat.add(Integer.toString(userChat));
			chat.add(messages);
			return chat;
		} catch (IOException | JsonSyntaxException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
}
