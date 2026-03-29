package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dao.CategoryRepository;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto request) {
        validateCategoryName(request.getName());

        Category category = CategoryMapper.mapToCategory(request);

        Category createdCategory = categoryRepository.save(category);
        log.debug("Категория {} успешно добавлена.", createdCategory.getName());

        return CategoryMapper.mapToCategoryDto(createdCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Integer catId) {
        int countEventsByCategory = categoryRepository.findEventsCountByCategories(catId);
        if (countEventsByCategory > 0) {
            throw new ConflictException(
                    String.format("Невозможно удалить! С данной категорией создано %d событие(я).",
                            countEventsByCategory)
            );
        }
        validateCategoryId(catId);

        categoryRepository.deleteById(catId);
        log.debug("Категория успешно удалена.");
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Integer catId, NewCategoryDto request) {
        validateCategoryId(catId);
        validateCategoryName(request.getName());

        Category category = CategoryMapper.mapToCategory(request);

        Category updatedCategory = categoryRepository.save(category);
        log.debug("Категория {} успешно обновлена.", updatedCategory.getName());

        return CategoryMapper.mapToCategoryDto(updatedCategory);
    }

    @Override
    public Collection<CategoryDto> getCategories(int from, int size) {
        List<Category> categories = categoryRepository.findAll();
        validateFromAndSize(categories, from, size);

        return categories.stream()
                .skip(from)
                .limit(size)
                .map(CategoryMapper::mapToCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoryById(Integer catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Категория с id: %s не найдена.", catId)
                ));

        return CategoryMapper.mapToCategoryDto(category);
    }

    private void validateCategoryName(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new ConflictException(
                    String.format("Пользователь с name: %s уже существует.", name)
            );
        }
    }

    private void validateCategoryId(Integer catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException(
                    String.format("Категория с id: %s не найдена.", catId)
            );
        }
    }

    private void validateFromAndSize(List<Category> categories, int from, int size) {
        if (from > categories.size()) {
            throw new BadRequestException(
                    String.format(
                            "BadRequest: from = %d, что больше длины списка categories = %d.",
                            from,
                            categories.size())
            );
        }

        if (from < 0) {
            throw new BadRequestException(
                    String.format("BadRequest: from = %d. From не может быть меньше 0.", from)
            );
        }
    }
}
