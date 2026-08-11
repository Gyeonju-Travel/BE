package com.example.gyeonjutravel.domain.stamp.repository;

import com.example.gyeonjutravel.domain.stamp.entity.StampAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StampAlbumRepository extends JpaRepository<StampAlbum, Long> {

    Optional<StampAlbum> findByScheduleIdAndMemberId(Long scheduleId, Long memberId);

    @Query("select coalesce(sum(album.totalDistanceMeters), 0) from StampAlbum album "
            + "where album.pet.id = :petId and album.member.id = :memberId")
    long sumTotalDistanceMetersByPetIdAndMemberId(
            @Param("petId") Long petId,
            @Param("memberId") Long memberId
    );

    @Query("select album from StampAlbum album "
            + "left join fetch album.photos "
            + "where album.schedule.id = :scheduleId and album.member.id = :memberId")
    Optional<StampAlbum> findWithPhotosByScheduleIdAndMemberId(
            @Param("scheduleId") Long scheduleId,
            @Param("memberId") Long memberId
    );

}
