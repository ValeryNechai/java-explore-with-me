package ru.practicum.ewm.compilation.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.compilation.model.Compilation;

import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @EntityGraph(attributePaths = {"events"})
    Page<Compilation> findByPinned(Boolean pinned, Pageable pageable);

    boolean existsByTitle(String title);

    @EntityGraph(attributePaths = {"events"})
    Page<Compilation> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"events"})
    Optional<Compilation> findById(Long id);
}
