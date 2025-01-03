package Map;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.List;

public class Map2 extends MapData {
    @Override
    protected void initializeMap() {
        this.baseImagePath = "images/map/baseImage/map2_base.png";
        this.backgroundImagePath = "images/map/KerningCity.png";
        this.backgroundMusicPath = "SoundTrack/KerningCity_background.wav";

        this.backgroundYOffset = -20;

        // 기본 지형 생성
        List<Rectangle> terrainList = new ArrayList<>();

        // 메인 플랫폼 추가
        terrainList.add(new Rectangle(0, 650, 1400, 20));    // 1층 메인
        terrainList.add(new Rectangle(100, 480, 1140, 20));  // 2층 메인
        terrainList.add(new Rectangle(100, 300, 1140, 20));  // 3층 메인
        terrainList.add(new Rectangle(280, 117, 780, 20));  // 4층 메인

        // 여러 계단 생성 (x 좌표만 변경하여 추가)
        terrainList.addAll(createStairs(235, 630));  // 1-1 계단
        terrainList.addAll(createStairs(740, 630));  // 1-2 계단
        terrainList.addAll(createStairs(570, 460)); //  2층 계단
        terrainList.add(new Rectangle(815,305, 20, 20)); //2층 계단 추가
        terrainList.addAll(createStairs(570, 280));  // 3층 계단
        terrainList.add(new Rectangle(815,125, 20, 20)); //3층 계단 추가
        this.terrain = new CopyOnWriteArrayList<>(terrainList);

        // 포탈 데이터 초기화
        this.portals = new CopyOnWriteArrayList<>(
                List.of(
                        new Portal(50, 500, 0, 1250, 460),
                        new Portal(1300, 500, 2, 700, 400)
                )
        );
    }

    private List<Rectangle> createStairs(int startX, int startY) {
        List<Rectangle> stairs = new ArrayList<>();

        // 초기 계단 블록
        for (int i = 0; i < 4; i++) {
            stairs.add(new Rectangle(
                    startX + (i * 12),      // x 좌표
                    startY - (i * 10),      // y 좌표
                    20,                     // 너비
                    20                      // 높이
            ));
        }

        // 가로 판자
        stairs.add(new Rectangle(startX + 48, startY - 40, 80, 20));

        // 나머지 계단 블록
        for (int i = 0; i < 10; i++) {
            stairs.add(new Rectangle(
                    startX + 128 + (i * 12),    // x 좌표
                    startY - 50 - (i * 10),     // y 좌표
                    20,                         // 너비
                    20                          // 높이
            ));
        }

        return stairs;
    }
}