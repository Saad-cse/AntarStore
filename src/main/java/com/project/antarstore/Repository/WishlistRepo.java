package com.project.antarstore.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.antarstore.Model.Products;
import com.project.antarstore.Model.Users;
import com.project.antarstore.Model.Wishlist;

public interface WishlistRepo extends JpaRepository<Wishlist, Long> {

	List<Wishlist> findAllByUser(Users user);

	Optional<Wishlist> findByUserAndProduct(Users user, Products product);

	boolean existsByUserAndProduct(Users user, Products product);

	long countByUser(Users user);
}
