package io.jenkins.plugins;

import static org.mockito.Mockito.*;

import hudson.model.User;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jenkins.model.Jenkins;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MfaFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private HttpSession session;

    @Mock
    private FilterConfig filterConfig;

    @Mock
    private User user;

    @Mock
    private MfaUserProperty mfaProperty;

    @Mock
    private Jenkins jenkins;

    private MfaFilter filter;

    @Before
    public void setUp() {
        filter = new MfaFilter();
        when(request.getSession()).thenReturn(session);
    }

    @Test
    public void testInitAndDestroy() throws Exception {
        filter.init(filterConfig);
        filter.destroy();
        // Apenas verifica que não lança exceções
    }

    @Test
    public void testNonHttpRequestPassesThrough() throws Exception {
        ServletRequest nonHttpRequest = mock(ServletRequest.class);
        ServletResponse nonHttpResponse = mock(ServletResponse.class);

        filter.doFilter(nonHttpRequest, nonHttpResponse, chain);

        verify(chain).doFilter(nonHttpRequest, nonHttpResponse);
    }

    @Test
    public void testJenkinsNotInitializedPassesThrough() throws Exception {
        try (MockedStatic<Jenkins> mockedJenkins = mockStatic(Jenkins.class)) {
            mockedJenkins.when(Jenkins::getInstanceOrNull).thenReturn(null);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }

    @Test
    public void testNoUserLoggedInPassesThrough() throws Exception {
        try (MockedStatic<Jenkins> mockedJenkins = mockStatic(Jenkins.class);
                MockedStatic<User> mockedUser = mockStatic(User.class)) {

            mockedJenkins.when(Jenkins::getInstanceOrNull).thenReturn(jenkins);
            mockedUser.when(User::current).thenReturn(null);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }

    @Test
    public void testUserWithoutMfaEnabledPassesThrough() throws Exception {
        try (MockedStatic<Jenkins> mockedJenkins = mockStatic(Jenkins.class);
                MockedStatic<User> mockedUser = mockStatic(User.class)) {

            mockedJenkins.when(Jenkins::getInstanceOrNull).thenReturn(jenkins);
            mockedUser.when(User::current).thenReturn(user);
            when(user.getProperty(MfaUserProperty.class)).thenReturn(null);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }

    @Test
    public void testMfaEnabledButNotVerifiedRedirects() throws Exception {
        try (MockedStatic<Jenkins> mockedJenkins = mockStatic(Jenkins.class);
                MockedStatic<User> mockedUser = mockStatic(User.class)) {

            mockedJenkins.when(Jenkins::getInstanceOrNull).thenReturn(jenkins);
            mockedUser.when(User::current).thenReturn(user);
            when(user.getProperty(MfaUserProperty.class)).thenReturn(mfaProperty);
            when(mfaProperty.isMfaEnabled()).thenReturn(true);
            when(request.getRequestURI()).thenReturn("/jenkins/restricted");
            when(request.getContextPath()).thenReturn("/jenkins");

            filter.doFilter(request, response, chain);

            verify(response).sendRedirect("/jenkins/mfa-verify");
            verify(chain, never()).doFilter(request, response);
        }
    }

    // maybe more tests ...

}
