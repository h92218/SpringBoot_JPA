QUERY DSL (Domain Specific Language)

## Projections
select 조회 대상 지정 

### Projection.bean()
수정자를 사용해서 값을 채운다.
쿼리 결과와 매핑할 프로퍼티 이름이 같아야함, 다르면 as 사용 

### Projection.fields()
필드에 직접 접근해서 값을 채운다.
필드를 private로 설정해도 동작함

### Projection.constructor()
생성자를 사용함.
지정한 프로젝션과 파라미터 순서가 같은 생성자가 필요함

- - -
   
## 스프링데이터 JPA 페이징 기능(org.springframework.data.domain)
```java
PageRequest pageRequest = PageRequest.of(0,3,Sort.by(Sort.Direction.DESC,"code")); //페이지번호, 한페이지당 컨텐츠 사이즈, 정렬기준
Page<Goods> result = goodsRepository.findAll(pageRequest);
result.getTotalElements() //전체 데이터 수
result.getTotalPages() //전체 페이지 수
result.getContent() //조회된 데이터
```



## QuerydslPredicateExecutor
* 사용법   
JpaRepository를 상속받고있는 repository 인터페이스에 상속 추가

* 장점   
EntityManager 주입/JPAQueryFactory생성 << 안 해도 됨

* 단점   
join 사용 불가   
메서드 파라미터가 Predicate라 쿼리메서드에 조건절을 직접 넘겨줘야 됨 
```java
repository.findAll(qGoods.useYn.eq(true))
```
 


## QueryDslRepositorySupport
* 사용법   
repository 구현체 클래스에 QuerydslRepositorySupport 상속 
(GoodsRepositoryImpl 참고)

* 장점   
getQuerydsl().applyPagination() 을 사용해서 페이징을 간편하게 구현 

* 단점   
스프링 데이터 Sort 사용할 때 버그가 있다고 하는데 아직 잘 모르겠음


## fetch 메소드
fetch() :  리스트로 결과를 반환, 데이터가 없는 경우 빈 리스트 반환   
fetchOne() : 단건 조회, 결과가 없는 경우 null, 둘 이상인 경우 NonUniqueResultException   
fetchFirst() : 첫번째 결과 fetch   
fetchResults() : 페이징 시 사용. total contents를 가져옴.   
fetchCount() : count 쿼리   



## 동적쿼리 만들기
1. BooleanBuilder 사용 
```java
BooleanBuilder booleanBuilder = new BooleanBuilder();
if(StringUtils.isNotBlank(param1)){
   booleanBuilder.and(qGoods.code.eq(param1));
}

if(StringUtils.isNotBlank(param2)){
   booleanBuilder.or(qGoods.code.eq(param2));
}

queryFactory.
            ...
            .where(booleanBuilder)
```
=> 가독성이 좋지않고 조건문으로 null체크, 하나하나 추가하기 불편   

2. BooleanExpression 사용
메소드를 따로 빼주는 방법   
=> 가독성이 높아지고 재사용성이 높음(권장)

```java
private BooleanExpression codeEq(String code){
   return StringUtils.isNotBlank(code) ? qGoods.code.eq(code) : null;
}

private BooleanExpression nameContains(String name){
   return StringUtils.isNotBlank(name) ? qGoods.name.contains(name) : null;
}

queryFactory.
            ...
            .where(codeEq(code),nameContains("블랙"))  //where 메소드에서 null이면 그냥 무시됨


//booleanBuilder로 붙일 수도 있음 
booleanBuilder.and(codeEq(code));
booleanBuilder.or(codeEq(code));
```

