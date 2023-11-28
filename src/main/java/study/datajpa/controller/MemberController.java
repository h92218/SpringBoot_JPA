package study.datajpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id){
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    /*
    도메인 클래스 컨버터 기능
    HPTT 요청은 회원 id를 받지만 도메인 클래스 컨버터가 회원 엔티티 객체를 반환함.
    트랜잭션이 없는 범위에서 엔티티를 조회했으므로 엔티티를 변경해도 DB에 반영되지 않음.
    단순 조회용으로만 사용해야한다.
     */
    @GetMapping("/members2/{id}")
    public String findMember2(@PathVariable("id") Member member){
        return member.getUsername();
    }


    /*요청 파라미터 예시
        page : 현재 페이지, 0부터 시작
        size : 한 페이지에 노출할 데이터 건수. 디폴트20갠데 application.yml에서도 설정가능
        sort : 정렬 조건 ASC,DESC
        @PageableDefault 로도 설정변경 할 수 있다.
        예) http://localhost:8080/members?page=1&size=3&sort=id,desc
     */
    @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size=5) Pageable pageable){
       Page<Member> page =  memberRepository.findAll(pageable);
       //엔티티를 그대로 외부로 노출하지 말 것. 항상 DTO로 변환할 것.
       Page<MemberDto> map = page.map(MemberDto::new);
       return map;
    }



    //의존성 주입 후 초기화 수행
    @PostConstruct
    public void init(){
       for(int i=0; i < 100 ; i ++){
           memberRepository.save(new Member("user" + i,i));
       }
    }
}
