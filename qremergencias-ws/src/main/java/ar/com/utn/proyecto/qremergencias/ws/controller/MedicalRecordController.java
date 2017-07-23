package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.dto.MedicalRecordDTO;
import ar.com.utn.proyecto.qremergencias.ws.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/medicalRecord")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    @GetMapping
    @PreAuthorize("isFullyAuthenticated()")
    public Page<MedicalRecordDTO> list(@PageableDefault final Pageable page,
                                       @AuthenticationPrincipal final User user) {

        final Page<MedicalRecord> domainPage = medicalRecordService.findByUser(user, page);
        return domainPage.map(MedicalRecordDTO::new);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isFullyAuthenticated()")
    public MedicalRecordDTO findById(@PathVariable final String id) {
        return new MedicalRecordDTO(medicalRecordService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MEDICO')")
    public Map<String, String> create(@Valid @RequestBody final MedicalRecordDTO medicalRecord,
                                      @AuthenticationPrincipal final User user) {
        final MedicalRecord saved = medicalRecordService.save(user, medicalRecord);
        return Collections.singletonMap("id", saved.getId());
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('MEDICO')")
    public void update(@PathVariable final String id,
                       @Valid @RequestBody final MedicalRecordDTO medicalRecord,
                       @AuthenticationPrincipal final User user) {
        medicalRecordService.update(id, user, medicalRecord);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MEDICO')")
    public void delete(@PathVariable final String id,
                       @AuthenticationPrincipal final User user) {
        medicalRecordService.delete(id, user);
    }


}
