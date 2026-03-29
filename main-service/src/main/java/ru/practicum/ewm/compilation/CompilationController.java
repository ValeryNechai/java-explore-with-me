package ru.practicum.ewm.compilation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.service.CompilationService;

import java.util.Collection;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CompilationController {
    private final CompilationService compilationService;

    @GetMapping("/compilations")
    @ResponseStatus(HttpStatus.OK)
    public Collection<CompilationDto> getAllCompilations(@RequestParam Boolean pinned,
                                                         @RequestParam(defaultValue = "0") int from,
                                                         @RequestParam(defaultValue = "10") int size) {
        return compilationService.getAllCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto getCompilationById(@PathVariable Integer compId) {
        return compilationService.getCompilationById(compId);
    }

    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@RequestBody @Valid NewCompilationDto request) {
        return compilationService.createCompilation(request);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Integer compId) {
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable Integer compId,
                                            @RequestBody @Valid UpdateCompilationRequest request) {
        return compilationService.updateCompilation(compId, request);
    }
}