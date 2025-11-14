package org.newdawn.spaceinvaders.singleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.newdawn.spaceinvaders.enums.GameObjectType;
import org.newdawn.spaceinvaders.enums.SectionType;
import org.newdawn.spaceinvaders.enums.SpecialCommandType;
import org.newdawn.spaceinvaders.map_load.SectionData;
import org.newdawn.spaceinvaders.map_load.map_load_commands.InstantiateCommand;
import org.newdawn.spaceinvaders.map_load.map_load_commands.MapLoadCommand;
import org.newdawn.spaceinvaders.map_load.map_load_commands.SectionCommand;
import org.newdawn.spaceinvaders.map_load.map_load_commands.SpecialCommand;

public class MapDataParser {
    private final static MapDataParser INSTANCE = new MapDataParser();
    public static MapDataParser getInstance()
    {
        return INSTANCE;
    }

    public Queue<SectionData> parseMapData(String plainData){
        Queue<MapLoadCommand> commands = new LinkedList<>();
        String[] plainFile = Arrays.stream(plainData.trim().split("\n"))
                            .map(String::toLowerCase) // 모든 영문자를 소문자로 바꿈
                            .map(String::trim)
                            .filter(s -> !s.isBlank()) // 빈 줄 제거
                            .toArray(String[]::new);
                            
        for (String plainCommand : plainFile){
            MapLoadCommand command = null;

            if (plainCommand.startsWith(">")){ // section command로 판단
                command = parseSectionCommand(plainCommand);
            }
            else if (plainCommand.startsWith(":")){ // special command로 판단
                command = parseSpecialCommand(plainCommand);
            }
            else if (plainCommand.startsWith("/")){
                continue; // 맵의 메타데이터를 의미함. 구현 보류
            }
            else{ // command 맨 앞에 특정한 표시자가 없으면 instantiate command로 판단
                command = parseInstantiateCommand(plainCommand);
            }
            
            commands.add(command);
        }

        SectionCommand currentSectionCommand = null;
        //TODO 흠 이런 구조에서는 현재 SpeicalCommand가 의미가 없어짐
        Queue<InstantiateCommand> currentInstantiateCommands = null;
        Queue<SectionData> sections = new LinkedList<>();

        while (!commands.isEmpty()){    
            MapLoadCommand currentCommand = commands.poll();

            if (currentCommand instanceof SectionCommand){
                if (currentSectionCommand != null) {
                    sections.add(new SectionData(currentSectionCommand, currentInstantiateCommands));
                }
                currentSectionCommand = (SectionCommand)currentCommand;
                currentInstantiateCommands = new LinkedList<>();
            }
            else if (currentCommand instanceof InstantiateCommand){
                currentInstantiateCommands.add((InstantiateCommand)currentCommand);
            }
            else if (currentCommand instanceof SpecialCommand){
                if (((SpecialCommand)currentCommand).getSpecialCommandType() == SpecialCommandType.GAME_END){
                    sections.add(new SectionData(currentSectionCommand, currentInstantiateCommands));
                    break;
                }
            }
        }
        
        return sections;
    }

    private MapLoadCommand parseInstantiateCommand(String plainData){
        String[] attributes = plainData.split("\\s+");

        if (attributes.length < 5) { throw new RuntimeException("생성 커맨드의 필수 속성값이 빠졌습니다."); }

        try {
            long instantiateTime = Long.parseLong(attributes[0]) << 16;
            long instantiateX = Long.parseLong(attributes[1]) << 16;
            long instantiateY = Long.parseLong(attributes[2]) << 16;
            GameObjectType gameObjectType = GameObjectType.fromValue(attributes[3]);
            int gameObjectName = Integer.parseInt(attributes[4]);

            ArrayList<String> extra = new ArrayList<>();
            for (int i = 5; i < attributes.length; i++){
                extra.add(attributes[i]);
            }

            return new InstantiateCommand(instantiateTime, instantiateX, instantiateY, gameObjectType, gameObjectName, extra);
        } catch (NumberFormatException  e) {
            throw new NumberFormatException("'생성 시간', '생성 x좌표', '생성 y좌표', '게임 오브젝트 Id' 속성 중 하나가 숫자가 아닙니다.");
        }
    }

    private MapLoadCommand parseSectionCommand(String plainData){
        String commandType = plainData.substring(1);

        return new SectionCommand(SectionType.fromValue(commandType));
    }

    private MapLoadCommand parseSpecialCommand(String plainData){
        String commandType = plainData.substring(1);

        return new SpecialCommand(SpecialCommandType.fromValue(commandType));
    }

    public static void main(String[] args) {
        Path filePath = Paths.get("src/main/resources/maps/map1.map"); // 파일 경로

        try {
            String content = Files.readString(filePath); // 파일 전체를 String으로 읽음
            new MapDataParser().parseMapData(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
