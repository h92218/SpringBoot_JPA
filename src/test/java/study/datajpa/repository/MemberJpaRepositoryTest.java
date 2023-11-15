package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
/*
transaction : 데이터베이스의 상태를 변화시키기 위해서 수행하는 작업의 단위
@Transactional : 수정된 사항을 save 없이도 반영함.
트랜젝션이 시작되었을때 다른시점에 시작된 트랜젝션과 서로 영향을 줄수 없어 격리성이 유지되며
에러 발생시 Rollback 하는 방법으로 일부만 남아있는것이 아닌
정상 전체저장 비정상 전체롤백을 통해 그 원자성을 유지함
* */
@Rollback(false)
//테스트인경우 롤백이 자동으로 됨
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void testMember(){
        Member member = new Member("memberA");
        Member savedMember = memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.find(savedMember.getId());
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
        //같은 트랜잭션 안에서는 영속성 컨텍스트 동일성 보장
        /*
        * 1차 캐시
        * 영속성 컨텍스트 내부에는 엔티티를 보관하는 저장소가 있는데 이를 1차 캐시라고 함
        * 일반적으로 트랜잭션을 시작하고 종료할 때까지만 1차캐시가 유효함
        * */
    }

    @Test
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //변경감지(더티체킹) 확인하기
        //findMember1.setUsername("member!!!!");


        //리스트 조회 검증
        List<Member> all = memberJpaRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);

        long deletedCount = memberJpaRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }
}