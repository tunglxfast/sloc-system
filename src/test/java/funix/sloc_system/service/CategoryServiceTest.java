package funix.sloc_system.service;

import funix.sloc_system.dto.CategoryDTO;
import funix.sloc_system.entity.Category;
import funix.sloc_system.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    private final List<String> CATEGORY_LIST = Arrays.asList(
      "Web Development",
      "Data Science",
      "Design",
      "Finance",
      "Programming",
      "Accounting"
      );

    @Test
    public void testFindAllCategoriesDTO() {
        List<CategoryDTO> categories = categoryService.findAllCategoriesDTO();
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
        assertTrue(categories.size()==6);
        for (CategoryDTO categoryDTO: categories) {
          CATEGORY_LIST.contains(categoryDTO.getName());
        }
    }

    @Test
    public void testGetAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
        assertTrue(categories.size()==6);

        for (Category category: categories) {
          CATEGORY_LIST.contains(category.getName());
        }
    }
} 