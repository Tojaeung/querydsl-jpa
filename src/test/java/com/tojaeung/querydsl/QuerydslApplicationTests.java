package com.tojaeung.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tojaeung.querydsl.domain.Member;
import com.tojaeung.querydsl.domain.QMember;
import com.tojaeung.querydsl.domain.Team;
import com.tojaeung.querydsl.dto.ComplexMemberDto;
import com.tojaeung.querydsl.dto.MemberDto;
import com.tojaeung.querydsl.dto.QMemberDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static com.tojaeung.querydsl.domain.QMember.member;
import static com.tojaeung.querydsl.domain.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {
    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void 쿼리dsl시작() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void 검색조건() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void 결과조회() {
        // 배열로 조회
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        // 단건 조회
        Member fetchOne = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member2"))
                .fetchOne();

        // 첫번째만 조회
        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        assertThat(fetch.get(0)).isEqualTo(fetchFirst);
    }

    @Test
    public void 정렬() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc().nullsFirst())  // null을 최대값 취급
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member member7 = result.get(2);

        assertThat(member5.getUsername()).isEqualTo(null);
        assertThat(member6.getUsername()).isEqualTo("member5");
        assertThat(member7.getUsername()).isEqualTo("member6");
    }

    @Test
    public void 페이징() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void 집합() {

        /*
         * select에 찍으면 튜플로 가져온다.
         * dto로 찍는 방법이 실무에서 많이 사용됨
         * */
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);  // 튜플이 모지??....

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    public void 그룹() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    // 디비에서 CASE문 사용은 좋지않음 백엔드 로직이나 프론트 로직에서 처리
    // 디비에서는 최소한의 필터링 그룹핑 등등 으로만....
    public void CASE문() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void 상수문자처리함수() {
        List<String> result = queryFactory
                // stringvalue 문자열로 변환
                // enum 처리할때 유리
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member2"))
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }

    }

    @Test
    public void 조인() {
        // 이너조인
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");

        // 세타조인(연관관계 없어도 조인)
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result2 = queryFactory
                .select(member)
                .from(member, team) // cross join
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result2)
                .extracting("username")
                .containsExactly("teamA", "teamB");

    }

    @Test
    public void 조인on절() {

        /*
         * 내부조인일때는 on, where 사용이 돌일하다. 되도록 where를 사용한다.
         * 외부조인은 on, where 사용 결과가 다르다 주의할것 !!
         * */
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))  // 필터링하는게 아니라 조인할거만 조인하는 기능
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void 페치조인() {
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println(findMember.toString());

    }

    @Test
    public void 서브쿼리() {
        // 서브쿼리 alias 달라야함, 새로 만들어줌
        QMember subMember = new QMember("subMember");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(subMember.age.max())
                                .from(subMember)
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(40);

        List<Member> result2 = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(subMember.age.avg())
                                .from(subMember)
                ))
                .fetch();

        assertThat(result2).extracting("age").containsExactly(30, 40);
    }

    @Test
    public void 프로젝션조회() {

        // setter조회 (@setter에 매칭해서 꽂아넣음, 매칭 안되면 null)
        List<MemberDto> setterResult = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age
                )).from(member)
                .fetch();

        System.out.println("setterResult = " + setterResult);

        // filed 조회 (DTO 필드값에 매칭해서 꽂아넣음, 매칭 안되면 null)
        List<MemberDto> fieldResult = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age
                )).from(member)
                .fetch();

        System.out.println("fieldResult = " + fieldResult);

        // constructor 조회 (생성자 타입에 맞춰서 꽂아넣음, 매칭 안되면 null)
        List<MemberDto> constructorResult = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age
                )).from(member)
                .fetch();

        System.out.println("constructorResult = " + constructorResult);

    }

    @Test
    public void 복잡한프로젝션조회() {
        // 서브쿼리 alias 위해서 생성
        QMember memberSub = new QMember("memberSub");

        List<ComplexMemberDto> result = queryFactory
                .select(Projections.constructor(ComplexMemberDto.class,
                        member.username.as("name"),
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(member)
                .fetch();

        System.out.println("result = " + result);
    }

    @Test
    public void 프로젝션어노테이션활용() {
        QMember memberSub = new QMember("memberSub");

        // 장점: 컴파일시점에서 오류 찾을수 있음
        // 단점: DTO 쿼리dsl에 의존하게 됨
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(
                        member.username,
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")))
                .from(member)
                .fetch();

        System.out.println("result = " + result);
    }

    @Test
    public void where동적쿼리() {
        String username = "member1";
        int age = 10;

        List<Member> result = searchMember(username, age);

        System.out.println("result = " + result);
    }

    private List<Member> searchMember(String usernameCond, int ageCond) {
        return queryFactory
                .selectFrom(member)
                // .where(usernameEq(usernameCond), ageEq(ageCond))
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    // 재사용 가능
    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    // 재사용 가능
    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    // 조립 가능
    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    @Test
    @Commit
    public void 수정삭제벌크연산() {
        Long updateCount = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        /*
         * 벌크 연산 후에는 영속성 컨텍스트를 초기화 시켜야한다.
         * 왜냐하면 영속성 컨텍스트를 거치지 않고 바로 디비로 쿼리되기 떄문이다.
         * */
        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();

        System.out.println("result = " + result);
    }
}
