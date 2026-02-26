package com.forgequeue.core.repository;

import com.forgequeue.core.domain.DeadLetterJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeadLetterJobRepository extends JpaRepository<DeadLetterJob, UUID> {
}