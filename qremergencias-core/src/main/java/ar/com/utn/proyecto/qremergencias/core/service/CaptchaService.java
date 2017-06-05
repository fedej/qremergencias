package ar.com.utn.proyecto.qremergencias.core.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

@Service
@SuppressWarnings({"PMD.AccessorMethodGeneration", "PMD.AccessorClassGeneration"})
public class CaptchaService {

    @Value("${recaptcha.url}")
    private String recaptchaUrl;

    @Value("${recaptcha.secret-key}")
    private String secretKey;

    @SuppressWarnings("PMD.ImmutableField")
    private RestTemplate template = new RestTemplate();

    @Setter
    static class RecaptchaResponse {
        @JsonProperty("success")
        private boolean success;
        @JsonProperty("error-codes")
        private Collection<String> errorCodes;
    }

    public boolean validate(final String remoteIp, final String response) {

        if (remoteIp == null || response == null) {
            return false;
        }

        final RecaptchaResponse recaptchaResponse = template.postForEntity(
                recaptchaUrl, createBody(remoteIp, response), RecaptchaResponse.class)
                .getBody();

        return recaptchaResponse.success && recaptchaResponse.errorCodes.isEmpty();

    }

    private MultiValueMap<String, String> createBody(final String remoteIp, final String response) {
        final MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", secretKey);
        form.add("remoteip", remoteIp);
        form.add("response", response);
        return form;
    }

}
