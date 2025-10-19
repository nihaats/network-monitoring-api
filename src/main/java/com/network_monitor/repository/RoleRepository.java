package com.network_monitor.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.network_monitor.model.ERole;
import com.network_monitor.model.Role;

public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(ERole name);
}
