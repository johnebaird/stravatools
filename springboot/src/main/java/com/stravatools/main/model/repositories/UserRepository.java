package com.stravatools.main.model.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import com.stravatools.main.model.User;

@Repository
public interface UserRepository extends CassandraRepository<User, String> {
    
    User findByUsername(String username);

}