## 조인
1. 연관관계 매핑되어 있는경우
```java
queryFactory
.select()
.from()
// alias를 qMenuCategory로 선언했다면 추가적인 join 및 where, select 절에 꼭 qMenuCategory를 사용해야 함.
//그렇지 않으면 의도하지 않은 조인이 추가됨. => join 두개 나감.
.rightJoin(qGoods.menuCategorySubCode, qMenuCategory)
.on()   // 조인시 조인대상 필터링
```
on으로 조인대상 먼저 필터링 하고 조인한다음에    
where로 거른다
=> 어느게 나은지는 EXPLAIN ANALYZE 사용해볼것   
=> 조인기준 테이블의 컬럼을 조회 안하는 경우 null나오는 것 조심   

2. 연관관계 매핑 안 되어 있는 경우
```java
queryFactory
.select()
.from(qGoods)
.leftJoin(qGoodsType)
.on(qGoodsTypeCode.eq(qGoodsType.code)) //일반 쿼리처럼 조인 조건 적어주면 됨
```

## Cross Join
묵시적 조인 사용 시 JPA가 join을 해주지만 크로스조인을 하게 되는 경우가 있음, 의도하지 않은 join이 발생하기도 함   
=> 성능상 좋지 않으니 되도록이면 명시적 조인을 사용하자   
(테스트해봤는데 cross join은 아직 못 봄)
```java
List<GoodsDto> result = queryFactory
                    .select(Projections.bean(GoodsDto.class,
                            qGoods.code,
                            qGoods.name,
                            qGoods.goodsType,
                            qGoods.price,
                            qGoods.contents,
                            qGoods.imgL.as("imageL"),
                            qGoods.imgM.as("imageM"),
                            qGoods.options,
                            qGoods.useYn,
                            qGoods.isNew))
                    .from(qGoods)
                    .where(qGoods.goodsType.code.eq("RPZ"))
                    .orderBy(qGoods.idx.asc())
                    .fetch();
```

※ 명시적 조인(Explicit Join) : join을 사용하여 두 테이블 조인   
※ 묵시적 조인(Implicit Join) : 콤마와 where을 사용하여 묵시적으로 조인   
※ 크로스 조인(Cross Join) : 두 테이블의 모든 행이 각각 조인됨.   



## N+1 문제
JPA 쿼리메소드사용 
```java
goodsRepository.findAll();
```
findAll() 수행 시점에    
Goods 엔티티를 조회하는 쿼리,   
매핑된 GoodsType를 조회하는 select 3번,   
매핑된 MenuCategory를 조회하는 select 6번 각각 수행됨(join으로 실행되지 않음)   
=> +관련된 엔티티의 데이터 개수만큼 쿼리가 나가는 현상+   
=> EAGER의 경우 쿼리가 한 번에 나감   
=> LAZY를 사용했을 땐 반복문을 사용하는 경우 엔티티 참조하려고 select가 나감.   
=> LAZY LOADING 매핑된 엔티티 조회가 포함되어 있으면 return시에 결국 select 하게 됨.   
```java
List<Goods> goods = goodsRepository.findAll();
for(Goods s : goods){
    System.out.println(s.getGoodsType().getName());
} 
```

※ 왜 join으로 쿼리 생성이 안 되고 select가 각각 나가나   
=> JPA가 메소드 이름을 분석해서 JPQL을 생성하고 실행함.   
=> JPQL을 생성할때는 fetch 전략을 참고하지 않기 때문   



## queryDSL 사용해도 N+1 문제가 생기나
```java
public List<Goods> nplus1test(){
        List<Goods> result = queryFactory
                    .select(qGoods) //entity 전체 조회 시 lazy loading으로 매핑된 엔티티에 대한 select가 따로 나감. join 안 됨
                    .from(qGoods) 
                    .leftJoin(qGoods.goodsType,qGoodsType)
                    .leftJoin(qGoods.menuCategorySubCode,qMenuCategory)  
                    .fetch();
    
            return result;
}
```

