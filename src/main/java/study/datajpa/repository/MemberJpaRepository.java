package study.datajpa.repository;

import org.springframework.stereotype.Repository;
import study.datajpa.entity.Member;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {

    @PersistenceContext
    //영속성 컨텍스트. EntityManager를 빈으로 주입할 때 사용하는 어노테이션
    private EntityManager em;

    /*
        JPA는 변경감지 기능으로 데이터를 변경함
        Entity를 수정하고 commit하면 변경감지를 해서 update가 되므로
        update 메소드 필요없음
    */
    public Member save(Member member){
        em.persist(member);
        return member;
    }


    public void delete(Member member){
        em.remove(member);
    }

    public List<Member> findAll(){
        //특정조건 필터링, where 등을 사용할 경우 JPA가 제공하는 JPQL 을 사용함
        //객체를 대상으로 하는 쿼리, 반환 클래스를 적어줌
        return em.createQuery("select m from Member m",Member.class).getResultList();
    }

    public Optional<Member> findById(Long id){
        Member member = em.find(Member.class,id);
        return Optional.ofNullable(member);
    }

    public long count(){
        return em.createQuery("select count(m) from Member m",Long.class).getSingleResult();
    }
    public Member find(Long id){
        return em.find(Member.class,id);
    }
}
