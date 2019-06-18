package io.codeka.gaia.controller;

import io.codeka.gaia.bo.Job;
import io.codeka.gaia.bo.Settings;
import io.codeka.gaia.repository.JobRepository;
import io.codeka.gaia.repository.StackRepository;
import io.codeka.gaia.repository.TerraformModuleRepository;
import io.codeka.gaia.runner.StackRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.UUID;


@Controller
public class StackController {

    private StackRepository stackRepository;

    private StackRunner stackRunner;

    private TerraformModuleRepository terraformModuleRepository;

    private Settings settings;

    private JobRepository jobRepository;

    @Autowired
    public StackController(StackRepository stackRepository, StackRunner stackRunner, TerraformModuleRepository terraformModuleRepository, Settings settings, JobRepository jobRepository) {
        this.stackRepository = stackRepository;
        this.stackRunner = stackRunner;
        this.terraformModuleRepository = terraformModuleRepository;
        this.settings = settings;
        this.jobRepository = jobRepository;
    }

    @GetMapping("/stacks")
    public String listStacks(){
        return "stacks";
    }

    @GetMapping("/stacks/{stackId}")
    public String editStack(@PathVariable String stackId, Model model){
        // checking if the stack exists
        // TODO throw an exception (404) if not
        if(stackRepository.existsById(stackId)){
            model.addAttribute("stackId", stackId);
            model.addAttribute("settings", settings);
        }
        return "stack";
    }

    @GetMapping("/stacks/{stackId}/apply")
    public String applyStack(@PathVariable String stackId, Model model){
        // checking if the stack exists
        // TODO throw an exception (404) if not
        if(stackRepository.existsById(stackId)){
            model.addAttribute("stackId", stackId);
        }

        // create a new job
        var job = new Job();
        job.setId(UUID.randomUUID().toString());
        job.setStackId(stackId);
        job.setDateTime(LocalDateTime.now());

        model.addAttribute("jobId", job.getId());

        // get the stack
        var stack = this.stackRepository.findById(stackId).get();
        // get the module
        var module = this.terraformModuleRepository.findById(stack.getModuleId()).get();

        this.stackRunner.apply(job, module, stack);

        return "job";
    }

    @GetMapping("/stacks/{stackId}/preview")
    public String previewStack(@PathVariable String stackId, Model model){
        // checking if the stack exists
        // TODO throw an exception (404) if not
        if(stackRepository.existsById(stackId)){
            model.addAttribute("stackId", stackId);
        }

        // create a new job
        var job = new Job();
        job.setId(UUID.randomUUID().toString());
        job.setStackId(stackId);
        job.setDateTime(LocalDateTime.now());

        model.addAttribute("jobId", job.getId());

        // get the stack
        var stack = this.stackRepository.findById(stackId).get();
        // get the module
        var module = this.terraformModuleRepository.findById(stack.getModuleId()).get();

        this.stackRunner.plan(job, module, stack);

        return "job";
    }

    @GetMapping("/stacks/{stackId}/jobs/{jobId}")
    public String viewJob(@PathVariable String stackId, @PathVariable String jobId, Model model){
        // checking if the stack exists
        // TODO throw an exception (404) if not
        if(stackRepository.existsById(stackId)){
            model.addAttribute("stackId", stackId);
        }
        if(jobRepository.existsById(jobId)){
            model.addAttribute("jobId", jobId);
        }

        return "job";
    }

    @GetMapping("/api/stacks/{stackId}/jobs/{jobId}")
    @ResponseBody
    public Job getJob(@PathVariable String stackId, @PathVariable String jobId){
        return this.stackRunner.getJob(jobId);
    }

    @GetMapping("/stacks/{stackId}/stop")
    public String stopStack(@PathVariable String stackId, Model model){
        // checking if the stack exists
        // TODO throw an exception (404) if not
        if(stackRepository.existsById(stackId)){
            model.addAttribute("stackId", stackId);
        }

        // create a new job
        var job = new Job();
        job.setId(UUID.randomUUID().toString());
        job.setStackId(stackId);
        job.setDateTime(LocalDateTime.now());

        model.addAttribute("jobId", job.getId());

        // get the stack
        var stack = this.stackRepository.findById(stackId).get();
        // get the module
        var module = this.terraformModuleRepository.findById(stack.getModuleId()).get();

        this.stackRunner.stop(job, module, stack);

        return "job";
    }

}