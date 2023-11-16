package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Long> {
    //구현체는 스프링 데이터 JPA가 생성해준다.

    //메서드 이름으로 쿼리를 생성해줌(메서드 이름 규칙 지켜야함)
    //필드명이 변경되면 인터페이스에 정의한 메서드 이름도 함게 변경해야 함.
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

}
