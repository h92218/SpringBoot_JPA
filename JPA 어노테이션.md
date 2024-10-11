## JPA
자바 애플리케이션에서 RDB를 사용하는 방식을 정의한 인터페이스와 어노테이션의 표준 집합
ORM 프레임워크(Hibernate, OpenJPA 등)에서 구현하도록 공통 API를 제공함
※ORM : 객체와 RDB를 자동 매핑해주는 기술       


      
## Hibernate
JPA의 모든 기능을 지원하며 Hibernate는 객체와 RDB간 매핑 처리


## EntityManagerFactory
EntityManager를 만드는 역할


## @EnableJpaRepositories
JPA Repository 빈을 활성화하는 어노테이션. 스프링부트에서는 자동설정이돼서 생략가능.
basePackages 속성을 주지 않으면 @SpringBootApplication에 설정한 빈 scan 범위와 동일한 범위로 빈을 scan함.

## @PersistenceContext
EntityManager를 빈으로 주입할 때 사용하는 어노테이션


## EntityManager & Persistence Context 영속성 컨텍스트
엔티티 매니저를 생성할 때 영속성 컨텍스트가 만들어짐   
영속성 컨텍스트 : JPA에서 엔티티 객체를 관리하는 환경 
하나의 영속성 컨텍스트는 하나의 트랜잭션에 대응하며, 트랜잭션이 종료되면 영속성 컨텍스트의 상태가 초기화됨
엔티티 매니저를 통해 엔티티를 영속성 컨텍스트에 저장(persist)하고 관리한다.
엔티티 매니저는 데이터 변경 시 트랜잭션을 시작해야 한다.
영속성 컨텍스트는 엔티티를 식별자 값으로 구분하기 때문에 반드시 식별자 값이 있어야 함.

### Entity Manager Method
  - persist() : 엔티티 매니저를 사용해서 영속성 컨텍스트의 1차 캐시에 저장(영속상태)
            트랜잭션을 커밋할 때(하기전에) flush가 발생하여 데이터베이스에 반영
  - find() :  엔티티 조회. 1차캐시 먼저 조회, 없으면 그다음 DB조회

  - flush() :
영속성 컨텍스트의 변경 내용을 DB에 반영한다. 커밋은 아님
영속성 컨텍스트에 있는걸 지우지는 않음
트랜잭션 커밋 시 플러시 자동 호출

QueryDSL 역시 JPQL 빌더 역할을 해주는 것이기 때문에 QueryDSL 실행 시에도 당연히 플러시와 트랜젝션 커밋은 자동 호출된다.


## Spring data jpa에서 Entity Manager를 직접 사용하지도 않고 트랜잭션 생성 필요 없는 이유
=> Spring data jpa에서 제공하는 JpaRepository를 사용하기 때문

## SimpleJpaRepository
SimpleJpaRepository는 JpaRepository 인터페이스의 기본 구현 클래스
JpaRepository 인터페이스를 상속하는 리포지토리 인터페이스가 구현체를 필요로 할 때, Spring Data JPA는 SimpleJpaRepository를 활용하여 구현체를 동적으로 생성한다.
여기에 @Transactional(readOnly = true) 가 달려있어서 트랜잭션으로 관리되는 것
EntityManager도 있음



## @Entity
JPA를 사용하여 테이블과 매핑할 클래스에 붙임
파라미터가 없는 public 또는 protected 기본생성자 꼭 있어야함 없으면 자바가 만듦.
파라미터가 있는 생성자만 있으면 자바가 기본생성자를 만들어주지 않으므로 직접 만들어야함.

* 기본 생성자가 있어야 하는 이유   
  JPA가 엔티티 객체를 생성하여 값을 주입할 때 Reflection API를 사용하는데 ReflectionAPI에서 기본생성자가 필요함
  
* public 또는 protected 접근제한자를 붙여야하는 이유   
   entity 지연로딩을 사용하는 경우 JPA가 원본 엔티티 클래스로부터 상속을 받는 프록시 객체를 생성하여 사용하기 때문(private 는 상속 안 됨)

 * Reflection API : 구체적인 클래스 타입을 알지 못해도 그 클래스의 메소드, 변수들에 접근할 수 있도록 하는 API
     
   사용예시
