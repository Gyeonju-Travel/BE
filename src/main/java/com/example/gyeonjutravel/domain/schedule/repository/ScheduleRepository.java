package com.example.gyeonjutravel.domain.schedule.repository;

import com.example.gyeonjutravel.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
