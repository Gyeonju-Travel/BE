package com.example.gyeonjutravel.domain.stamp.service;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.pet.repository.PetRepository;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.example.gyeonjutravel.domain.schedule.entity.Schedule;
import com.example.gyeonjutravel.domain.schedule.repository.ScheduleRepository;
import com.example.gyeonjutravel.domain.stamp.entity.StampAlbum;
import com.example.gyeonjutravel.domain.stamp.repository.StampAlbumRepository;
import com.example.gyeonjutravel.domain.stamp.repository.PlaceVisitRepository;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import com.example.gyeonjutravel.global.storage.ImageStorageService;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class PrivateTravelImageTest {
    private final ScheduleRepository schedules = mock(ScheduleRepository.class);
    private final StampAlbumRepository albums = mock(StampAlbumRepository.class);
    private final ImageStorageService images = mock(ImageStorageService.class);
    private final StampService service = new StampService(schedules, mock(PlaceRepository.class),
            mock(com.example.gyeonjutravel.domain.place.service.PlaceCatalog.class),
            mock(PetRepository.class), albums, mock(PlaceVisitRepository.class), images);

    @Test
    void nonOwnerCannotReadAlbumOrSignPhotos() {
        when(schedules.findByIdAndMemberId(10L, 2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getAlbum(2L, 10L)).isInstanceOf(GeneralException.class);
        verifyNoInteractions(images, albums);
    }

    @Test
    void ownerGetsSignedAlbumAndThumbnailWithoutMutatingPhotos() {
        Schedule schedule = mock(Schedule.class);
        when(schedule.getId()).thenReturn(10L);
        when(schedule.isStarted()).thenReturn(true);
        when(schedule.getTravelDate()).thenReturn(LocalDate.now().minusDays(1));
        when(schedule.getStartedAt()).thenReturn(LocalDate.now().minusDays(1).atStartOfDay());
        Pet pet = Pet.builder().name("Test").profileImageUrl("pet-images/profile.png").build();
        StampAlbum album = new StampAlbum(schedule, mock(Member.class), pet);
        album.replacePhotos(List.of("stamp-albums/one.png", "stamp-albums/two.png"));
        when(schedules.findByIdAndMemberId(10L, 1L)).thenReturn(Optional.of(schedule));
        when(albums.findByScheduleIdAndMemberId(10L, 1L)).thenReturn(Optional.of(album));
        when(images.readUrl(anyString())).thenAnswer(call -> "signed:" + call.getArgument(0));

        var response = service.getAlbum(1L, 10L);
        assertThat(response.petProfileImageUrl()).isEqualTo("signed:pet-images/profile.png");
        assertThat(response.photoUrls()).containsExactly("signed:stamp-albums/one.png", "signed:stamp-albums/two.png");
        when(albums.findAllWithPhotosByMemberId(1L)).thenReturn(List.of(album));
        when(schedules.findStartedSchedulesWithItemsByMemberId(1L)).thenReturn(List.of(schedule));
        var records = service.getTravelRecords(1L);
        assertThat(records.records().get(0).photoUrl()).isEqualTo("signed:stamp-albums/one.png");
        verify(images, times(2)).readUrl("stamp-albums/one.png");
        assertThat(album.getPhotos().get(0).getImageUrl()).isEqualTo("stamp-albums/one.png");
    }
}
