package ar.com.utn.proyecto.qremergencias.ws.controller;

import ar.com.utn.proyecto.qremergencias.core.domain.MedicalRecord;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.MedicalRecordDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.FilterDTO;
import ar.com.utn.proyecto.qremergencias.ws.service.GridFsService;
import ar.com.utn.proyecto.qremergencias.ws.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriTemplate;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicalRecord")
public class MedicalRecordController {

    public static final String HAS_ROLE_MEDICO = "hasRole('MEDICO')";

    @Value("${qremergencias.baseUrl.file}")
    private UriTemplate fileUriTemplate;

    private final MedicalRecordService medicalRecordService;
    private final GridFsService gridFsService;

    @Autowired
    public MedicalRecordController(final MedicalRecordService medicalRecordService,
                                   final GridFsService gridFsService) {
        this.medicalRecordService = medicalRecordService;
        this.gridFsService = gridFsService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PACIENTE')")
    public Page<MedicalRecordDTO> listMyRecords(@PageableDefault final Pageable page,
                                       @AuthenticationPrincipal final UserFront user,
                                                @RequestBody(required = false) final FilterDTO filter) {

        final Page<MedicalRecord> domainPage = medicalRecordService.findByUser(user, page, filter);
        return domainPage.map(m -> new MedicalRecordDTO(m, fileUriTemplate,
                gridFsService.findGridFSFile()));
    }

    @GetMapping("/user")
    @PreAuthorize(HAS_ROLE_MEDICO)
    public Page<MedicalRecordDTO> listPatientRecords(@PageableDefault final Pageable page,
                                       @RequestParam final String username,
                                                     @RequestBody(required = false) final FilterDTO filter) {

        final Page<MedicalRecord> domainPage = medicalRecordService.findByUsername(username, page, filter);
        return domainPage.map(m -> new MedicalRecordDTO(m, fileUriTemplate,
                gridFsService.findGridFSFile()));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isFullyAuthenticated()")
    public MedicalRecordDTO findById(@PathVariable final String id) {
        return new MedicalRecordDTO(medicalRecordService.findById(id), fileUriTemplate,
                gridFsService.findGridFSFile());
    }

    @GetMapping("/file/{fileId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isFullyAuthenticated()")
    @ApiIgnore("URL is handled by the backend")
    public Resource findFileById(@PathVariable final String fileId, final HttpServletResponse response) {
        final GridFsResource fileById = gridFsService.findFileById(fileId);

        if (!fileById.getContentType().matches(".*pdf.*|.*image.*")) {
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileById.getFilename());
            response.addHeader(HttpHeaders.CONTENT_TYPE, fileById.getContentType());
        }

        return fileById;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(HAS_ROLE_MEDICO)
    public Map<String, String> create(@Valid final MedicalRecordDTO medicalRecord,
                             @RequestPart(required = false, name = "file") final MultipartFile file,
                             @AuthenticationPrincipal final UserFront user) {
        final List<MultipartFile> files = new ArrayList<>(1);
        if (file != null) {
            files.add(file);
        }

        final MedicalRecord saved = medicalRecordService.save(user, medicalRecord, files);
        return Collections.singletonMap("id", saved.getId());
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize(HAS_ROLE_MEDICO)
    public void update(@PathVariable final String id,
                       @Valid @RequestBody final MedicalRecordDTO medicalRecord,
                       @AuthenticationPrincipal final UserFront user) {
        medicalRecordService.update(id, user, medicalRecord);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(HAS_ROLE_MEDICO)
    public void delete(@PathVariable final String id,
                       @AuthenticationPrincipal final UserFront user) {
        medicalRecordService.delete(id, user);
    }


}
