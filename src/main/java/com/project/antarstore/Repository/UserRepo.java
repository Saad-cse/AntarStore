package com.project.antarstore.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.antarstore.Model.Users;
import com.project.antarstore.Model.Users.UserRole;
import com.project.antarstore.Model.Users.UserStatus;

public interface UserRepo extends JpaRepository<Users, Long>{

	boolean existsByEmail(String email);

	Users findByEmail(String email);

	List<Users> findAllByRole(UserRole user);

	List<Users> findAllByRoleAndStatus(UserRole user, UserStatus status);
  
}
