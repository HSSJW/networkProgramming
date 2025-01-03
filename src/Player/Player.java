package Player;

import Map.MapData;
import Player.Skills.AbstractSkill.ESkill;
import Player.Skills.AbstractSkill.QSkill;
import Player.Skills.AbstractSkill.WSkill;

import java.awt.*;
import java.awt.geom.AffineTransform;

public abstract class Player {
    protected int id; // 플레이어 ID
    protected int x, y; // 위치
    protected int width, height; // 크기
    protected int verticalSpeed = 0; // 수직 속도
    protected final int GRAVITY = 2; // 중력
    protected final int JUMP_STRENGTH = -20; // 점프 힘
    protected boolean onGround = false; // 바닥 여부
    protected boolean facingRight = true; // 방향
    protected boolean moving = false; // 이동 여부
    protected String currentState = "idle"; // 현재 상태

    protected Image standRightImage, standLeftImage;
    protected Image leftImage, rightImage;
    protected Image jumpRightImage, jumpLeftImage;
    protected Image hitRightImage, hitLeftImage;
    protected Image currentImage; // 현재 상태의 이미지


    //스킬관련
    protected QSkill qSkill;
    protected WSkill wSkill;
    protected ESkill eSkill;


    public Player(int id, int startX, int startY) {
        this.id = id;
        this.x = startX;
        this.y = startY;

        initializeImages(); // 각 플레이어별 이미지 초기화
        initializeSkills(); // 스킬 초기화 추가

        this.currentImage = standRightImage;
        this.width = currentImage.getWidth(null);
        this.height = currentImage.getHeight(null);

    }

    // 각 플레이어별 이미지를 초기화하는 추상 메서드
    protected abstract void initializeImages();
    // 플레이어 그리기


    public void draw(Graphics2D g2d, Component observer) {
        // 캐릭터 그리기
        g2d.drawImage(currentImage, x, y, width, height, observer);

        // 스킬 상태 확인 및 그리기
        if (qSkill != null) {
            if (qSkill.isActive()) {

                qSkill.draw(g2d, observer);
            }
        }

        // 다른 스킬들도 마찬가지로 처리
        if (wSkill != null && wSkill.isActive()) {
            wSkill.draw(g2d, observer);
        }
        if (eSkill != null && eSkill.isActive()) {
            eSkill.draw(g2d, observer);
        }
    }


    public void updateSkills() {
        if (qSkill != null && qSkill.isActive()) qSkill.update();
        if (wSkill != null && wSkill.isActive()) wSkill.update();
        if (eSkill != null && eSkill.isActive()) eSkill.update();
    }

// 스킬 초기화를 위한 추상 메서드
    protected abstract void initializeSkills();

    // 스킬 사용 메서드
    public void useQSkill() {
        if (qSkill.canUse()) {
            System.out.println("Q스킬 실행");
            qSkill.activate(facingRight);
            setState("skill_q");
        }
    }

    public void useWSkill() {
        if (wSkill.canUse()) {
            wSkill.activate(facingRight);
            setState("skill_w");
        }
    }

    public void useESkill() {
        if (eSkill.canUse()) {
            eSkill.activate(facingRight);
            setState("skill_e");
        }
    }

    //보고있는 방향 리턴 true >> 오른족 false >> 왼쪽
    public boolean isFacingRight() {
        return facingRight;
    }

    // 플레이어 이동
    public void moveLeft(int speed) {
        x -= speed;
        facingRight = false; // 왼쪽 방향 설정
        moving = true; // 이동 중
        if (!currentState.equals("jump")) { // 점프 중이 아니면 이동 이미지
            currentState = "left";
            currentImage = leftImage;
        }
    }

    public void moveRight(int speed) {
        x += speed;
        facingRight = true; // 오른쪽 방향 설정
        moving = true; // 이동 중
        if (!currentState.equals("jump")) { // 점프 중이 아니면 이동 이미지
            currentState = "right";
            currentImage = rightImage;
        }
    }

