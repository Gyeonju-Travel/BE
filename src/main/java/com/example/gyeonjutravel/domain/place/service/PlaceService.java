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
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;

    public MapPlacePageResponse search(
            List<PlaceCategory> categories,
            String keyword,
            int page,
            int size
    ) {
        validatePage(page, size);
        Specification<Place> specification = createSpecification(categories, keyword);
        Page<MapPlaceResponse> result = placeRepository.findAll(
                        specification,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"))
                )
                .map(MapPlaceResponse::from);
        return MapPlacePageResponse.from(result);
    }

    public PlaceDetailResponse getDetail(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND));
        if (!ClosedPlaces.isOpen(place)) {
            throw new GeneralException(PlaceErrorCode.PLACE_NOT_FOUND);
        }
        return PlaceDetailResponse.from(place);
    }

    @Transactional
    public MapPlaceResponse saveBookmark(Member authenticatedMember, Long placeId) {
        Member member = findMember(authenticatedMember.getId());
        Place place = findPlace(placeId);
        if (!member.addBookmark(place)) {
            throw new GeneralException(PlaceErrorCode.BOOKMARK_ALREADY_EXISTS);
        }
        return MapPlaceResponse.from(place);
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
        return bookmarks
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

    private Specification<Place> createSpecification(
            List<PlaceCategory> categories,
            String keyword
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").in(categories));
            }
            predicates.add(criteriaBuilder.not(root.get("name").in(ClosedPlaces.NAMES)));
            if (keyword != null && !keyword.isBlank()) {
                keywordTokens(keyword).forEach(token -> {
                    String pattern = "%" + token.toLowerCase(Locale.ROOT) + "%";
                    List<Predicate> tokenPredicates = new ArrayList<>(List.of(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("area")), pattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("roadAddress")), pattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("lotAddress")), pattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("originalCategory")), pattern),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("detailCategory")), pattern)
                    ));
                    List<PlaceCategory> matchingCategories = categoriesMatching(token);
                    if (!matchingCategories.isEmpty()) {
                        tokenPredicates.add(root.get("category").in(matchingCategories));
                    }
                    predicates.add(criteriaBuilder.or(tokenPredicates.toArray(Predicate[]::new)));
                });
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
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
