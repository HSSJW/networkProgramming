package Player.Skills.AbstractSkill;

import Player.Player;
import Player.Skills.Skill;

import javax.swing.*;
import java.awt.*;

public abstract class QSkill extends Skill {
    // 원본 이미지 크기에 맞게 수정
    protected static final int SKILL_WIDTH = 885;
    protected static final int SKILL_HEIGHT = 279;
    private static final int SKILL_DURATION = 2000; // 1초로 증가

    public QSkill(Player owner) {
        super("Q_Skill", 30, 2000, SKILL_DURATION, owner); // 쿨다운 2초, 지속시간 1초
        System.out.println("QSkill constructor called");
        loadSkillImages(); // 명시적으로 이미지 로드 호출
    }

    @Override
    public void activate(boolean facingRight) {
        if (canUse()) {
            this.facingRight = facingRight;
            isActive = true;
            lastUseTime = System.currentTimeMillis();
            // hitbox 초기화
            int hitboxX = facingRight ?
                    owner.getX() + owner.getWidth() :
                    owner.getX() - SKILL_WIDTH;
            hitbox = new Rectangle(hitboxX, owner.getY(), SKILL_WIDTH, SKILL_HEIGHT);
            System.out.println("Skill activated, will last for " + SKILL_DURATION + "ms");
        }
    }

    @Override
    public void update() {
        if (isActive) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastUseTime;

            System.out.println("Skill elapsed time: " + elapsedTime + "ms");

            // 스킬 지속 시간 체크
            if (elapsedTime >= duration) {
                isActive = false;
                System.out.println("Skill deactivated due to duration end");
                return;
            }

            // hitbox 업데이트
            int hitboxX = facingRight ?
                    owner.getX() + owner.getWidth() :
                    owner.getX() - SKILL_WIDTH;
            hitbox.setLocation(hitboxX, owner.getY());
        }
    }

    @Override
    public void draw(Graphics2D g2d, Component observer) {
        if (isActive && hitbox != null) {
            // 스킬 이미지 먼저 그리기
            Image currentGif = facingRight ? rightImage : leftImage;
            if (currentGif != null && currentGif != null) {
                System.out.println("Drawing gif image"); // 디버깅용
                g2d.drawImage(currentGif,
                        hitbox.x, hitbox.y,
                        SKILL_WIDTH, SKILL_HEIGHT,
                        observer);
            }

            // hitbox 시각화 (디버깅용)
            g2d.setColor(new Color(255, 0, 0, 30));  // 투명도를 더 낮춤
            g2d.fillRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);

            // hitbox 테두리 (디버깅용)
            g2d.setColor(new Color(255, 255, 255, 100));  // 투명한 흰색
            g2d.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
        }
    }

    protected boolean isGifPlaying() {
        ImageIcon currentGif = facingRight ? gifRight : gifLeft;
        return currentGif != null && currentGif.getImageObserver() != null;
    }

    @Override
    protected abstract void loadSkillImages();

}