package ar.com.utn.proyecto.qremergencias.ws.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QREmergenciasController {

    private static final String TOKEN = "token";

    @Autowired
    private CsrfTokenRepository tokenRepository;

    @RequestMapping(value = "/token", method = RequestMethod.GET)
    public Map<String, String> getToken(final HttpServletRequest request,
                                        final HttpServletResponse response) {
        final CsrfToken token = tokenRepository.generateToken(request);
        tokenRepository.saveToken(token, request, response);
        return Collections.singletonMap(TOKEN, token.getToken());
    }

}
