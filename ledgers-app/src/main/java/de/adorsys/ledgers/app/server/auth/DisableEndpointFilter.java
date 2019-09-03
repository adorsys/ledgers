package de.adorsys.ledgers.app.server.auth;

import de.adorsys.ledgers.middleware.rest.resource.ResetDataMgmtStaffAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DisableEndpointFilter extends OncePerRequestFilter {
    private static final String SUFFIX = "/**";
    private static final List<String> EXCLUDED_URLS = Collections.singletonList(ResetDataMgmtStaffAPI.BASE_PATH + SUFFIX);
    private static final List<String> PROFILES = Arrays.asList("develop", "sandbox");
    private static final AntPathMatcher matcher = new AntPathMatcher();
    private final Environment environment;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(isNotAccessibleEndpoint(request)) {
            log.info("This endpoint is not accessible");
            response.setStatus(HttpStatus.NOT_FOUND.value());
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isNotAccessibleEndpoint(HttpServletRequest request) {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        boolean isNoneMatchedProfile = PROFILES.stream()
                            .noneMatch(activeProfiles::contains);
        return isNoneMatchedProfile && isExcludedEndpoint(request);
    }

    private boolean isExcludedEndpoint(HttpServletRequest request) {
        return EXCLUDED_URLS.stream()
                       .anyMatch(p -> matcher.match(p, request.getServletPath()));
    }
}
