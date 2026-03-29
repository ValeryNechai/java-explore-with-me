package ru.practicum.ewm.category.service;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;

import java.util.Collection;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto request);

    void deleteCategory(Integer catId);

    CategoryDto updateCategory(Integer catId, NewCategoryDto request);

    Collection<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategoryById(Integer catId);
}