    public void jump() {
        if (onGround) { // 바닥에서만 점프 가능
            verticalSpeed = JUMP_STRENGTH; // 점프 힘 설정
            onGround = false; // 점프 상태로 변경
            currentState = "jump"; // 상태 변경
            currentImage = facingRight ? jumpRightImage : jumpLeftImage; // 방향에 따른 점프 이미지
        }
    }

    public void takeHit() {
        currentState = "hit"; // 상태 변경
        currentImage = facingRight ? hitRightImage : hitLeftImage; // 방향에 따른 피격 이미지
    }

    public void stopMoving() {
        currentState = "idle";
        currentImage = facingRight ? standRightImage : standLeftImage; // 방향에 따른 서 있는 이미지

    }

    public void update(MapData currentMap, int mapWidth, int mapHeight) {
        Rectangle[] ground = currentMap.getTerrain().toArray(new Rectangle[0]);

        // 중력 적용
        if(verticalSpeed <= 25)  //지형 뚫고 내려가는 것을 방지하기위한 수직가속도 제한 설정
            verticalSpeed += GRAVITY;

        // 위치 업데이트
        y += verticalSpeed;

        // 바닥 충돌 감지
        boolean wasOnGround = onGround;
        onGround = false;

        // 가장 가까운 지형을 추적
        Rectangle closestGround = null;

        for (Rectangle rect : ground) {
            // 여백 보정을 위한 오프셋 추가
            int bottomOffset = 0;
            int maxStepHeight = 20; // 최대로 올라갈 수 있는 높이 제한

            // 플레이어의 발 위치와 지형의 높이 차이 계산
            int heightDifference = (y + height) - rect.y;

            // 플레이어가 지형 위에 있고, 좌우로 겹치는지 확인
            if (x + width - 20 > rect.x && x + 20 < rect.x + rect.width) {
                // 높이 차이가 허용 범위 내인 경우에만 처리
                if (Math.abs(heightDifference) <= maxStepHeight) {
                    // 플레이어가 지형보다 위에 있고 떨어지는 중인 경우
                    if (y + height >= rect.y && y + height - verticalSpeed <= rect.y) {
                        if (closestGround == null || rect.y < closestGround.y) {
                            closestGround = rect;
                        }
                    }
                    // 플레이어가 지형보다 약간 아래에 있는 경우 (계단 오르기)
                    else if (heightDifference > 0 && heightDifference <= maxStepHeight && verticalSpeed >= 0) {
                        if (closestGround == null || rect.y < closestGround.y) {
                            closestGround = rect;
                        }
                    }
                }
            }
        }

        // 가장 가까운 지형과 충돌 처리
        if (closestGround != null) {
            onGround = true;
            y = closestGround.y - height;
            verticalSpeed = 0;

            if (currentState.equals("jump")) {
                stopMoving();
            }
        }

        // 착지 시 idle 상태로 전환
        if (onGround && !wasOnGround && !moving) {
            stopMoving();
        }

        // 맵 경계 내로 제한
        x = Math.max(50, Math.min(mapWidth - width + 100, x));
        y = Math.min(mapHeight - height + 150, y);

        // 이동 상태가 없으면 idle로 복구
        if (!moving && onGround) {
            stopMoving();
        }

        // 이동 상태 초기화
        moving = false;
    }




    // Getters and Setters
    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setState(String state) {
        currentState = state;
        switch (state) {
            case "left":
                currentImage = leftImage;
                break;
            case "right":
                currentImage = rightImage;
                break;
            case "jump":
                currentImage = facingRight ? jumpRightImage : jumpLeftImage;
                break;
            case "hit":
                currentImage = facingRight ? hitRightImage : hitLeftImage;
                break;
            default: // "idle"
                currentImage = facingRight ? standRightImage : standLeftImage;
                break;
        }
    }
    public String getCurrentState() {
        return currentState;
    }


    public boolean isOnGround() {
        return onGround;
    }

    public void setImage(Image image) {
        this.currentImage = image;
    }

    // Skill getters
    public QSkill getQSkill() {
        return qSkill;
    }

    public WSkill getWSkill() {
        return wSkill;
    }

    public ESkill getESkill() {
        return eSkill;
    }
}
