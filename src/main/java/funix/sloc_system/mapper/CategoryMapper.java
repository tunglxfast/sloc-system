package funix.sloc_system.mapper;

import funix.sloc_system.dto.CategoryDTO;
import funix.sloc_system.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<CategoryDTO> toDTO(List<Category> categories) {
        if (categories == null) {
            return null;
        }
        return categories.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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

    public List<Category> toEntity(List<CategoryDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
} 