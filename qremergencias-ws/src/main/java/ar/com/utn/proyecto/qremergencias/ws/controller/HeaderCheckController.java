package ar.com.utn.proyecto.qremergencias.ws.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/check")
public class HeaderCheckController {

    @GetMapping("/ping")
    @ResponseStatus(HttpStatus.OK)
    public String ping(@RequestParam final String userId) {
        return "pong";
    }

}
