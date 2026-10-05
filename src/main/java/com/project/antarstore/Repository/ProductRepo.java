package com.project.antarstore.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.antarstore.Model.Category;
import com.project.antarstore.Model.Products;

public interface ProductRepo extends JpaRepository<Products, Long>{

	List<Products> findAllByCategory(Category category);

	List<Products> findAllByVisibilityTrue();

	List<Products> findAllByCategoryAndVisibilityTrue(Category category);

	List<Products> findAllByVisibilityTrueAndProductNameContainingIgnoreCase(String keyword);

}
