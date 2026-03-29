package ru.practicum.ewm.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.user.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);

    boolean existsByUserId(Integer userId);
}
