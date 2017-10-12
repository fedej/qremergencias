package ar.com.utn.proyecto.qremergencias.ws.service;

import ar.com.utn.proyecto.qremergencias.core.domain.DoctorFront;
import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.dto.CreateUserDTO;
import ar.com.utn.proyecto.qremergencias.core.dto.emergency.CreateDoctorDTO;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import ar.com.utn.proyecto.qremergencias.core.service.UserService;
import com.mongodb.gridfs.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class UserFrontService extends UserService {

    @Autowired
    private UserFrontRepository userFrontRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;


    public UserFront create(final CreateUserDTO createUserDTO) {
        if (createUserDTO == null) {
            return null;
        }
        final UserFront user = createUserFront(createUserDTO);
        final UserFront userFront = save(user);
        final String token = UUID.randomUUID().toString();
        createVerificationToken(userFront,token);
        return userFront;
    }

    private UserFront createUserFront(final CreateUserDTO createUserDTO) {
        final UserFront user = new UserFront();
        user.setUsername(createUserDTO.getEmail());
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(createUserDTO.getPassword());
        user.getRoles().add(createUserDTO.getRole());
        user.getRoles().add("ROLE_USER");
        return user;
    }

    private DoctorFront createDoctorFront(final CreateDoctorDTO model, final MultipartFile evidence) {
        final DoctorFront doctor = new DoctorFront();
        doctor.setUsername(model.getEmail());
        doctor.setEmail(model.getEmail());
        doctor.setPassword(model.getPassword());
        doctor.getRoles().add(model.getRole());
        doctor.getRoles().add("ROLE_USER");
        doctor.setRegistrationNumber(model.getRegistrationNumber());
        if (evidence != null) {
            try {
                final GridFSFile stored = gridFsTemplate.store(evidence.getInputStream(),
                        evidence.getOriginalFilename(),
                        evidence.getContentType());
                doctor.setEvidenceFile(stored.getId());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        return doctor;
    }

    public DoctorFront createDoctor(final CreateDoctorDTO createDoctorDTO, final MultipartFile file) {
        if (createDoctorDTO == null) {
            return null;
        }
        final DoctorFront doctorFront = createDoctorFront(createDoctorDTO,file);
        return save(doctorFront);
    }

    public UserFront findByUsername(final String username) {
        return userFrontRepository.findByUsername(username);
    }

    public UserFront update(final UserFront toUpdate) {
        return userFrontRepository.save(toUpdate);
    }

}