```java
//이름으로 클래스 찾기
Class basket = Class.forName("Basket");
//생성자 가져오기
Constructor[] constructors = basket.getDeclaredConstructors();
//인스턴스생성
Basket basket = constructor.newInstance();
//필드 가져오기
Field[] fields = class.getDeclaredFields();
//메소드 가져오기
Method[] methods = class.getDeclaredMethods();
```



## @Table
엔티티와 매핑할 테이블
name 생략시 엔티티 이름을 테이블 이름으로 사용함
unique제약조건 만들어 줄 수 있음
```java
@Table(name="BASKET", uniqueConstraints = {@UniqueConstraint(
 name = "BASKET_UNIQUE1",
 columnNames = {"memberId","basketNo"}
)})
```




## @DynamicInsert & @DynamicUpdate
설정된 컬럼만 가지고 동적으로 insert와 update 쿼리생성하여 날림
모든 필드를 사용하면 데이터 전송량이 증가하지만 쿼리가 항상 같으므로 재사용 가능
컬럼이 대략 30개 이상이 되면 정적 수정 쿼리보다 @DynamicUpdate를 사용하는 것이 빠르다고 함.


## @GeneratedValue
기본키 자동 생성
스프링 3.0 밑으로는 spring.jpa.hibernate.use-new-id-generator-mappings=true 속성 추가 필요


## @GeneratedValue(strategy = GenerationType.IDENTITY)
기본 키 생성을 데이터베이스에 위임한다 
주로 MySQL, PostgreSQL, SQL Server, DB2에서 사용 가능. 
DB에 값을 저장하고 나서야 기본 키 값을 구할 수 있음.
원래는 트랜잭션이 끝나는 commit 시점(직전)에 flush가 이루어지지만 이 경우는 db에서 id값을 받아와야 해서 save()메서드를 호출하는 시점에 insert를 flush함
하이버네이트는 DB에 값을 저장하면서 동시에 생성된 기본 키 값을 얻어오므로 DB와 한 번만 통신함.


## save() 메서드
엔티티에 식별자 값이 없으면 새로운 엔티티 => EntityManager.persist() 를 호출
식별자 값이 있으면 이미 있는 엔티티 => EntityManager.merge()를 호출


## GeneratedValue 키 생성 전략
### @GeneratedValue(strategy = GenerationType.SEQUENCE)
데이터베이스 시퀀스를 사용해서 기본 키를 할당한다.
시퀀스를 지원하는 오라클, PostgreSQL, H2, DB2 에서 사용 가능.
DB에서 시퀀스를 생성하고 @SequnceGenerator를 사용해서 시퀀스 생성키를 등록하고 매핑해줘야함
commit 시점에 flush가 이루어짐
DB에서 시퀀스 값을 조회해야 해서 시퀀스 조회, entity insert 이렇게 총 2번 통신
=> allocationSize 속성을 사용해 최적화 할 수 있음
=> 설정한 값 만큼 한번에 시퀀스 값을 증가시키고 나서 메모리에 시퀀스 값을 할당, 메모리에서 식별자를 갖다쓰는방법


### @GeneratedValue(strategy = GenerationType.TABLE)
키 생성 전용 테이블을 하나 만들고 이름 컬럼, 값 컬럼을 만들어서 사용.
값을 조회하면서 select, 값 증가시키면서 update, entity insert 총 3번.
최적화 전략은 시퀀스와 같음

### @GeneratedValue(strategy = GenerationType.UUID)
UUID를 사용하여 기본키를 생성.

### @GeneratedValue(strategy = GenerationType.AUTO)
DB에 따라 IDENTITY, SEQUENCE, TABLE 전략 중 하나를 자동으로 선택



