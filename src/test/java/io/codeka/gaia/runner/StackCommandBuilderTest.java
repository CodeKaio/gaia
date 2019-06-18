package io.codeka.gaia.runner;

import io.codeka.gaia.bo.Settings;
import io.codeka.gaia.bo.Stack;
import io.codeka.gaia.bo.TerraformModule;
import io.codeka.gaia.bo.TerraformVariable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StackCommandBuilderTest {

    @Test
    void buildApplyCommand_shouldGenerateASimpleApplyCommand(){
        var stackCommandBuilder = new StackCommandBuilder(new Settings());

        var module = new TerraformModule();
        module.setVariables(Collections.emptyList());

        var stack = new Stack();

        var applyCommand = stackCommandBuilder.buildApplyCommand(stack, module);

        assertEquals("terraform apply --auto-approve ", applyCommand);
    }

    @Test
    void buildApplyCommand_shouldGenerateASingleVariableApplyCommand(){
        var stackCommandBuilder = new StackCommandBuilder(new Settings());

        var module = new TerraformModule();
        var variable = new TerraformVariable();
        variable.setName("test");
        module.setVariables(List.of(variable));

        var stack = new Stack();
        stack.setVariableValues(Map.of("test", "value"));

        var applyCommand = stackCommandBuilder.buildApplyCommand(stack, module);

        assertEquals("terraform apply --auto-approve -var \"test=value\" ", applyCommand);
    }

    @Test
    void buildApplyCommand_shouldGenerateAMultipleVariableApplyCommand(){
        var stackCommandBuilder = new StackCommandBuilder(new Settings());

        var module = new TerraformModule();
        var variable = new TerraformVariable();
        variable.setName("test");
        var variable2 = new TerraformVariable();
        variable2.setName("test2");
        module.setVariables(List.of(variable, variable2));

        var stack = new Stack();
        stack.setVariableValues(Map.of("test", "value", "test2", "value2"));

        var applyCommand = stackCommandBuilder.buildApplyCommand(stack, module);

        assertEquals("terraform apply --auto-approve -var \"test=value\" -var \"test2=value2\" ", applyCommand);
    }

    @Test
    void buildApplyCommand_shouldUseDefaultVariableValues(){
        var stackCommandBuilder = new StackCommandBuilder(new Settings());

        var module = new TerraformModule();
        var variable = new TerraformVariable();
        variable.setName("test");
        variable.setDefaultValue("defaultValue");
        module.setVariables(List.of(variable));

        var stack = new Stack();
        stack.setVariableValues(Collections.emptyMap());

        var applyCommand = stackCommandBuilder.buildApplyCommand(stack, module);

        assertEquals("terraform apply --auto-approve -var \"test=defaultValue\" ", applyCommand);
    }

    @Test
    void buildApplyScript_shouldGenerateAFullScript(){
        var stackCommandBuilder = new StackCommandBuilder(new Settings());

        TerraformModule module = moduleWithDirectory();

        var stack = new Stack();
        var script = stackCommandBuilder.buildApplyScript(stack, module);

        assertTrue(script.contains("git clone git://test module\ncd module\ncd directory\necho 'generating backend configuration'"));
        assertTrue(script.contains("terraform version\nterraform init\nterraform apply"));
    }

    @Test
    void buildApplyScript_shouldGenerateAFullScript_forAModuleWithoutDirectory(){
        var stackCommandBuilder = new StackCommandBuilder(new Settings());

        TerraformModule module = moduleWithoutDirectory();

        var stack = new Stack();
        var script = stackCommandBuilder.buildApplyScript(stack, module);

        assertTrue(script.contains("git clone git://test module\ncd module\necho 'generating backend configuration'"));
        assertTrue(script.contains("terraform version\nterraform init\nterraform apply"));
    }

    @Test
    void buildPlanScript_shouldGenerateAFullScript(){
        var stackCommandBuilder = new StackCommandBuilder(new Settings());

        TerraformModule module = moduleWithDirectory();

        var stack = new Stack();
        var script = stackCommandBuilder.buildPlanScript(stack, module);

        assertTrue(script.contains("git clone git://test module\ncd module\ncd directory\necho 'generating backend configuration'"));
        assertTrue(script.contains("terraform version\nterraform init\nterraform plan"));
    }

    @Test
    void buildPlanScript_shouldGenerateAFullScript_forAModuleWithoutDirectory(){
        var stackCommandBuilder = new StackCommandBuilder(new Settings());

        TerraformModule module = moduleWithoutDirectory();

        var stack = new Stack();
        var script = stackCommandBuilder.buildPlanScript(stack, module);

        assertTrue(script.contains("git clone git://test module\ncd module\necho 'generating backend configuration'"));
        assertTrue(script.contains("terraform version\nterraform init\nterraform plan"));
    }

    @Test
    void buildDestroyScript_shouldGenerateAFullScript(){
        var stackCommandBuilder = new StackCommandBuilder(new Settings());

        TerraformModule module = moduleWithDirectory();

        var stack = new Stack();
        var script = stackCommandBuilder.buildDestroyScript(stack, module);

        assertTrue(script.contains("git clone git://test module\ncd module\ncd directory\necho 'generating backend configuration'"));
        assertTrue(script.contains("terraform version\nterraform init\nterraform destroy"));
    }

    @Test
    void buildDestroyScript_shouldGenerateAFullScript_forAModuleWithoutDirectory(){
        var stackCommandBuilder = new StackCommandBuilder(new Settings());

        TerraformModule module = moduleWithoutDirectory();

        var stack = new Stack();
        var script = stackCommandBuilder.buildDestroyScript(stack, module);

        assertTrue(script.contains("git clone git://test module\ncd module\necho 'generating backend configuration'"));
        assertTrue(script.contains("terraform version\nterraform init\nterraform destroy"));
    }

    @NotNull
    private TerraformModule moduleWithDirectory() {
        var module = new TerraformModule();
        module.setGitRepositoryUrl("git://test");
        module.setDirectory("directory");

        var variable = new TerraformVariable();
        variable.setName("test");
        variable.setDefaultValue("defaultValue");
        module.setVariables(List.of(variable));
        return module;
    }

    @NotNull
    private TerraformModule moduleWithoutDirectory() {
        var module = new TerraformModule();
        module.setGitRepositoryUrl("git://test");

        var variable = new TerraformVariable();
        variable.setName("test");
        variable.setDefaultValue("defaultValue");
        module.setVariables(List.of(variable));
        return module;
    }

}