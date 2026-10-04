package com.project.antarstore.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.antarstore.Model.Category;

public interface CategoryRepo extends JpaRepository<Category, Long> {

	boolean existsByCategoryName(String categoryName);

	boolean existsByCategoryNameIgnoreCase(String categoryName);

	List<Category> findAllByIsVisible(boolean b);

}
