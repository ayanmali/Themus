package com.delphi.delphi.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.delphi.delphi.entities.jobs.Job;
import com.delphi.delphi.utils.JobStatus;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {
    @Override
    @NonNull
    Optional<Job> findById(@NonNull String id);

    Optional<Job> findByStatus(JobStatus status);
}
