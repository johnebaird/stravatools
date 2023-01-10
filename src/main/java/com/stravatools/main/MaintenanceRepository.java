package com.stravatools.main;

import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceRepository extends CassandraRepository<Maintenance, UUID> {

    List<Maintenance> findByUsername(String username);

    
}
