package io.codeka.gaia.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Transient;

import java.io.StringWriter;
import java.io.Writer;

/**
 * A job is the instanciation of a stack
 */
public class Job {

    private String id;

    private String stackId;

    @Transient
    private StringWriter stringWriter = new StringWriter();

    private String logs;

    private JobStatus jobStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogs() {
        if(jobStatus == JobStatus.FINISHED || jobStatus == JobStatus.FAILED){
            return logs;
        }
        return stringWriter.toString();
    }

    @JsonIgnore
    public Writer getLogsWriter(){
        return stringWriter;
    }

    public JobStatus getStatus(){
        return this.jobStatus;
    }

    public void start() {
        this.jobStatus = JobStatus.RUNNING;
    }

    public void end(){
        this.jobStatus = JobStatus.FINISHED;
        // getting final logs
        this.logs = this.stringWriter.toString();
    }

    public void fail() {
        this.jobStatus = JobStatus.FAILED;
        // getting final logs
        this.logs = this.stringWriter.toString();
    }

    public String getStackId() {
        return stackId;
    }

    public void setStackId(String stackId) {
        this.stackId = stackId;
    }
}
