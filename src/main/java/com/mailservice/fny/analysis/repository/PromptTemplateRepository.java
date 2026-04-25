package com.mailservice.fny.analysis.repository;

import com.mailservice.fny.analysis.entity.PromptTemplate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, String> {

    Optional<PromptTemplate> findFirstByPromptCodeAndPromptTypeAndActiveTrueOrderByVersionDesc(
            String promptCode,
            String promptType
    );
}
