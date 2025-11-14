package org.newdawn.spaceinvaders.map_load;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapList {
    ArrayList<MapInfo> list = new ArrayList<>();
    public ArrayList<MapInfo> getList() {
        return list;
    }

    public MapList(){
        refresh();
    }

    public void refresh(){
        list.clear();

        // 맵 목록 읽어오기
        ArrayList<Path> pathList = new ArrayList<>();
        try {
            pathList.addAll(listFiles("maps"));
        } catch (Exception e) {
            System.err.println("리소스를 읽는 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
        //TODO 유저 커스텀 맵 목록 읽어와서 pathList에 추가하기

        // 맵 목록 처리
        for(Path path : pathList){
            list.add(new MapInfo(path));
        }
    }

    /**
     * IDE와 JAR 환경 모두에서 resources 폴더 내의 파일 목록을 가져옵니다.
     * @param path resources 폴더를 기준으로 한 내부 디렉터리 경로 (예: "maps")
     * @return 파일 이름 목록
     */
    private ArrayList<Path> listFiles(String path) throws IOException, URISyntaxException {
        // 1. ClassLoader를 통해 리소스의 URL을 가져옵니다.
        URL url = this.getClass().getClassLoader().getResource(path);
        if (url == null) {
            throw new IOException("리소스를 찾을 수 없습니다: " + path);
        }
        URI uri = url.toURI();

        Path myPath;

        // 2. URI 스킴을 확인하여 JAR 파일 내부인지, 일반 파일 시스템인지 명확히 구분합니다.
        if (uri.getScheme().equals("jar")) {
            // JAR 파일 환경일 경우
            FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            // JAR 파일 내의 경로를 가져옵니다.
            myPath = fileSystem.getPath(path);
        } else {
            // IDE 등 일반 파일 시스템 환경일 경우
            myPath = Paths.get(uri);
        }

        // 3. 얻어진 Path 객체를 사용하여 파일 목록을 스트림으로 처리합니다.
        //    이 부분은 두 환경 모두 동일하게 동작합니다.
        try (Stream<Path> stream = Files.list(myPath)) {
            return stream.filter(Files::isRegularFile).collect(Collectors.toCollection(ArrayList::new));
        }
    }
}