## 컬럼을 지정해줘도 발생하나
컬럼 지정시 join이 된다.
컬럼을 지정해주면 tuple로 받아야함 
```java
@Override
public List<Tuple> nplus1test(){
        List<Tuple> result = queryFactory
                    .select(qGoods.code,qGoods.goodsType)
                    .from(qGoods)
                    .fetch();
        
        for(Tuple tuple : result){
            System.out.println(tuple.get(qGoods.code));
            System.out.println(tuple.get(qGoods.goodsType));
        }
        return result;
}
```




## Entity가 아니라 dto로 뽑아도 문제가 발생하나(Projection 사용)
```java
@Override
public List<GoodsDto> nplus1test(){
    List<GoodsDto> result = queryFactory
                    .select(Projections.bean(GoodsDto.class, 
                              qGoods.code, 
                              qGoodsType))  //join 자동 생성되지만 select 컬럼에 entity를 넣지 않도록 주의 => 성능상 필요한 컬럼만 조회할 것 
                    .from(qGoods)
                    .fetch();
    
            return result;
}
```

## EntityGraph 
연관관계가 지연로딩으로 되어있는 엔티티를 조회할 경우 fetch join을 사용한다.    
쿼리 메소드 위에 달면 됨.   
```java
@EntityGraph(attributePaths = {"menuCategorySubCode","goodsType"})
List<Goods> findAll();
```

## fetchjoin
select 대상 entity와 fetch join이 걸려있는 entity를 포함하여 함께 select 함 => 쿼리에 join 포함됨    
일반 join은 실제 질의하는 select 대상 entity만 select함   
```java
@Override
public List<Goods> nplus1test(){
        List<Goods> result = queryFactory
                    .select(qGoods)
                    .from(qGoods)
                    .leftJoin(qGoods.goodsType,qGoodsType)
                    .fetchJoin()  
                    .leftJoin(qGoods.menuCategorySubCode,qMenuCategory)
                    .fetchJoin()    
                    .fetch();
    
            return result;
}
```


## lazy loading 사용시 주의점
https://stackoverflow.com/questions/30082281/manytoonefetch-fetchtype-lazy-doesnt-work-on-non-primary-key-referenced-co 참고   
Goods 엔티티에 Lazy loading으로 매핑된 엔티티   
```java
@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
@JoinColumn(name = "menucategory_sub_code", referencedColumnName = "sub_code")
private MenuCategory menuCategorySubCode;
```

Goods 조회 시 Lazy loading이 동작하지 않고 eager로 동작함 
```java
goodsRepository.findAll(); 
```
ManyToOne에 One으로 매핑된 객체의 참조된 컬럼이 PK가 아니면 Lazy loding이 동작하지 않음    
=> PK로 바꿔줬더니 Lazy loading 동작함    
=> 테이블 설계시 many쪽에서 fk로 참조할 컬럼을 pk로 하거나... 아니면 연관관계를 매핑해주지 않거나 한다고 함.   


## 페이징 기능 사용 안하고 OFFSET과 LIMIT을 이용하는 경우 성능개선
예를 들어 offset이 1000이고 limit가 10인 경우 1010 행을 읽어 10개만 사용하는 쿼리가 된다.   
```java
 @Override
public List<GoodsDto> selectOffsetTest(int offset, int limit) {
    List<GoodsDto> result = queryFactory.select(Projections.bean(GoodsDto.class,
                                qGoods.code,
                                qGoods.name
                                ))
                                .from(qGoods)
                                .orderBy(qGoods.code.desc())
                                .offset(offset)
                                .limit(limit)
                                .fetch();
                                return result;
                                
}
```
생성되는 쿼리
```sql
select
        g1_0.code,
        g1_0.name
    from
        GOODS g1_0
    order by
        g1_0.code desc
    offset
        ? rows
    fetch
        first ? rows only
```

=> offset을 사용하지 않는 방법을 사용하여 성능 개선   
=> 마지막 조회 결과의 id를 조건문에 사용하여 이전에 조회된 결과를 한 번에 건너뛸 수 있게 한다.   

