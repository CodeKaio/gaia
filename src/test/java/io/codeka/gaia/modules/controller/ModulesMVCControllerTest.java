package io.codeka.gaia.modules.controller;

import io.codeka.gaia.modules.bo.TerraformModule;
import io.codeka.gaia.modules.repository.TerraformModuleGitRepository;
import io.codeka.gaia.modules.repository.TerraformModuleRepository;
import io.codeka.gaia.teams.Team;
import io.codeka.gaia.teams.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModulesMVCControllerTest {

    private ModulesMVCController controller;

    @Mock
    private TerraformModuleRepository moduleRepository;

    @Mock
    private TerraformModuleGitRepository moduleGitRepository;

    private User admin = new User("admin", null);

    private Team userTeam = new Team("Red Is Dead");

    private User standardUser = new User("Odile Deray", userTeam);

    @BeforeEach
    void setup() {
        controller = new ModulesMVCController(moduleRepository, moduleGitRepository);
    }

    @Test
    void description_shouldReturnRightView() {
        // given
        var module = new TerraformModule();
        var model = mock(Model.class);

        // when
        when(moduleRepository.findById(anyString())).thenReturn(Optional.of(module));
        var result = controller.description("TEST", model);

        // then
        assertThat(result).isEqualTo("module_description");
        verify(moduleRepository).findById("TEST");
        verify(model).addAttribute("module", module);
    }

    @Test
    void description_shouldThrowExceptionIfModuleNotFound() {
        // given
        var model = mock(Model.class);

        // when
        when(moduleRepository.findById(anyString())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> controller.description("TEST", model));

        // then
        verify(moduleRepository).findById("TEST");
        verify(model, never()).addAttribute(eq("module"), any());
    }

    @Test
    void importModule_shouldShowImportModuleView(){
        // when
        var res = controller.importModule();

        // then
        assertEquals("modules_import", res);
    }

}
