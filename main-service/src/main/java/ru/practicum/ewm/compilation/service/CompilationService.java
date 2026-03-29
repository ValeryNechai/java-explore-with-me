package ru.practicum.ewm.compilation.service;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;

import java.util.Collection;

public interface CompilationService {
    Collection<CompilationDto> getAllCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(Integer compId);

    CompilationDto createCompilation(NewCompilationDto request);

    void deleteCompilation(Integer compId);

    CompilationDto updateCompilation(Integer compId, UpdateCompilationRequest request);
}
