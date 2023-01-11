package com.stravatools.main.model.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import com.stravatools.main.model.Muting;

@Repository
public interface MutingRepository extends CassandraRepository<Muting, UUID> {

    List<Muting> findByUsername(String username);

    
}