## @ManyToOne / @OneToMany / @JoinColumn
연관관계가 있는 두 엔티티는 각자 조회하면 불편하므로 엔티티에 연관관계를 매핑해줘서(객체참조) 조회 및 수정을 편하게 할 수 있음
보통 외래키를 가진 쪽을 연관관계의 주인이라고 함
연관관계의 주인쪽이 DB 연관관계와 매핑되고 외래 키 컬럼의 값을 등록/수정/삭제 할 수 있고, 아닌 쪽은 읽기만 가능
예)Basket 엔티티
```java
@Entity
public class Basket{

 @Id
 private String basketNo;
 private String date;

 @ManyToOne  //연관관계 N:1 매핑 정보를 나타내는 어노테이션
 @JoinColumn(name="member_id") //외래키를 매핑하는 어노테이션. name에는 매핑할 외래 키 이름 지정
 private Member member;

 public void setMember(Member member){
   this.member = member;
   //member쪽에서도 basket참조 수정
  //어차피 member쪽에서 값을 설정해도 member는 연관관계 주인이 아니라서 DB반영 시 사용되지 않으나 JPA를 사용하지 않는 순수객체 상태에서 문제가 발생하지 않도록 하기 위함
   member.getBaskets().add(this);
 }
}
```
예)Member 엔티티   
```java
@Entity
public class Member{

 @Id
 @Column(name="member_id")
 private String memberId;

 private String memberName;

 @OneToMany(mappedBy="member") //연관관계 1:N 매핑 정보를 나타내는 어노테이션. 연관관계가 아닌 쪽은 maapedBy로 반대쪽 매핑의 필드 이름을 값으로 주면 됨
 private List<Basket> baskets;

}
```   
예) 두 엔티티 저장시   
```java
Basket basket1 = new Basket("basket1","20240310");
Basket basket2 = new Basket("basket2","20240311");
Member member = new Member("member1","hyunsun");

//basket 테이블의 memberId 컬럼에 값이 들어가게 됨
basket1.setMember(member);
basket2.setMember(member);
</code></pre>

예)basket 엔티티 조회시
<pre><code class="java">
Basket basket = repository.findByBasketNo("basket1"); // join 쿼리가 나간다.
basket.getDate();
basket.getMember.getMemberId();
basket.getMember.getMemberName();
</code></pre>

예)Member 엔티티 조회시
<pre><code class="java">
Membmer member = repository.findByMemberId("member1"); // join 쿼리가 나간다.
member.getBaskets().size() //사이즈 2개 조회가능
```

## 즉시로딩과 지연로딩

### @ManyToOne(fetch = FetchType.EAGER) 즉시로딩
연관된 엔티티를 join을 사용하여 함께 조회한다.

### @ManyToOne(fetch = FetchType.LAZY) 지연로딩
연관된 객체를 프록시로 조회함
실제 사용될 때까지 데이터 로딩을 미루다가 실제 사용할 때 초기화하면서 db조회해옴.

### fetch 기본값
@ManyToOne, @OneToOne : 즉시로딩
@OneToMany, @ManyToMany : 지연로딩(일대다 조인은 결과 데이터가 다 쪽에 있는 수만큼 증가하게 되므로)

## 선택적 관계와 조인
### @ManyToOne(optional = false)
선택적 관계면 외부조인을 사용하고 필수 관계면 내부 조인을 사용
다대일인경우(OneToMany, ManyToMany) 무조건 외부조인

### @JoinColumn(nullable = false)
NULL 허용 여부. 기본값은 true
true인 경우 외부조인, false인 경우 내부조인사용



## 스프링 데이터 JPA 반환타입
### 컬렉션
=> 쿼리 결과가 없는 경우 null이 아니라 사이즈가 0인 빈 컬렉션 반환
```java
List<Goods> findByCode(String code);
```


### 단건
=> 쿼리 결과가 없는 경우 null을 반환
=> 쿼리 결과가 여러건이면 NonUniqueResultException 발생
```java
Goods findByIdx(Long idx);
```


