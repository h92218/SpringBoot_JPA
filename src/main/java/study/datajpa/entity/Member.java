package study.datajpa.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class Member {

    @Id //식별자
    @GeneratedValue //순차적 값을 생성해서 넣어줌
    private Long id;
    private String username;

    //entity는 디폴트 생성자가 있어야 함. access level은 protected로.
    //jpa에서 프록시 등의 경우에 사용해야 함.
    protected Member(){

    }
    public Member(String username) {
        this.username = username;
    }
}
