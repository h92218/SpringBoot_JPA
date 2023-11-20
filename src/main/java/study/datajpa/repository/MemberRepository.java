package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    //구현체는 스프링 데이터 JPA가 생성해준다.

    //메서드 이름으로 쿼리를 생성해줌(메서드 이름 규칙 지켜야함)
    //필드명이 변경되면 인터페이스에 정의한 메서드 이름도 함게 변경해야 함.
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);


    //파라미터 바인딩 @Param
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    
    //dto 반환하기
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t ")
    List<MemberDto> findMemberDto();

    //콜렉션 파라미터 바인딩
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    //반환타입을 유연하게 쓸 수 있음
    List<Member> findListByUsername(String username); //컬렉션
    Member findMemberByUsername(String username); //단건

    Optional<Member> findOptionalMemberByUsername(String username); //단건 Optional

}
