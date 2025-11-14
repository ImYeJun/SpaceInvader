package org.newdawn.spaceinvaders.map_load;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MapInfo {
    private String hash;
    private Path path;
    private String title;
    private String description;

    public MapInfo(Path path) {
        this.path = path;

        try {
            // 2. Files.readString() 메서드를 호출하여 파일 내용을 String으로 읽어옵니다.
            String content = Files.readString(path);

            hash = md5(content);
            title = "";
            description = "";

            String[] lines = content.split("\n");
            for(String l: lines){
                if(l.contains("/title")){
                    title = l.split(" ", 2)[1].trim();
                }
                else if(l.contains("/description")){
                    description = l.split(" ", 2)[1].trim().replace("\\n", "\n");
                }

                if(!title.isEmpty() && !description.isEmpty()){
                    break;
                }
            }

//                System.out.println(list.get(list.size()-1).toString());
        } catch (IOException e) {
            System.err.println("파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "MapInfo " + title + " " + path + " " + hash;
    }

    public String getHash() {
        return hash;
    }
    public Path getPath() {
        return path;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }

    public static String md5(String input) {
        try {
            // 1. MessageDigest 인스턴스를 "MD5" 알고리즘으로 생성합니다.
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 2. 해시할 문자열을 UTF-8 바이트 배열로 변환하여 업데이트합니다.
            md.update(input.getBytes(StandardCharsets.UTF_8));

            // 3. digest() 메서드를 호출하여 해시 계산을 완료하고, 결과를 바이트 배열로 받습니다.
            byte[] digest = md.digest();

            // 4. 바이트 배열을 16진수 문자열로 변환합니다.
            // BigInteger를 사용하면 변환 로직을 매우 간단하게 작성할 수 있습니다.
            String md5Hash = new BigInteger(1, digest).toString(16);

            // 5. 해시 결과가 32자리가 되도록 앞에 0을 채웁니다.
            while (md5Hash.length() < 32) {
                md5Hash = "0" + md5Hash;
            }

            return md5Hash;

        } catch (NoSuchAlgorithmException e) {
            // "MD5" 알고리즘이 지원되지 않는 경우 예외 처리
            // (일반적인 Java 환경에서는 발생하지 않습니다.)
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
