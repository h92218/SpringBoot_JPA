package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    //인터페이스밖에 없는데 동작한다.
    @Autowired TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember(){
        System.out.println("memberRepository = " + memberRepository.getClass());
        //memberRepository = class jdk.proxy2.$Proxy121

        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(savedMember.getId()).get();
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }


    @Test
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //변경감지(더티체킹) 확인하기
        //findMember1.setUsername("member!!!!");


        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA",20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testQuery(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 10);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void findUsernameList(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 10);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();
        for(String s : usernameList){
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto(){
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);


        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for(MemberDto dto : memberDto){
            System.out.println("dto = " + dto);
        }
        //dto = MemberDto(id=2, username=AAA, teamName=teamA)
    }


    @Test
    public void findByNames(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 10);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA","BBB"));
        for(Member member : result){
            System.out.println("member = " + member);
        }
    }


    @Test
    public void returnType(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 10);
        memberRepository.save(m1);
        memberRepository.save(m2);

        //List<Member> aaa = memberRepository.findListByUsername("AAA");
        //Member aaa = memberRepository.findMemberByUsername("AAA");

        List<Member> result = memberRepository.findListByUsername("sfsfsfsf");
        System.out.println("result = " + result.size());
        //null이 아니라 빈 컬렉션을 반환해 사이즈가 0이 나온다.


        Member result2 = memberRepository.findMemberByUsername("fdfdfdf");
        System.out.println("result2 = " + result2);
        //단건조회건이 없는 경우 null 반환

        Optional<Member> findMember = memberRepository.findOptionalMemberByUsername("asdf");
        System.out.println("findMember = " + findMember);
        //findMember = Optional.empty
    }

    //JPA 페이징 테스트
    @Test
    public void paging(){
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",10));
        memberRepository.save(new Member("member4",10));
        memberRepository.save(new Member("member5",10));

        //주의 : 페이지가 0부터 시작함. 3개 가져오기
        int age=10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        //반환 타입을 Page로 받으면 totalCount를 가져오는 쿼리도 함께 나감.
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        //페이지를 유지하면서 내부 entity를 dto로 변환하기
        Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        //slice : 요청한거보다 1개 더 가져와서 더보기 누르면 보이는 기능. 전체 count를 가져오지 않음.
        //Slice<Member> page =  memberRepository.findByAge(age, pageRequest);

        List<Member> content = page.getContent();
        long totalElement = page.getTotalElements();//slice는 totalCount 쿼리를 날리지 않는다.

        for(Member member : content){
            System.out.println("member = " + member);
        }

        System.out.println("totalElements : " + totalElement);
        //totalElements : 5


        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue(); //첫번째 페이지냐
        assertThat(page.hasNext()).isTrue(); //다음페이지가 있냐


        /* slice인 경우 확인
        assertThat(content.size()).isEqualTo(3);
        //assertThat(page.getTotalElements()).isEqualTo(5); slice는 totalCount 쿼리를 날리지 않는다.
        assertThat(page.getNumber()).isEqualTo(0);
        //assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue(); //첫번째 페이지냐
        assertThat(page.hasNext()).isTrue(); //다음페이지가 있냐
        */

    }


    @Test
    public void bulkUpdate(){
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",19));
        memberRepository.save(new Member("member3",20));
        memberRepository.save(new Member("member4",21));
        memberRepository.save(new Member("member5",40));

        int resultCount = memberRepository.bulkAgePlus(20);

        //DB에 데이터를 반영하고 영속성 컨텍스트를 지움.
       //em.flush();
       //em.clear();

        List<Member> result = memberRepository.findListByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);
        /* member5 = Member(id=5, username=member5, age=40)
        * bulk 연산은 영속성컨텍스트를 이용하지 않고 바로 DB에 입력을 하므로
        * 영속성 컨텍스트에는 아직 데이터가 40인것임.
        * 하지만 flush, clear를 하면 영속성 컨텍스트가 비었기 때문에 db에서 조회해 온다.
         */
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);
        
        em.flush();
        em.clear();

        //member 조회하는 쿼리가 나가고 연관된 team 쿼리가 한번 더 나가는 문제가 있음. n+1문제라고 함
        List<Member> members = memberRepository.findAll();
        for(Member member : members){
            System.out.println("member.getUsername() = " + member.getUsername());
            System.out.println("member.teamClass = " + member.getTeam().getClass());
            //member.teamClass = class study.datajpa.entity.Team$HibernateProxy$yvtonfut (프록시객체)
            //fetch를 사용하는 경우 member.teamClass = class study.datajpa.entity.Team
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }
    }
}