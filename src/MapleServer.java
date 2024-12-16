import Map.MapData;
import Monster.*;

import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MapleServer {
    private final int port;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private ServerSocket serverSocket;
    private boolean isRunning = true;

    // 맵 관련
    private static CopyOnWriteArrayList<MapData> maps;
    private final MonsterManager monsterManager;
    private Timer monsterUpdateTimer;
    private int currentMapIndex = 0;

    static {
        maps = MapData.getMaps();
    }

    public MapleServer(int port) {
        this.port = port;
        this.monsterManager = new MonsterManager();
        monsterManager.initializeMonsters(currentMapIndex);

        // 몬스터 업데이트 타이머 설정
        monsterUpdateTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!clients.isEmpty()) {  // 클라이언트가 있을 때만 업데이트
                    monsterManager.updateMonsters(maps.get(currentMapIndex));
                    broadcastMonsterState();
                }
            }
        });
    }

    private synchronized void broadcastMonsterState() {
        try {
            String monsterState = createMonsterStateMessage();
            // 디버깅을 위한 로그 추가
            System.out.println("Broadcasting monster state: " + monsterState);

            for (ClientHandler client : clients) {
                if (client != null) {
                    client.send(monsterState);
                    // 각 메시지 사이에 약간의 지연 추가
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error broadcasting monster state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        // 몬스터 업데이트 타이머 시작
        monsterUpdateTimer.start();

        // 클라이언트 접속을 처리하는 스레드 시작
        new Thread(() -> {
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    if (clients.size() < 2) {  // 최대 2명까지만 접속 허용
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        clients.add(clientHandler);

                        // 새로운 클라이언트 접속 시 현재 몬스터 상태 즉시 전송
                        clientHandler.send(createMonsterStateMessage());

                        new Thread(clientHandler).start();
                        System.out.println("New client connected. Total clients: " + clients.size());
                    } else {
                        clientSocket.close();  // 추가 접속 거부
                    }
                } catch (IOException e) {
                    if (isRunning) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void stop() {
        isRunning = false;
        monsterUpdateTimer.stop();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            for (ClientHandler client : clients) {
                client.closeConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String createMonsterStateMessage() {
        StringBuilder message = new StringBuilder("MONSTER_UPDATE,");
        for (Monster monster : monsterManager.getMonsters()) {
            // 각 몬스터 데이터 사이에 명확한 구분자 추가
            message.append(String.format("%d,%d,%d,%d,%d,%b;",
                    monster.getX(),
                    monster.getY(),
                    monster.getHp(),
                    monster.getCurrentState().ordinal(),
                    monster.getCurrentDirection().ordinal(),
                    monster.isAlive()
            ));
        }
        // 마지막 세미콜론 제거
        if (message.charAt(message.length() - 1) == ';') {
            message.setLength(message.length() - 1);
        }
        return message.toString();
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private DataOutputStream output;
        private boolean isConnected = true;

        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
            this.output = new DataOutputStream(socket.getOutputStream());
        }

        public void run() {
            try (DataInputStream input = new DataInputStream(clientSocket.getInputStream())) {
                while (isConnected) {
                    String inputLine = input.readUTF();
                    if (inputLine.startsWith("MOVE")) {
                        for (ClientHandler client : clients) {
                            if (client != this) {
                                client.send(inputLine);
                            }
                        }
                    }
                    else if (inputLine.startsWith("HIT_MONSTER")) {
                        String[] data = inputLine.split(",");
                        int monsterId = Integer.parseInt(data[1]);
                        int damage = Integer.parseInt(data[2]);
                        monsterManager.handleMonsterHit(monsterId, damage);
                        broadcastMonsterState();
                    }
                    else if (inputLine.startsWith("MAP_CHANGE")) {
                        String[] data = inputLine.split(",");
                        currentMapIndex = Integer.parseInt(data[1]);
                        monsterManager.initializeMonsters(currentMapIndex);
                        broadcastMonsterState();
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + e.getMessage());
            } finally {
                closeConnection();
            }
        }

        public void send(String message) {
            try {
                if (isConnected && output != null) {
                    // 메시지 전송 전 synchronized 블록으로 보호
                    synchronized (output) {
                        output.writeUTF(message);
                        output.flush();

                        // 연속된 메시지 사이에 작은 지연 추가
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error sending message: " + message);
                e.printStackTrace();
                closeConnection();
            }
        }

        public void closeConnection() {
            try {
                isConnected = false;
                clients.remove(this);
                clientSocket.close();
                System.out.println("Client connection closed. Remaining clients: " + clients.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int port = 5000;
        MapleServer server = new MapleServer(port);
        try {
            server.start();

            // 종료 시그널 처리
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}