package org.newdawn.spaceinvaders.loop;

import networking.Network;
import networking.rudp.Connection;
import networking.rudp.IRUDPPeerListener;
import networking.rudp.PacketData.*;
import networking.rudp.RUDPPeer;
import org.newdawn.spaceinvaders.Game;
import org.newdawn.spaceinvaders.enums.GameLoopResultType;
import org.newdawn.spaceinvaders.loop.game_loop.IGameLoopGameResultListener;
import org.newdawn.spaceinvaders.loop_input.LoopInput;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//플레이어 루프
//	- 진입 후 수신이든 발신이든 모든 통신을 시작하기까지 1초정도 대기시간 둬야할 듯.
//클라: 입력할 때마다 server제외 입력 브로드캐스팅, 게임 끝나면 server에게 결과 전송, 게임 끝나고 나갈 때 server제외 disconnect
//서버:

import javax.swing.JFileChooser;

import org.newdawn.spaceinvaders.loop_input.LoopInputLog;
import org.newdawn.spaceinvaders.loop.game_loop.GameLoopSnapshot;
import serializer.GameLoopSerializer;

//플레이어 루프
//	- 진입 후 수신이든 발신이든 모든 통신을 시작하기까지 1초정도 대기시간 둬야할 듯.
//클라: 입력할 때마다 server제외 입력 브로드캐스팅, 게임 끝나면 server에게 결과 전송, 게임 끝나고 나갈 때 server제외 disconnect
//서버:

public class GameLoopPlayerLoop extends Loop implements IGameLoopGameResultListener {
    GameLoop gameLoop;
    boolean gameStarted = false;
    public boolean isPlaying(){
        return gameLoop != null && gameStarted;
    }

    // -1이면 현재 롤백할 필요가 없다는 의미
    //  그렇지 않다면 해당 프레임으로 롤백을 해야한다는 의미
    long rollbackTargetFrame = -1L;

    Map<Long, GameLoopSnapshot> snapshots = new HashMap<>();
    LoopInputLog loopInputLog = new LoopInputLog();

    PacketDataS2CPreprocessForGame preprocessInfo = null;

    public GameLoopPlayerLoop(Game game){
        super(game);
    }

