package com.project.antarstore.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.antarstore.Model.SavedAddress;
import com.project.antarstore.Model.Users;

public interface SavedAddressRepo extends JpaRepository<SavedAddress, Long> {

	SavedAddress findByUserAndActiveTrue(Users user);

}
