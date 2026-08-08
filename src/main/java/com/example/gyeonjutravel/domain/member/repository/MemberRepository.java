package com.example.gyeonjutravel.domain.member.repository;

import com.example.gyeonjutravel.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    @Modifying
    @Query(value = "delete from place_bookmarks where member_id = :memberId", nativeQuery = true)
    void deleteBookmarksByMemberId(@Param("memberId") Long memberId);
}
