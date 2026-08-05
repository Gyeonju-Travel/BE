package com.example.gyeonjutravel.domain.place.repository;

import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long>, JpaSpecificationExecutor<Place> {

    long countByCategory(PlaceCategory category);

    @Query("select place from Member member join member.bookmarkedPlaces place "
            + "where member.id = :memberId order by place.id desc")
    List<Place> findBookmarkedPlacesByMemberId(@Param("memberId") Long memberId);

    @Query("select place from Member member join member.bookmarkedPlaces place "
            + "where member.id = :memberId and place.category in :categories order by place.id desc")
    List<Place> findBookmarkedPlacesByMemberIdAndCategories(
            @Param("memberId") Long memberId,
            @Param("categories") List<PlaceCategory> categories
    );

    @Query("select place from Member member join member.bookmarkedPlaces place "
            + "where member.id = :memberId and place.id in :placeIds")
    List<Place> findBookmarkedPlacesByMemberIdAndIds(
            @Param("memberId") Long memberId,
            @Param("placeIds") List<Long> placeIds
    );
}
