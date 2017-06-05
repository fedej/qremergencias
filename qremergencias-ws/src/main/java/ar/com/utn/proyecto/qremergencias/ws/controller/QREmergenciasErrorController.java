package ar.com.utn.proyecto.qremergencias.ws.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class QREmergenciasErrorController extends BasicErrorController {

    @Autowired
    public QREmergenciasErrorController(final ErrorAttributes errorAttributes,
                                        final ServerProperties serverProperties) {
        super(errorAttributes, serverProperties.getError());
    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    protected Map<String, Object> getErrorAttributes(final HttpServletRequest request,
                                                     final boolean includeStackTrace) {
        final Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("timestamp", new Date());
        errorAttributes.put("error", getStatus(request).getReasonPhrase());
        errorAttributes.put("status", getStatus(request).value());
        errorAttributes.put("message", "");
        errorAttributes.put("code", 1000);
        return errorAttributes;
    }

}
