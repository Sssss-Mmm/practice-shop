package com.example.practice_shop;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.practice_shop.constant.EventStatus;
import com.example.practice_shop.entity.Event;
import com.example.practice_shop.entity.Venue;
import com.example.practice_shop.repository.EventRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** 공연 검색 쿼리를 실제 Postgres에서 실행. 조건 없이(전부 null) 부르는 메인 페이지 호출이 깨졌던 적이 있다. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class EventSearchQueryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired EventRepository eventRepository;
    @Autowired EntityManager em;

    @Test
    void 검색_조건이_없어도_조회되고_키워드는_대소문자_무시로_찾는다() {
        Venue venue = Venue.builder().name("v").build();
        em.persist(venue);
        em.persist(Event.builder().title("Blue Hour").status(EventStatus.ON_SALE).venue(venue).build());
        em.persist(Event.builder().title("Green Room").status(EventStatus.ON_SALE).venue(venue).build());
        em.flush();

        assertThat(eventRepository.searchEvents(null, null, null, null, null)).hasSize(2);
        assertThat(eventRepository.searchEvents("blue", null, null, null, null))
                .extracting(Event::getTitle).containsExactly("Blue Hour");
    }
}
