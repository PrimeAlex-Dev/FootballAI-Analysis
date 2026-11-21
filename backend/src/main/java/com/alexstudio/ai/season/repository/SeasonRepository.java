package com.alexstudio.ai.season.repository;

import com.alexstudio.ai.season.entity.Season;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SeasonRepository extends JpaRepository<Season, Long>, JpaSpecificationExecutor<Season> {

    boolean existsByName(String name);

    Optional<Season> findByName(String name);
}
