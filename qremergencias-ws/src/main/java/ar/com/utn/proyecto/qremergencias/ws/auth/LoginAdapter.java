package ar.com.utn.proyecto.qremergencias.ws.auth;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.LoginUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;

@Component
public class LoginAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    public LoginUserDTO login(final UserFront user, final String password,
                              final NativeWebRequest request) {
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                user.getUsername(), password, user.getAuthorities());

        doAuthenticate(request, token);

        return getLoginUserDto(user, null);
    }

    public LoginUserDTO getLoginUserDto(final UserFront user, final String picture) {
        return new LoginUserDTO(user.getName(), user.getLastname(), picture);
    }

    private void doAuthenticate(final NativeWebRequest request,
            final AbstractAuthenticationToken token) {
        token.setDetails(
                new WebAuthenticationDetails(request.getNativeRequest(HttpServletRequest.class)));

        final Authentication authenticatedUser = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
    }

}
