package com.dalcho.adme;

import com.dalcho.adme.dto.ChatMessage;
import com.dalcho.adme.dto.LastMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5)
public class FileRead {
    private Map<String, LastMessage> lastMessageMap;
    private String chatUploadLocation;
    private String roomId = "0ad72ee0-ffb2-488c-897b-1db901d06a87";

    @Setup
    public void setup() throws IOException{
        lastMessageMap = new HashMap<>();
        ChatMessage chatMessage = new ChatMessage();
        int adminChat = 0;
        int userChat=0;
        String day = "20";
        String time="3 : 07";
        LastMessage lastMessage = LastMessage.of(chatMessage, adminChat, userChat, day, time);
        lastMessageMap.put(roomId, lastMessage);
        chatUploadLocation = "./src/main/resources/static/files/";
    }
    @Benchmark
    public LastMessage getMap() {
        return lastMessageMap.get(roomId);
    }

    @Benchmark
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
}
