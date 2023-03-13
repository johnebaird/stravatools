package com.stravatools.main.model.repositories;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import com.stravatools.main.model.Bearer;

@Repository
public interface BearerRepository extends CassandraRepository<Bearer, UUID> {}