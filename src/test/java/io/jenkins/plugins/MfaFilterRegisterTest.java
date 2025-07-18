package io.jenkins.plugins;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.PluginServletFilter;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MfaFilterRegisterTest {

    @Test
    public void testInitializationMilestone() throws Exception {
        // Verifica se a anotação está correta
        Initializer initializer =
                MfaFilterRegister.class.getDeclaredMethod("init").getAnnotation(Initializer.class);

        assertNotNull(initializer);
        assertEquals(InitMilestone.PLUGINS_STARTED, initializer.after());
    }

    @Test
    public void testFilterRegistration() throws Exception {
        try (MockedStatic<PluginServletFilter> pluginServletFilter = mockStatic(PluginServletFilter.class)) {
            // Configura o mock para resolver a ambiguidade
            pluginServletFilter
                    .when(() -> PluginServletFilter.addFilter(any(Filter.class)))
                    .thenAnswer(invocation -> null);

            // Executa o método de registro
            MfaFilterRegister.init();

            // Verifica se o filtro foi adicionado
            pluginServletFilter.verify(() -> PluginServletFilter.addFilter(isA(MfaFilter.class)));
        }
    }

    @Test
    public void testServletExceptionHandling() {
        try (MockedStatic<PluginServletFilter> pluginServletFilter = mockStatic(PluginServletFilter.class)) {
            // Configura para lançar exceção
            pluginServletFilter
                    .when(() -> PluginServletFilter.addFilter(any(Filter.class)))
                    .thenThrow(new ServletException("Test error"));

            // Verifica se a exceção é propagada
            ServletException exception = assertThrows(ServletException.class, MfaFilterRegister::init);

            assertEquals("Test error", exception.getMessage());
        }
    }
}
