package com.ktb.community.repository;

import com.ktb.community.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> { }
