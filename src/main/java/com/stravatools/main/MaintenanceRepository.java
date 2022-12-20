package com.stravatools.main;

import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceRepository extends CassandraRepository<Maintenance, UUID> {}
