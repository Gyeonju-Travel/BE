package com.example.gyeonjutravel.domain.stamp.repository;

import com.example.gyeonjutravel.domain.stamp.entity.StampAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

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

    @Query("select distinct album from StampAlbum album "
            + "left join fetch album.photos "
            + "where album.member.id = :memberId")
    List<StampAlbum> findAllWithPhotosByMemberId(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            delete from stamp_album_photos
            where album_id in (
                select id from stamp_albums
                where member_id = :memberId and schedule_id in (:scheduleIds)
            )
            """, nativeQuery = true)
    void deletePhotosByMemberIdAndScheduleIdIn(
            @Param("memberId") Long memberId,
            @Param("scheduleIds") List<Long> scheduleIds
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from StampAlbum album "
            + "where album.member.id = :memberId and album.schedule.id in :scheduleIds")
    void deleteAllByMemberIdAndScheduleIdIn(
            @Param("memberId") Long memberId,
            @Param("scheduleIds") List<Long> scheduleIds
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            delete from stamp_album_photos
            where album_id in (
                select id from stamp_albums where member_id = :memberId
            )
            """, nativeQuery = true)
    void deletePhotosByMemberId(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from StampAlbum album where album.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
