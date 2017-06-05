package ar.com.utn.proyecto.qremergencias.core.service;

import ar.com.utn.proyecto.qremergencias.core.domain.Role;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.repository.RoleRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    public Role save(final Role role) {
        Assert.notNull(role, "role can't be  null");
        return roleRepository.save(role);
    }

    public Page<Role> listRoles(final Pageable page) {
        return roleRepository.findAll(page);
    }

    @Transactional
    public void delete(final String roleId) {
        Assert.notNull(roleId, "roleId can't be  null");
        final Role roleDb = findOne(roleId);
        Assert.notNull(roleDb, "role not found");

        final List<User> usersWithRole = userRepository.findByRolesContaining(roleDb);

        for (final User user : usersWithRole) {
            user.getRoles().remove(roleDb);
        }

        // desasocio los usuarios-roles
        userRepository.save(usersWithRole);
        roleRepository.delete(roleDb);
    }

    public Role findOne(final String roleId) {
        Assert.notNull(roleId, "roleId can't be  null");
        return roleRepository.findOne(roleId);
    }

    @Transactional
    public Role update(final Role role) {
        Assert.notNull(role, "role can't be  null");
        Role roleDb = findOne(role.getId());
        Assert.notNull(roleDb, "role not found");


        roleDb.setAuthority(role.getAuthority());
        roleDb.setAssignable(role.isAssignable());

        roleDb = roleRepository.save(roleDb);

        return roleDb;
    }

    public Role getRoleAdmin() {
        return roleRepository.findByAuthority("ROLE_ADMIN");
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role getRoleOperator() {
        return roleRepository.findByAuthority("ROLE_OPERATOR");
    }
}
