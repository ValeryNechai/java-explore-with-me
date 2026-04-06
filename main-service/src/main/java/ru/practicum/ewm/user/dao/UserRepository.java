package ru.practicum.ewm.user.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.rating.dto.UserWithRating;
import ru.practicum.ewm.user.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsById(Long id);

    @Query("SELECT new ru.practicum.ewm.rating.dto.UserWithRating(" +
            "u.id, u.name, COALESCE(SUM(r.value), 0)) " +
            "FROM User u " +
            "LEFT JOIN Event e ON u.id = e.initiator.id " +
            "LEFT JOIN RatingEvent r ON e.id = r.event.id " +
            "GROUP BY u.id, u.name " +
            "ORDER BY COALESCE(SUM(r.value), 0) DESC")
    List<UserWithRating> findUsersWithRating(Pageable pageable);
}
