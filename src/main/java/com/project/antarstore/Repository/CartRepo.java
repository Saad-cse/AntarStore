package com.project.antarstore.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.antarstore.Model.Cart;
import com.project.antarstore.Model.Products;
import com.project.antarstore.Model.Users;

public interface CartRepo extends JpaRepository<Cart, Long>{

	boolean existsByUserAndProduct(Users user, Products product);

	Optional<Cart> findByUserAndProductAndColorAndSize(Users user, Products product, String color, String size);

	List<Cart> findAllByUser(Users user);

}
