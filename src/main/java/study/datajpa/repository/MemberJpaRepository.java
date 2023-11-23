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

    public List<Member> findByUsernameAndAgeGreaterThan(String username, int age){
        return em.createQuery("select m from Member m where m.username = :username and m.age > :age")
                .setParameter("username", username)
                .setParameter("age", age).getResultList();
    }

    //JPA 페이징
    //현재 데이터베이스에 맞는 쿼리가 나간다.
    public List<Member> findByPage(int age, int offset, int limit){// offset 부터 시작해서 limit개를 가져오기
        return em.createQuery("select m from Member m where m.age = :age order by m.username desc")
                .setParameter("age",age)
                .setFirstResult(offset) //어디서부터 가져올지
                .setMaxResults(limit) //개수를 몇 개 가져올지
                .getResultList();

    }

    public long totalCount(int age){
        return em.createQuery("select count(m) from Member m where m.age= :age", Long.class)
                .setParameter("age",age)
                .getSingleResult();
    }


    //벌크성 수정 쿼리
    public int bulkAgePlus(int age){
        int resultCount = em.createQuery("update Member m set m.age=m.age + 1 where m.age >= :age")
                .setParameter("age",age)
                .executeUpdate(); //응답값에 개수가 나오게 함.

        return resultCount;
    }

}
