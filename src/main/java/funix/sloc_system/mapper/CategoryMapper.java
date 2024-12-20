package funix.sloc_system.mapper;

import funix.sloc_system.dto.CategoryDTO;
import funix.sloc_system.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {
    
    public CategoryDTO toDTO(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        
        return dto;
    }

    public Category toEntity(CategoryDTO dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        
        return category;
    }

    public List<CategoryDTO> toDTO(List<Category> categories) {
        return categories.stream().map(this::toDTO).toList();
    }

    public List<Category> toEntity(List<CategoryDTO> categoryDTOList) {
        return categoryDTOList.stream().map(this::toEntity).toList();
    }
} 