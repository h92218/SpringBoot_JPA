package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.datajpa.entity.Member;

public interface MemberRepository extends JpaRepository<Member,Long> {
    //구현체는 스프링 데이터 JPA가 생성해준다.
}
