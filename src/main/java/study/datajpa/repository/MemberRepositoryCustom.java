package study.datajpa.repository;

import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}

/*
* 사용자 정의 리포지토리 구현
* 스프링 데이터 JPA 리포지토리는 인터페이스만 정의하고 구현체는 스프링이 자동 생성한다.
* 인터페이스의 메서드를 직접 구현하고 싶은 경우 사용
* 인터페이스 생성, 구현체 생성(인터페이스 이름 + Impl 규칙 지켜야 함)
* 그냥 클래스로 만들고 스프링 빈으로 등록해서 사용해도 됨.
* 핵심 비지니스 로직과 아닌 로직의 분리, 라이프사이클을 고려해서 만들어야 함.
* */