package majorproject.maf.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import majorproject.maf.exception.auth.JwtValidationException;
import majorproject.maf.service.JWTService;
import majorproject.maf.service.MyUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwt;

    @Autowired
    ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header= request.getHeader("Authorization");

        String token="";
        String username="";
        try{
            if(header!=null && header.startsWith("Bearer ")) {
                token = header.substring(7);
                username=jwt.extractUserName(token);
            }
            if(username!=""&& SecurityContextHolder.getContext().getAuthentication()==null ) {
                UserDetails userDetails=context.getBean(MyUserDetailService.class).loadUserByUsername(username);
                if(jwt.validateToken(token,userDetails)){
                    UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
            }
            filterChain.doFilter(request,response);
        } catch (JwtValidationException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
        {
            "success": false,
            "message": "Invalid jwt token sent",
            "data": "%s"
        }
    """.formatted(ex.getMessage()));
            response.getWriter().flush();
            return; // ✅ VERY IMPORTANT - stops further filters
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("""
        {
            "success": false,
            "message": "Unexpected authentication error",
            "data": "%s"
        }
    """.formatted(ex.getMessage()));
            response.getWriter().flush();
            return; // ✅ also stop here
        }

    }
}

