package io.codeka.gaia.stacks.controller;

import io.codeka.gaia.modules.repository.TerraformModuleRepository;
import io.codeka.gaia.runner.StackRunner;
import io.codeka.gaia.stacks.bo.Job;
import io.codeka.gaia.stacks.bo.JobType;
import io.codeka.gaia.stacks.repository.JobRepository;
import io.codeka.gaia.stacks.repository.StackRepository;
import io.codeka.gaia.teams.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StackController {

    private StackRepository stackRepository;

    private StackRunner stackRunner;

    private TerraformModuleRepository terraformModuleRepository;

    private JobRepository jobRepository;

    @Autowired
    public StackController(StackRepository stackRepository, StackRunner stackRunner, TerraformModuleRepository terraformModuleRepository, JobRepository jobRepository) {
        this.stackRepository = stackRepository;
        this.stackRunner = stackRunner;
        this.terraformModuleRepository = terraformModuleRepository;
        this.jobRepository = jobRepository;
    }

//    @GetMapping("/stacks")
    public String listStacks() {
        return "stacks";
    }

//    @GetMapping("/stacks/{stackId}/{jobType}")
    public String launchJob(@PathVariable String stackId, @PathVariable JobType jobType, Model model, User user) {
        // get the stack
        var stack = this.stackRepository.findById(stackId).orElseThrow(StackNotFoundException::new);
        model.addAttribute("stackId", stackId);

        // get the module
        var module = this.terraformModuleRepository.findById(stack.getModuleId()).orElseThrow();

        // create a new job
        var job = new Job(jobType, stackId, user);
        job.setTerraformImage(module.getTerraformImage());
        jobRepository.save(job);
        model.addAttribute("jobId", job.getId());

        return "job";
    }

//    @GetMapping("/stacks/{stackId}/jobs/{jobId}")
    public String viewJob(@PathVariable String stackId, @PathVariable String jobId, Model model) {
        // checking if the stack exists
        // TODO throw an exception (404) if not
        if (stackRepository.existsById(stackId)) {
            model.addAttribute("stackId", stackId);
        }
        if (jobRepository.existsById(jobId)) {
            model.addAttribute("jobId", jobId);
        }

        model.addAttribute("edition", true);
        return "job";
    }

    @GetMapping("/api/stacks/{stackId}/jobs/{jobId}")
    @ResponseBody
    public Job getJob(@PathVariable String stackId, @PathVariable String jobId) {
        return this.stackRunner.getJob(jobId);
    }

}
