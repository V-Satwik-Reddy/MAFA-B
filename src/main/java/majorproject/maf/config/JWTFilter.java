package majorproject.maf.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.service.JWTService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import reactor.util.annotation.NonNull;

import java.io.IOException;
import java.util.Collections;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTService jwt;
    @Value("${jwt.secret:}")
    private String JWT_SECRET;

    public JWTFilter(JWTService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String cronJobHeader = request.getHeader("Cron-Job-Secret");
        if(cronJobHeader!=null && !cronJobHeader.isEmpty()){
            if(cronJobHeader.equals(JWT_SECRET)){
                UsernamePasswordAuthenticationToken cronAuth = new UsernamePasswordAuthenticationToken(
                        "CRON_SYSTEM", // Principal (can be any string or dummy user)
                        null,          // Credentials
                        Collections.singleton(new SimpleGrantedAuthority("CRON")) // Role/Authority
                );
                SecurityContextHolder.getContext().setAuthentication(cronAuth);
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("""
                {
                    "success": false,
                    "message": "Unauthorized",
                    "data": "Invalid Cron Job Secret"
                }
            """);
                response.getWriter().flush();
            }
            return;
        }
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            UserDto userDto = jwt.extractUser(token);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwt.validateAccessToken(token)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDto, token,Collections.singleton(new SimpleGrantedAuthority("USER")));
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            String safeMessage = "Authentication failed. Please log in again.";
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"" + safeMessage + "\"}");
            response.getWriter().flush();
        }
    }

}

