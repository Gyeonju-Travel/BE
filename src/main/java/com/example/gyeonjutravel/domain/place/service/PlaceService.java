package com.example.gyeonjutravel.domain.place.service;

import com.example.gyeonjutravel.domain.member.entity.Member;
import com.example.gyeonjutravel.domain.member.exception.MemberErrorCode;
import com.example.gyeonjutravel.domain.member.repository.MemberRepository;
import com.example.gyeonjutravel.domain.place.dto.response.MapPlacePageResponse;
import com.example.gyeonjutravel.domain.place.dto.response.MapPlaceResponse;
import com.example.gyeonjutravel.domain.place.dto.response.PlaceDetailResponse;
import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.place.entity.PlaceCategory;
import com.example.gyeonjutravel.domain.place.exception.PlaceErrorCode;
import com.example.gyeonjutravel.domain.place.repository.PlaceRepository;
import com.example.gyeonjutravel.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceCatalog placeCatalog;
    private final MemberRepository memberRepository;

    public MapPlacePageResponse search(
            List<PlaceCategory> categories,
            String keyword,
            int page,
            int size
    ) {
        validatePage(page, size);
        boolean includeAttractions = categories == null || categories.isEmpty()
                || categories.contains(PlaceCategory.ATTRACTION);
        List<Place> matches = placeCatalog.all(includeAttractions).stream()
                .filter(ClosedPlaces::isOpen)
                .filter(place -> categories == null || categories.isEmpty() || categories.contains(place.getCategory()))
                .filter(place -> matchesKeyword(place, keyword))
                .sorted(java.util.Comparator.comparing(Place::getId))
                .toList();
        long offset = (long) page * size;
        List<Place> selected = matches.stream().skip(offset).limit(size).toList();
        List<MapPlaceResponse> content = placeCatalog.resolveAll(selected).stream().map(MapPlaceResponse::from).toList();
        return new MapPlacePageResponse(content, page, size, matches.size(),
                (int) ((matches.size() + (long) size - 1) / size));
    }

    private boolean matchesKeyword(Place place, String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        List<String> fields = Stream.of(place.getName(), place.getArea(), place.getRoadAddress(),
                        place.getLotAddress(), place.getOriginalCategory(), place.getDetailCategory())
                .filter(java.util.Objects::nonNull).map(value -> value.toLowerCase(Locale.ROOT)).toList();
        return keywordTokens(keyword).stream().allMatch(token ->
                fields.stream().anyMatch(value -> value.contains(token.toLowerCase(Locale.ROOT)))
                        || categoriesMatching(token).contains(place.getCategory()));
    }

    public PlaceDetailResponse getDetail(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND));
        if (!ClosedPlaces.isOpen(place)) {
            throw new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
        return PlaceDetailResponse.from(placeCatalog.resolve(place));
    }

    @Transactional
    public MapPlaceResponse saveBookmark(Member authenticatedMember, Long placeId) {
        Member member = findMember(authenticatedMember.getId());
        Place place = findPlace(placeId);
        if (!member.addBookmark(place)) {
            throw new GeneralException(PlaceErrorCode.BOOKMARK_ALREADY_EXISTS);
        }
        return MapPlaceResponse.from(placeCatalog.resolve(place));
    }

    public List<MapPlaceResponse> getBookmarks(
            Member authenticatedMember,
            List<PlaceCategory> categories
    ) {
        List<Place> bookmarks = categories == null || categories.isEmpty()
                ? placeRepository.findBookmarkedPlacesByMemberId(authenticatedMember.getId())
                : placeRepository.findBookmarkedPlacesByMemberIdAndCategories(
                        authenticatedMember.getId(), categories
                );
        return placeCatalog.resolveAll(bookmarks)
                .stream()
                .filter(ClosedPlaces::isOpen)
                .map(MapPlaceResponse::from)
                .toList();
    }

    @Transactional
    public void deleteBookmarks(Member authenticatedMember, List<Long> placeIds) {
        if (placeIds == null || placeIds.isEmpty()
                || placeIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new GeneralException(PlaceErrorCode.INVALID_BOOKMARK_PLACE_IDS);
        }
        Member member = findMember(authenticatedMember.getId());
        if (!member.removeBookmarks(Set.copyOf(placeIds))) {
            throw new GeneralException(PlaceErrorCode.BOOKMARK_NOT_FOUND);
        }
        memberRepository.flush();
    }

    private List<String> keywordTokens(String keyword) {
        return Stream.of(keyword.trim().split("\\s+"))
                .filter(token -> !token.isBlank())
                .distinct()
                .toList();
    }

    private List<PlaceCategory> categoriesMatching(String token) {
        String normalizedToken = token.toLowerCase(Locale.ROOT);
        return Stream.of(PlaceCategory.values())
                .filter(category -> category.name().toLowerCase(Locale.ROOT).contains(normalizedToken)
                        || category.getLabel().toLowerCase(Locale.ROOT).contains(normalizedToken))
                .toList();
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 200) {
            throw new GeneralException(PlaceErrorCode.INVALID_PAGE_SIZE);
        }
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private Place findPlace(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND));
        if (!ClosedPlaces.isOpen(place)) {
            throw new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
        return place;
    }

}
