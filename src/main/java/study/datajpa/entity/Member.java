package study.datajpa.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//entity는 디폴트 생성자가 있어야 함.
//jpa에서 프록시 등의 경우에 사용해야 하므로 access level은 protected
@ToString(of = {"id","username","age"})
public class Member {

    @Id //식별자
    @GeneratedValue //순차적 값을 생성해서 넣어줌
    @Column(name="member_id")
    private Long id;
    private String username;
    private int age;

    //다대일관계
    @ManyToOne(fetch = FetchType.LAZY)
    //JPA에서 모든 연관관계는 LAY로 설정할것.
    //Eager(즉시로딩) 설정 시 성능최적화가 힘들다.
    @JoinColumn(name="team_id") //외래 키 매핑
    private Team team;



    public Member(String username) {
        this.username = username;
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if(team != null) {
            changeTeam(team);
        }
    }

    public Member(String username, int age) {
        this.username = username;
        this.age = age;
    }

    public void changeTeam(Team team){
        this.team = team;
        team.getMembers().add(this);
    }
}
