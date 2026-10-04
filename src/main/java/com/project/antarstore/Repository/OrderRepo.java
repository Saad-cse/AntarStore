package com.project.antarstore.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.antarstore.Model.Orders;
import com.project.antarstore.Model.Users;

public interface OrderRepo extends JpaRepository<Orders, Long> {

	boolean existsByOrderId(String orderId);

	List<Orders> findAllByUsersOrderByOrderedAtDesc(Users user);

	List<Orders> findAllByOrderByOrderedAtDesc();

}
