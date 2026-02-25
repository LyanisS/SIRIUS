package fr.episen.sirius.pcc.back.security;

import fr.episen.sirius.pcc.back.models.voyageur.Utilisateur;
import fr.episen.sirius.pcc.back.repositories.voyageur.UtilisateurRepository;
import fr.episen.sirius.pcc.back.services.voyageur.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!isAuthRequired(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token manquant.");
            return;
        }

        final String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token invalide ou expiré.");
            return;
        }

        Long uid = jwtService.extractUserId(token);

        Optional<Utilisateur> utilisateur = utilisateurRepository.findById(uid);
        if (utilisateur.isEmpty()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Utilisateur introuvable.");
            return;
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(utilisateur.get(), null, List.of());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    protected boolean isAuthRequired(HttpServletRequest request) throws ServletException {
        try {
            ServletRequestPathUtils.parseAndCache(request);

            HandlerExecutionChain handler = requestMappingHandlerMapping.getHandler(request);
            if (handler == null) return false;

            Object handlerObject = handler.getHandler();
            if (!(handlerObject instanceof HandlerMethod method)) return false;

            return method.hasMethodAnnotation(AuthRequired.class);
        } catch (Exception e) {
            return false;
        }
    }
}