    @Override
    public void onExitLoop(){
        super.onExitLoop();

        //루프 나갈 때 서버 제외 모든 피어와 연결 끊기
        try {
            getGame().getRudpPeer().disconnectAll("server");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.gc();
    }

    @Override
    public void onGameResultChanged(GameLoopResultType gameResult) {
        gameResult();
    }

    @Override
    public void process(ArrayList<LoopInput> inputs) {
        super.process(inputs);

        // 통신(processReceivedData())은 게임이 시작되기 전에도 수행되어야 하니 여기 위치 시킴
        getGame().getRudpPeer().processReceivedData();



        // 이하는 게임이 시작된 이후에만 실행되는 동작들
        if(!isPlaying()){return;}

        if(inputs != null){
            for (LoopInput loopInput : inputs) {
                // 이번 프레임에 들어온 입력의 playerID 수정
                loopInput.playerID = gameLoop.myPlayerID;
            }

            // 이번 프레임에 들어온 입력을 모든 피어에게 브로드캐스트
            if(!inputs.isEmpty()){
                loopInputLog.inputFrame = gameLoop.currentFrame;
                loopInputLog.inputs = inputs;
                p2pInput(loopInputLog.toSaveData());
            }
        }


        // 게임 종료 시
        if(gameLoop.getGameResult() != GameLoopResultType.IN_GAME){
            if(isKeyInputJustPressed(0, "escape")) {
                getGame().changeLoop(new LobbyLoop(getGame()));
            }
            else if(isKeyInputJustPressed(0, "record")) {
                String data = gameLoop.getReplayData();

                // JFileChooser 객체 생성
                JFileChooser chooser = new JFileChooser();

                // 파일 저장 다이얼로그를 현재 프레임(this) 중앙에 띄움
                int result = chooser.showSaveDialog(getGame());

                // 사용자가 '저장' 버튼을 눌렀는지 확인
                if (result == JFileChooser.APPROVE_OPTION) {
                    // 저장할 파일 경로를 File 객체로 받아옴
                    File fileToSave = chooser.getSelectedFile();

                    String filePath = fileToSave.getAbsolutePath(); // 저장할 파일 경로

                    if (!filePath.endsWith(".txt")) {
                        fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
                        filePath = fileToSave.getAbsolutePath();
                    }

                    try {
                        // Path 객체 생성
                        Path path = Paths.get(filePath);

                        // 파일에 문자열 쓰기 (기본 인코딩은 UTF-8)
                        Files.writeString(path, data);

                        System.out.println("파일 저장 성공");

                    } catch (IOException e) {
                        System.err.println("파일 저장 중 오류가 발생");
                        e.printStackTrace();
                    }
                }

                // 리플레이 저장 즉시 메인메뉴로 사출
                getGame().changeLoop(new LobbyLoop(getGame()));
            }
        }



        long currentFrame = gameLoop.currentFrame;

        //현재 프레임의 자신의 입력을 스냅샷에 저장
        putInputs(currentFrame, inputs);
        
        // 롤백 필요시
        if(rollbackTargetFrame > -1){
            gameLoop = GameLoopSerializer.getInstance().deserialize(snapshots.get(rollbackTargetFrame).state);;
            gameLoop.setGame(getGame());
            gameLoop.gameResultListener = this;

            rollbackTargetFrame = -1;
        }

        // currentFrame + 1까지 돌림
        while(gameLoop.currentFrame < currentFrame + 1){
            gameLoop.process(snapshots.get(gameLoop.currentFrame).inputs);

            //스냅샷 state 저장
            putState(gameLoop.currentFrame, GameLoopSerializer.getInstance().serialize(gameLoop));
        }

        // 너무 오래된 스냅샷 청소
        snapshots.keySet().removeIf(frame -> frame < currentFrame - 60L * 10);  // 60 = 1초
    }


    @Override
    public void draw(Graphics2D g) {
        if(isPlaying()) gameLoop.draw(g);

        super.draw(g);

        if (isPlaying() && gameLoop.getGameResult() != GameLoopResultType.IN_GAME) {
            Font font = g.getFont();
            g.setFont(new Font(font.getFontName(), Font.BOLD, 20));

            String message = "";
            if (gameLoop.getGameResult() == GameLoopResultType.WIN) {
                message = "Well done! You Win!";
                g.setColor(Color.yellow);
            }
            else if (gameLoop.getGameResult() == GameLoopResultType.LOSE) {
                message = "Oh no! They got you...";
                g.setColor(Color.red);
            }
            g.drawString(message,(800-g.getFontMetrics().stringWidth(message))/2,250);

            message = "Press ESC to exit";
            g.setColor(Color.white);
            g.drawString(message,(800-g.getFontMetrics().stringWidth(message))/2,300);
            message = "Press R to save Replay";
            g.setColor(Color.white);
            g.drawString(message,(800-g.getFontMetrics().stringWidth(message))/2,325);
        }
    }

    @Override
    protected IRUDPPeerListener generateIRUDPPeerListener() {
        final GameLoopPlayerLoop thisLoop = this;
        return new  IRUDPPeerListener() {
            @Override
            public boolean onConnected(RUDPPeer peer, Connection connection) {
                return false;
            }

            @Override
            public boolean onDisconnected(RUDPPeer peer, Connection connection) {
                if (connection.getAddress().getAddress().getHostAddress().equals(Network.SERVER_IP)) {
                    System.out.println(connection.getAddress().getAddress().getHostAddress() + " disconnected");
                    System.exit(0);
                }
                return true;
            }

            @Override
            public boolean onReceived(RUDPPeer peer, Connection connection, PacketData data) {
                if (data instanceof PacketDataS2CPreprocessForGame) {
                    // 게임 생성
                    //멀티플레이 정보에 따라서 시드, 플레이어 카운트, 마이 플레이어 아이디, 맵데이터 넣어줘야함
                    preprocessInfo =  (PacketDataS2CPreprocessForGame) data;
                    gameLoop = new GameLoop(getGame(),
                            preprocessInfo.gameLoopSeed,
                            preprocessInfo.playersUID.size(),
                            preprocessInfo.playerIDInLobby,
                            preprocessInfo.mapID);
                    gameLoop.gameResultListener = thisLoop;

                    putState(gameLoop.currentFrame, GameLoopSerializer.getInstance().serialize(gameLoop));

                    // 연결할 주소들 리스트 업
                    ArrayList<InetSocketAddress> peerAddresses = new ArrayList<>();
                    for(int i = 0; preprocessInfo.playersUID.size() > i; i++){
                        if(i == gameLoop.getMyPlayerID())continue;

                        InetSocketAddress address = new InetSocketAddress(preprocessInfo.addresses.get(i), preprocessInfo.ports.get(i));
                        peerAddresses.add(address);
                    }

                    while(!peerAddresses.isEmpty()){
                        try {
                            // 연결 된 피어는 목록에서 제거
                            for(int i = peerAddresses.size() - 1; i >= 0; i--){
                                if(getGame().getRudpPeer().isConnected(peerAddresses.get(i))){
                                    peerAddresses.remove(i);
                                }
                            }

                            System.out.println("=== 피어 연결 시도 ====");
                            for(InetSocketAddress address : peerAddresses){

                                System.out.println("연결 시도 - " + address.getAddress().getHostAddress() + ":" + address.getPort());
                                // 한 번 시도할 때마다 5번씩 연결 요청
                                for(int k = 0; k < 5; k++){
                                    Thread.sleep(10);
                                    try {
                                        getGame().getRudpPeer().connect(address);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }

                            // 1초에 한 번씩 시도
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    //전처리 완료 전송
                    preprocessOK();
                    System.out.println("전처리 완료. 세션ID : " + preprocessInfo.gameSessionID);

                    return true;
                }
                else if (data instanceof PacketDataS2CStartGame) {
                    gameStarted = true;
                    return true;
                }
                else if (data instanceof PacketDataP2PInput) {
                    // 받은 데이터 파싱
                    loopInputLog.setFromSaveData(((PacketDataP2PInput) data).inputLog);

                    // 받은 데이터의 입력 프레임
                    long frame = loopInputLog.inputFrame;

                    // 받은 입력 데이터를 스냅샷에 추가
                    putInputs(frame, loopInputLog.inputs);

                    // 현재 프레임보다 더 과거의 입력이라면 rollbackTargetFrame을 설정하여 롤백이 필요함을 알림
                    if(frame < gameLoop.currentFrame)
                        if(rollbackTargetFrame == -1L || frame < rollbackTargetFrame){
                            rollbackTargetFrame = frame;
                        }
                    return true;
                }
                return false;
            }
        };
    }

    void preprocessOK(){
        try {
            getGame().getRudpPeer().broadcastAboutTag("server", new PacketDataC2SPreprocessOK());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    void p2pInput(String inputLogString) {
        try {
            PacketDataP2PInput packetData = new PacketDataP2PInput();

            packetData.inputLog = inputLogString;

            getGame().getRudpPeer().broadcast(packetData, "server");
        } catch (Exception e) {
            throw new RuntimeException(inputLogString, e);
        }
    }
    void gameResult(){
        if(gameLoop.getGameResult() == GameLoopResultType.IN_GAME){
            System.err.println("아직 게임이 끝나지 않은 상태에서는 게임 결과를 서버로 전송할 수 없음");
            return;
        }

        // 서버에 게임 종료 알림
        try {
            getGame().getRudpPeer().broadcastAboutTag("server", new PacketDataC2SGameResult(
                    gameLoop.getScore(), gameLoop.getGameResult() ==  GameLoopResultType.WIN
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 게임 클리어 시 파이어베이스에 저장
        if(gameLoop.getMyPlayerID() == 0 && gameLoop.getGameResult() ==  GameLoopResultType.WIN){
            try {
                getGame().firebaseRankings.saveGameResult(
                        getGame().authToken,
                        preprocessInfo.gameSessionID,
                        preprocessInfo.mapID,
                        gameLoop.getScore(),
                        preprocessInfo.playersUID
                        );
            } catch (IOException e) {
                throw new RuntimeException("Firebase 저장 실패", e);
            }
        }
    }



    // 해당 프레임의 스냅샷에 입력 데이터 추가
    void putInputs(long frame, ArrayList<LoopInput> inputs){
        GameLoopSnapshot snapshot = snapshots.getOrDefault(frame, new GameLoopSnapshot());
        if(inputs != null && !inputs.isEmpty()){
            snapshot.inputs.addAll(inputs);
        }
        snapshots.put(frame, snapshot);
    }
    // 해당 프레임의 스냅샷에 게임 상태 데이터 추가
    void putState(long frame, byte[] state){
        GameLoopSnapshot snapshot = snapshots.getOrDefault(frame, new GameLoopSnapshot());
        snapshot.state = state;
        snapshots.put(frame, snapshot);
    }
}
