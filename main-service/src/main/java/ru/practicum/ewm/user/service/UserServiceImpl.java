package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.dao.UserRepository;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException(
                    String.format("Пользователь с email: %s уже существует.", request.getEmail())
            );
        }

        User user = UserMapper.mapToUser(request);
        User createdUser = userRepository.save(user);
        log.debug("Пользователь {} успешно добавлен.", user.getName());

        return UserMapper.mapToUserDto(createdUser);
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        if (!userRepository.existsByUserId(userId)) {
            throw new NotFoundException(
                    String.format("Пользователь с id: %s не найден.", userId)
            );
        }

        userRepository.deleteById(userId);
        log.debug("Пользователь с id = {} успешно удален.", userId);
    }

    @Override
    public Collection<UserDto> getUsers(List<Integer> ids, int from, int size) {
        List<User> users = userRepository.findAllById(ids);

        validateIds(ids, users);
        validateFromAndSize(ids, from, size);

        return users.stream()
                .skip(from)
                .limit(size)
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    private void validateIds(List<Integer> ids, List<User> users) {
        if (users.size() != ids.size()) {
            List<Integer> foundIds = users.stream()
                    .map(User::getId)
                    .toList();
            List<Integer> notFoundIds = ids.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            throw new BadRequestException(
                    String.format("BadRequest: пользователи с id = %s не найдены.", notFoundIds)
            );
        }
    }

    private void validateFromAndSize(List<Integer> ids, int from, int size) {
        if (from > ids.size()) {
            throw new BadRequestException(
                    String.format("BadRequest: from = %d, что больше списка длины ids = %d.", from, ids.size())
            );
        }

        if (from < 0) {
            throw new BadRequestException(
                    String.format("BadRequest: from = %d. From не может быть меньше 0.", from)
            );
        }
    }
}
