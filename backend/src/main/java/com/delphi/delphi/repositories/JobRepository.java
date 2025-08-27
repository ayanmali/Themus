package com.delphi.delphi.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.Job;
import com.delphi.delphi.utils.enums.JobStatus;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
    @Override
    @NonNull
    Optional<Job> findById(@NonNull UUID id);

    Page<Job> findByStatus(JobStatus status, Pageable pageable);
}
