package com.example.gyeonjutravel.domain.stamp.entity;

import com.example.gyeonjutravel.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "stamp_album_photos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StampAlbumPhoto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id", nullable = false)
    private StampAlbum album;

    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public StampAlbumPhoto(StampAlbum album, String imageUrl, int displayOrder) {
        this.album = album;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }
}
