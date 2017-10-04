package ar.com.utn.proyecto.qremergencias.bo.service;

import ar.com.utn.proyecto.qremergencias.core.domain.UserFront;
import ar.com.utn.proyecto.qremergencias.core.repository.UserFrontRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class VerificationService {

    private static final String MEDICO = "ROLE_MEDICO";

    private final UserFrontRepository userFrontRepository;

    @Autowired
    public VerificationService(final UserFrontRepository userFrontRepository) {
        this.userFrontRepository = userFrontRepository;
    }

    public Page<UserFront> findMedicos(final Pageable page) {
        return userFrontRepository.findByRolesContaining(MEDICO, page);
    }

    public void verify(final String id) {
        modifyMedico(id, true);
    }

    public void unverify(final String id) {
        modifyMedico(id, false);
    }

    private void modifyMedico(final String id, final boolean verified) {
        final UserFront medico = userFrontRepository.findOne(id);
        medico.setEnabled(verified);
        userFrontRepository.save(medico);
    }

}
