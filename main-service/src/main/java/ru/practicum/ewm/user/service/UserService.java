package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.Collection;
import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest request);

    void deleteUser(Long userId);

    Collection<UserDto> getUsers(List<Long> ids, int from, int size);
}
