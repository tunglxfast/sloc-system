package funix.sloc_system.service;

import funix.sloc_system.dto.CategoryDTO;
import funix.sloc_system.entity.Category;
import funix.sloc_system.mapper.CategoryMapper;
import funix.sloc_system.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryMapper categoryMapper;

    public List<CategoryDTO> findAllCategoriesDTO() {
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toDTO(categories);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
