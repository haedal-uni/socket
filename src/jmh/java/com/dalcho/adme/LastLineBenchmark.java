package com.dalcho.adme;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.LastMessage;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class LastLineBenchmark {
    private Map<String, LastMessage> lastMessageMap;
    private Map<String, LastMessage> lastLineMap;
	private String chatUploadLocation;
    private String roomId = "0ad72ee0-ffb2-488c-897b-1db901d06a87";
	@Setup
	public void setup() {
		chatUploadLocation = "./src/main/resources/static/files/";
        lastMessageMap = new HashMap<>();
        lastLineMap = new HashMap<>();
        ChatMessage chatMessage = new ChatMessage();
        int adminChat = 0;
        int userChat=0;
        String day = "20";
        String time="3 : 07";
        LastMessage lastMessage = LastMessage.of(chatMessage, adminChat, userChat, day, time);
        lastLineMap.put(roomId, lastMessage);
	}
	@Benchmark
	public List<String> AtestOriginal() throws IOException {
		return lastLineOriginal(chatUploadLocation + "/" + roomId + ".txt");
	}

	@Benchmark
	public List<String> BtestImproved() throws IOException {
		return lastLineImproved(chatUploadLocation + "/" + roomId + ".txt");
	}

    @Benchmark
    public void DgetFileAftergetMap(){
		lastLine(roomId);
    }

    @Benchmark
    public LastMessage ConlyGetFile(){
        return getFile();
    }

	@Benchmark
	public LastMessage FonlyGetMap(){
		return getMap();
	}


    public LastMessage getMap() {
        return lastLineMap.get(roomId);
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

    public LastMessage getFile() {
        String filePath = chatUploadLocation + "/" + roomId + ".txt";
        File file = new File(filePath);
        // 파일의 존재 여부 확인
        if (!file.exists()) {
            try {
                // 파일이 존재하지 않는 경우 새로 생성
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            long fileLength = file.length();
            if (fileLength > 0) {
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
                    return null;
                }
                if (line.startsWith(",")) {
                    line = line.substring(1);
                }
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(line).getAsJsonObject();
                int adminChat = json.get("adminChat").getAsInt();
                int userChat = json.get("userChat").getAsInt();
                String message = json.get("message").getAsString().trim();
                String messages = new String(message.getBytes("iso-8859-1"), "utf-8");
                String day = json.get("day").getAsString();
                String time = json.get("time").getAsString();
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setRoomId(roomId);
                chatMessage.setMessage(messages);
                return LastMessage.of(chatMessage, adminChat, userChat, day, time);
            } else {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setRoomId(roomId);
                chatMessage.setMessage("");
                return LastMessage.of(chatMessage, 0, 0, "", "");
            }
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }

    }

	public LastMessage lastLine(String roomId) {
		if (lastMessageMap.containsKey(roomId)) {
			return lastMessageMap.get(roomId);
		} else {
			String filePath = chatUploadLocation + "/" + roomId + ".txt";
			File file = new File(filePath);
			// 파일의 존재 여부 확인
			if (!file.exists()) {
				try {
					// 파일이 존재하지 않는 경우 새로 생성
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
				long fileLength = file.length();
				if (fileLength > 0) {
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
						return null;
					}
					if (line.startsWith(",")) {
						line = line.substring(1);
					}
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(line).getAsJsonObject();
					int adminChat = json.get("adminChat").getAsInt();
					int userChat = json.get("userChat").getAsInt();
					String message = json.get("message").getAsString().trim();
					String messages = new String(message.getBytes("iso-8859-1"), "utf-8");
					String day = json.get("day").getAsString();
					String time = json.get("time").getAsString();
					ChatMessage chatMessage = new ChatMessage();
					chatMessage.setRoomId(roomId);
					chatMessage.setMessage(messages);
					lastMessageMap.put(roomId, LastMessage.of(chatMessage, adminChat, userChat, day, time));
					return LastMessage.of(chatMessage, adminChat, userChat, day, time);
				} else {
					ChatMessage chatMessage = new ChatMessage();
					chatMessage.setRoomId(roomId);
					chatMessage.setMessage("");
					lastMessageMap.put(roomId, LastMessage.of(chatMessage, 0, 0, "day", "time"));
					return LastMessage.of(chatMessage, 0, 0, "", "");
				}
			} catch (IOException | JsonSyntaxException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
