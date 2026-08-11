package com.example.gyeonjutravel.domain.stamp.entity;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.schedule.entity.Schedule;
import com.example.gyeonjutravel.global.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "stamp_albums")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StampAlbum extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false, unique = true)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(name = "total_distance_meters", nullable = false)
    private long totalDistanceMeters;

    @Column(name = "footprint_count", nullable = false)
    private int footprintCount;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<StampAlbumPhoto> photos = new ArrayList<>();

    public StampAlbum(Schedule schedule, Member member, Pet pet) {
        this.schedule = schedule;
        this.member = member;
        this.pet = pet;
    }

    public void addDistance(long distanceMeters) {
        this.totalDistanceMeters += distanceMeters;
        this.footprintCount = (int) (this.totalDistanceMeters / 100);
    }

    public void replacePhotos(List<String> imageUrls) {
        this.photos.clear();
        for (int index = 0; index < imageUrls.size(); index++) {
            this.photos.add(new StampAlbumPhoto(this, imageUrls.get(index), index + 1));
        }
    }

}