```java
queryFactory.select(Projections.bean(GoodsDto.class,
                                qGoods.code,
                                qGoods.name
                                ))
                                .from(qGoods)
                                .where(qGoods.code.lt(lastGoods))
                                .orderBy(qGoods.code.desc())
                                .limit(limit)
                                .fetch();
```

생성되는 쿼리 
```sql
select
        g1_0.code,
        g1_0.name
    from
        GOODS g1_0
    where
        g1_0.code<?
    order by
        g1_0.code desc
    fetch
        first ? rows only
```

## 프로시저 사용하기
엔티티매니저 사용
```java
StoredProcedureQuery storedProcedure = em.createStoredProcedureQuery("hyunsunfunc");
storedProcedure.registerStoredProcedureParameter("codestr", String.class, ParameterMode.IN);
storedProcedure.setParameter("codestr", "RPZ002");
storedProcedure.execute();
```


# postgre jsonb 매핑

## Hypersistence Utils library 참고 >> https://github.com/vladmihalcea/hypersistence-utils
Oracle, SQL Server, Postgre SQL, MySQL의 JSON Type 지원   
JSON 컬럼을 Map, List, POJO, String, JsonNode 엔티티에 매핑시킬 수 있다. (POJO : Plain Old Java Object. 자바 객체)   
PostgreSQL에선 JsonType이나 JsonBinaryType을 jsonb컬럼과 json컬럼에 다 매핑해서 쓸 수 있음.   
```java
@Type(JsonType.class) //JsonType으로 써도 됨
@Comment("사이즈&가격")
@Column(name = "price", nullable = false, columnDefinition = "jsonb")
private Map<String, Object> price;
```


## PostgreSQl JSON 저장 타입
1. json 타입 : 텍스트 원본 저장, 쿼리 수행시마다 json 데이터의 구조를 파싱하여 결과를 반환함(성능 저하의 여지가 있음)
2. jsonb 타입 : 바이너리 형식으로 저장. 내부적으로 데이터가 구조화되고 인덱싱되어 저장됨. 쿼리 성능 향상. 더 많은 저장공간 필요. 빠른 쿼리 성능 제공.


## jsonb 타입 조건절에 사용하기


### 네이티브 쿼리 사용하기
```java
String sql = "select idx, code, name from goods where price -> 'L' = '27900'";
Query nativeQuery = em.createNativeQuery(sql,GoodsDto.class);
List<Goods> resultList1 = nativeQuery.getResultList();
```
 
=> entity로 result를 받으려는 경우 모든 컬럼을 조회해야 함. 일부만 select 하는 경우 column not found 에러 남.   
=> dto로 받으려는 경우 조회한 컬럼을 가진 생성자가 있어야함   
Cannot instantiate query result type 'kr.co.dominos.goods.dto.GoodsDto' due to: Result class must have a single constructor with exactly 3 parameters   
=> 일부 컬럼만 조회하고 싶은 경우는 Object[]로 받아서 변환시키거나, 결과매핑을 위해서 엔티티에 @SqlResultSetMapping을 세팅해야함   

### JDBCTemplate으로 쿼리 날리기
```java
String sql = "select idx, code, name from goods where price -> 'L' = '27900'";
List<Goods> resultList = jdbcTemplate.query(sql,new BeanPropertyRowMapper<>(Goods.class));
```

### StringTemplate 이용하기
postgreSQL의 jsonb관련 function,Operator    
참고 => https://www.postgresql.org/docs/9.5/functions-json.html
```java
StringTemplate condition1 = Expressions.stringTemplate("jsonb_extract_path_text({0},{1})", qGoods.price,"L");
       
List<GoodsDto> result = queryFactory.select(Projections.bean(GoodsDto.class,
                                qGoods.code,
                                qGoods.name
                                ))
                                .from(qGoods)
                                .where(condition1.eq("27900"))
                                .fetch();
```


