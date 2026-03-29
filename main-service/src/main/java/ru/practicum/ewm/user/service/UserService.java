package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.Collection;
import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest request);

    void deleteUser(Integer userId);

    Collection<UserDto> getUsers(List<Integer> ids, int from, int size);
}
