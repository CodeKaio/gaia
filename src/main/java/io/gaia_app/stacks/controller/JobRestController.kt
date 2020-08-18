package io.gaia_app.stacks.controller

import io.gaia_app.modules.repository.TerraformModuleRepository
import io.gaia_app.runner.StackRunner
import io.gaia_app.stacks.bo.Job
import io.gaia_app.stacks.repository.JobRepository
import io.gaia_app.stacks.repository.StackRepository
import io.gaia_app.stacks.repository.StepRepository
import io.gaia_app.stacks.workflow.JobWorkflow
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/jobs")
class JobRestController(
        private val jobRepository: JobRepository,
        private val stackRepository: StackRepository,
        private val stackRunner: StackRunner,
        private val stepRepository: StepRepository) {

    @GetMapping(params = ["stackId"])
    fun jobs(@RequestParam stackId: String) = jobRepository.findAllByStackIdOrderByStartDateTimeDesc(stackId)

    @GetMapping("/{id}")
    fun job(@PathVariable id: String): Job {
        val runningJob = stackRunner.getJob(id);
        if (runningJob.isPresent) {
            return runningJob.get();
        }
        return jobRepository.findById(id).orElseThrow { JobNotFoundException() }
    }

    @PostMapping("/{id}/plan")
    fun plan(@PathVariable id: String) {
        val job = jobRepository.findById(id).orElseThrow { JobNotFoundException() }
        val stack = stackRepository.findById(job.stackId).orElseThrow()
        stackRunner.plan(JobWorkflow(job), stack.module, stack)
    }

    @PostMapping("/{id}/apply")
    fun apply(@PathVariable id: String) {
        val job = jobRepository.findById(id).orElseThrow { JobNotFoundException() }
        val stack = stackRepository.findById(job.stackId).orElseThrow()
        stackRunner.apply(JobWorkflow(job), stack.module, stack)
    }

    @PostMapping("/{id}/retry")
    fun retry(@PathVariable id: String) {
        val job = jobRepository.findById(id).orElseThrow { JobNotFoundException() }
        val stack = stackRepository.findById(job.stackId).orElseThrow()
        stackRunner.retry(JobWorkflow(job), stack.module, stack)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) {
        stepRepository.deleteByJobId(id)
        jobRepository.deleteById(id)
    }

}

@ResponseStatus(HttpStatus.NOT_FOUND)
internal class JobNotFoundException : RuntimeException()
