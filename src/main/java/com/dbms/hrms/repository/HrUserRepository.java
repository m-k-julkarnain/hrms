package com.dbms.hrms.repository;

import com.dbms.hrms.model.HrUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HrUserRepository extends JpaRepository<HrUser, Integer> {
    Optional<HrUser> findByUsername(String username);
}
