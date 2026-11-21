package com.alexstudio.ai.season.repository;

import com.alexstudio.ai.season.entity.Season;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonRepository extends JpaRepository<Season, Long> {

    boolean existsByName(String name);

    Optional<Season> findByName(String name);
}
