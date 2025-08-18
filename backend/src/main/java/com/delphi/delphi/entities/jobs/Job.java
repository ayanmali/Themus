package com.delphi.delphi.entities.jobs;

import java.io.Serializable;

import com.delphi.delphi.utils.JobStatus;
import com.delphi.delphi.utils.JobType;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

//@MappedSuperclass
public class Job implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String jobId;

    private JobStatus status;  // PENDING, RUNNING, COMPLETED, FAILED

    private JobType jobType;

    @Lob
    private String result;

    public Job() {}

    public Job(String jobId, JobStatus status) {
        this.jobId = jobId;
        this.status = status;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }    

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }
    
}
