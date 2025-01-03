package Player.Skills.Player1_Skill;

import Player.Skills.AbstractSkill.ESkill;
import Player.Player;

import javax.swing.*;

// Player1 E스킬
public class Player1ESkill extends ESkill {
    public Player1ESkill(Player owner) {
        super(owner);
    }

    @Override
    protected void loadSkillImages() {

            // E스킬은 방향 구분 없이 하나의 이미지만 사용하므로 rightImage에만 할당
            rightImage = new ImageIcon("images/Player/Player1/skill/player1_e.gif").getImage();
            // 스킬 크기 설정
            updateSkillDimensions(rightImage);
    }
}
