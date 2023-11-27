package study.datajpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id","name"})
public class Team  extends  JpaBaseEntity{

    @Id
    @GeneratedValue
    @Column(name ="team_id")
    private Long id;
    private String name;

    //일대다 관계
    @OneToMany(mappedBy = "team")
    //연관관계 상대방에 자신이 매핑되어 있는 필드명
    //foreign키가 없는 쪽에 기재하는걸 권장함

    private List<Member> members = new ArrayList<>();

    public Team(String name){
        this.name = name;
    }
}
