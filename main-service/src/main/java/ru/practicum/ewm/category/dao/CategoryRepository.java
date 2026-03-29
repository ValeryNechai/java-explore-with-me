package ru.practicum.ewm.category.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsByName(String name);

    boolean existsById(Integer id);

    @Query("select COUNT(e) FROM Event e WHERE e.category.id = ?1")
    int findEventsCountByCategories(Integer catId);
}
