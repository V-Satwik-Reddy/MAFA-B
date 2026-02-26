package majorproject.maf.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.service.JWTService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTService jwt;
    ApplicationContext context;
    @Value("${jwt.secret:}")
    private String JWT_SECRET;

    public JWTFilter(JWTService jwt, ApplicationContext context) {
        this.jwt = jwt;
        this.context = context;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String cronJobHeader = request.getHeader("Cron-Job-Secret");
        if(cronJobHeader!=null && !cronJobHeader.isEmpty()){
            if(cronJobHeader.equals(JWT_SECRET)){
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
            if(token.equals("NOT_FOUND")){
                throw new JwtException("Invalid token");
            }
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
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
            {
                "success": false,
                "message": "Unauthorized",
                "data": "%s"
            }
        """.formatted(ex.getMessage()));
            response.getWriter().flush();
        }
    }

}

