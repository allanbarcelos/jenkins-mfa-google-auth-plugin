package io.jenkins.plugins;

import static org.mockito.Mockito.*;

import hudson.model.User;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MfaEnforceFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private HttpSession session;

    @Mock
    private User user;

    @Mock
    private MfaUserProperty mfaProperty;

    @Mock
    private FilterConfig filterConfig;

    private MfaEnforceFilter filter;

    @Before
    public void setUp() {
        filter = new MfaEnforceFilter();
    }

    @Test
    public void testStaticResourcesBypass() throws Exception {
        when(request.getRequestURI()).thenReturn("/jenkins/static/some-resource.css");
        when(request.getContextPath()).thenReturn("/jenkins");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(response);
    }

    @Test
    public void testNoUserLoggedIn() throws Exception {
        when(request.getRequestURI()).thenReturn("/jenkins/some-page");
        when(request.getContextPath()).thenReturn("/jenkins");

        try (MockedStatic<User> mockedUser = mockStatic(User.class)) {
            mockedUser.when(User::current).thenReturn(null);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verifyNoInteractions(response);
        }
    }

    @Test
    public void testUserWithMfaDisabled() throws Exception {
        when(request.getRequestURI()).thenReturn("/jenkins/some-page");
        when(request.getContextPath()).thenReturn("/jenkins");

        try (MockedStatic<User> mockedUser = mockStatic(User.class)) {
            mockedUser.when(User::current).thenReturn(user);
            when(user.getProperty(MfaUserProperty.class)).thenReturn(null);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verifyNoInteractions(response);
        }
    }

    @Test
    public void testUserWithMfaEnabledButNotVerified() throws Exception {
        when(request.getRequestURI()).thenReturn("/jenkins/some-page");
        when(request.getContextPath()).thenReturn("/jenkins");
        when(request.getSession(false)).thenReturn(session);

        try (MockedStatic<User> mockedUser = mockStatic(User.class)) {
            mockedUser.when(User::current).thenReturn(user);
            when(user.getProperty(MfaUserProperty.class)).thenReturn(mfaProperty);
            when(mfaProperty.isMfaEnabled()).thenReturn(true);
            when(session.getAttribute("mfa-verified")).thenReturn(null);

            filter.doFilter(request, response, chain);

            verify(response).sendRedirect("/jenkins/mfa-verify/");
            verify(chain, never()).doFilter(request, response);
        }
    }

    // maybe more tests ...
}
