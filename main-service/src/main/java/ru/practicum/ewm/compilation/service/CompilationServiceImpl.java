package ru.practicum.ewm.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dao.CompilationRepository;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.events.dao.EventRepository;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public Collection<CompilationDto> getAllCompilations(Boolean pinned, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        Page<Compilation> compilationsPage;
        if (pinned == null) {
            compilationsPage = compilationRepository.findAll(pageable);
        } else {
            compilationsPage = compilationRepository.findByPinned(pinned, pageable);
        }

        return compilationsPage.stream()
                .map(CompilationMapper::mapToCompilationDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = validateCompilation(compId);

        return CompilationMapper.mapToCompilationDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto request) {
        validateTitle(request.getTitle());
        List<Event> events = new ArrayList<>();
        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            events = validateEvents(request.getEvents());
        }

        Compilation compilation = Compilation.builder()
                .title(request.getTitle())
                .pinned(request.getPinned())
                .events(events)
                .build();

        Compilation createdCompilation = compilationRepository.save(compilation);
        log.debug("Подборка {} успешно добавлена.", compilation.getTitle());

        return CompilationMapper.mapToCompilationDto(createdCompilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        validateCompilation(compId);

        compilationRepository.deleteById(compId);
        log.debug("Подборка с id = {}, успешно удалена.", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = validateCompilation(compId);

        if (request.hasEvents()) {
            List<Event> events = validateEvents(request.getEvents());
            compilation.setEvents(events);
        }
         if (request.hasPinned()) {
             validatePinned(request.getPinned());
             compilation.setPinned(request.getPinned());
         }

         if (request.hasTitle()) {
             validateTitle(request.getTitle());
             compilation.setTitle(request.getTitle());
         }

         return CompilationMapper.mapToCompilationDto(compilation);
    }

    private Compilation validateCompilation(Long compId) {
        if (compId == null) {
            throw new NotFoundException("Id не может быть null!");
        }

        return compilationRepository.findById(compId)
                .orElseThrow(() -> new BadRequestException(String.format("Подборка с id = %d не найдена!", compId)));
    }

    private List<Event> validateEvents(List<Long> eventIds) {
        List<Event> events = eventRepository.findByIdIn(eventIds);

        if (events.size() != eventIds.size()) {
            throw new BadRequestException(String.format(
                    "Передано %d eventIds, а найдено только %d events!", eventIds.size(), events.size())
            );
        }

        return events;
    }

    private void validateTitle(String title) {
        if (compilationRepository.existsByTitle(title)) {
            throw new ConflictException("Заголовок подборки должен быть уникален!");
        }
    }

    private void validatePinned(Boolean pinned) {
        if (pinned == null) {
            throw new BadRequestException("Pinned не может быть null");
        }
    }
}