### optional단건
=> 쿼리 결과가 없는 경우 Optional.empty
```java
Optional<Goods> findByContents(String contents);
```
=> 있다면 
``java
Optional<Goods> goods2 = goodsRepository.findByContents(contents);
goods2.ifPresent(good -> {System.out.println("가져온 goods : " + good.getName()); });
```


### optional컬렉션
=> 쿼리 결과가 없는 경우 null이 아니라 사이즈가 0인 빈 컬렉션 반환하므로 null값 감지 못함.
```java
goods2.ifPresent(good -> {System.out.println("가져온 goods 0 : " + good.get(0).getName()); });
```

## Optional 사용

### Optional.empty()
빈 Optional 객체를 생성할 때 사용함
```java
Optional<Goods> goods1 = Optional.empty();
``

### Optional.of();
주어진 값으로 Optional 객체 생성.
null이라면 NPE 발생함.
```java
Optional<Goods> testGoods = Optional.of(goodsRepository.findByCode("RPZ001"));
```
   
### Optional.orElse()
Optional 값이 없으면 orElse 안의 값이 반환됨.
파라미터안의 값을 미리 생성해놓고 없으면 사용하는 방식.. 
Optional 값이 있든 없든 무조건 실행됨
```java
Optional<Goods> testGoods = Optional.of(goodsRepository.findByCode("RPZ001"));
Goods testGoods2 = testGoods.orElse(goodsRepository.findByCode("RPZ002"));
```


### Optional.orElseGet()
값이 없으면 get을 실행하여 orElseGet() 안의 값이 반환됨.
매개변수는 supplier
```java
Optional<Goods> testGoods = Optional.of(goodsRepository.findByCode("RPZ001"));
Goods testGoods2 = testGoods.orElseGet(() -> goodsRepository.findByCode("RPZ002"));
```


### Optional.get()
Optional 객체 안의 값을 반환함.
null인 경우 NoSuchElementException 발생 => isPresent()로 비어있는지 먼저 확인하는게 나음.
```java
//isPresent : Optional 객체 안에 값이 있는지 여부 반환
if(testGoods.isPresent()){
   Goods testGoods2 = testGoods.get();
}
```



### Optional.ifPresent()
Optional 객체 안에 값이 존재하는 경우에만 동작 수행
```java
Optional<Goods> goods1 = goodsRepository.findByName(name);
goods1.ifPresent(good -> System.out.println("가져온 goods : " + good.getName()));
</code></pre>

### Optional.ifPresentOrElse
```java
Optional<Goods> goods1 = goodsRepository.findByName(name);
goods1.ifPresentOrElse(good -> System.out.println("가져온 goods : " + good.getName()),
                       () -> {throw new NoSuchElementException("그런 메뉴는 없습니다.");}
                       );
```

### Optional.ofNullable();
주어진 값으로 해당 값을 가지는 Optional 객체 생성,
null이면 null인 Optional 객체 생성
BasketService isSameDoughAndEdge 참고
```java
Optional<String> goodsEdge = Optional.ofNullable(goods.getOptions().getEdge())
                                            .map(map -> (String)map.get("code"));
Optional<String> basketGoodsEdge = Optional.ofNullable(basketGood.getOptions().getEdge())
                                            .map(map -> (String)map.get("code"));
boolean isEdgeSame = goodsEdge.equals(basketGoodsEdge);
```


### Optional.orElseThrow()
Optional 안의 값이 있으면 반환,
없으면 예외 발생
```java
Optional<Goods> testGoods = Optional.of(goodsRepository.findByCode("RPZ001"));
Goods testGoods2 = testGoods.orElseThrow(() -> new NoSuchElementException("그런 피자는 없습니다."));
```

### Optional을 왜쓰는지..
Optional은 null을 반환하면 오류가 발생할 가능성이 매우 높은 경우에 '결과 없음'을 명확하게 드러내기 위해 메소드의 반환 타입으로 사용되도록 매우 제한적인 경우로 설계되었다


- 생성자, 수정자, 메소드 파라미터 등으로 Optional을 넘기지 말것 => 반환타입으로 사용하기 위해 고안된 것

- isPresent()와 get() 대신에 orElse()/orElseGet()/orElseThrow()를 사용하는게 낫다

- Optional<T> 대신 OptionalInt, OptionalLong, OptionalDouble

- Optional 객체를 컬렉션 안에 넣지 말것(컬렉션의 원소로 사용 금지 ). 직렬화 지원하지 않음.
 
