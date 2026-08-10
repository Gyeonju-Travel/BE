package com.example.gyeonjutravel.domain.home.service;

import com.example.gyeonjutravel.domain.pet.entity.Pet;
import com.example.gyeonjutravel.domain.place.entity.Place;
import com.example.gyeonjutravel.domain.home.enums.DogCondition;
import com.example.gyeonjutravel.domain.schedule.entity.DepartureArea;

import java.time.LocalDate;
import java.util.List;

public interface RecommendedPlaceSelector {

    List<Long> select(
            DepartureArea departureArea,
            LocalDate date,
            DogCondition condition,
            Pet representativePet,
            List<Place> places
    );
}
