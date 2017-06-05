package ar.com.utn.proyecto.qremergencias.core.service;

import ar.com.utn.proyecto.qremergencias.core.domain.Role;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.repository.RoleRepository;
import ar.com.utn.proyecto.qremergencias.core.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RoleServiceTest {

    private static final String ROLE = "role";

    @InjectMocks
    private RoleService service;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSave() {

        when(roleRepository.save(any(Role.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );
        final Role newRole = new Role();

        final Role role = service.save(newRole);

        assertNotNull(role);
        assertEquals(newRole, role);
        verify(roleRepository).save(eq(newRole));
    }

    @Test
    public void testListRoles() {

        when(roleRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(new Role())));
        final Pageable page = new PageRequest(0, 10);

        Page<Role> roles = service.listRoles(page);

        assertNotNull(roles);
        assertEquals(1, roles.getTotalPages());
        assertEquals(1, roles.getTotalElements());

        verify(roleRepository).findAll(eq(page));
    }

    @Test
    public void testDelete() {

        final Role role = new Role();
        role.setAuthority(ROLE);
        when(roleRepository.findOne(anyString())).thenReturn(role);

        final User user = new User();
        user.setUsername("TEST");
        user.getRoles().add(role);
        when(userRepository.findByRolesContaining(eq(role))).thenReturn(Collections.singletonList(user));

        service.delete("125");

        assertTrue(user.getRoles().isEmpty());

        verify(roleRepository).delete(eq(role));
        verify(userRepository).save(eq(Collections.singletonList(user)));
    }

    @Test
    public void testFindOne() {

        final Role role = new Role();
        role.setAuthority(ROLE);
        when(roleRepository.findOne(anyString())).thenReturn(role);

        final Role found = service.findOne("1");

        assertNotNull(found);
        assertEquals(found, role);
    }

    @Test
    public void testUpdate() {

        Role role = new Role();
        when(roleRepository.findOne(anyString())).thenReturn(role);
        when(roleRepository.save(any(Role.class))).thenAnswer(
                invocationOnMock -> invocationOnMock.getArguments()[0]
        );

        final Role updatedRole = new Role();
        String authority = "OTRA";
        updatedRole.setAuthority(authority);
        updatedRole.setId("3");
        Role updated = service.update(updatedRole);

        assertNotNull(updated);
        assertNotNull(updated.getAuthority());
        assertEquals(authority, updated.getAuthority());

        verify(roleRepository).save(eq(role));
    }

    @Test
    public void testGetRoleAdmin() {

        Role role = new Role();
        String roleAdmin = "ADMIN";
        role.setAuthority(roleAdmin);
        when(roleRepository.findByAuthority(anyString())).thenReturn(role);

        Role admin = service.getRoleAdmin();

        assertNotNull(admin);
        assertNotNull(admin.getAuthority());
        assertEquals(roleAdmin, role.getAuthority());
    }

    @Test
    public void testGetRoleOperator() {

        Role role = new Role();
        String roleOperator = "OPERATOR";
        role.setAuthority(roleOperator);
        when(roleRepository.findByAuthority(anyString())).thenReturn(role);

        Role operator = service.getRoleOperator();

        assertNotNull(operator);
        assertNotNull(operator.getAuthority());
        assertEquals(roleOperator, role.getAuthority());
    }

    @Test
    public void testFindAll() {

        when(roleRepository.findAll()).thenReturn(Collections.singletonList(new Role()));

        List<Role> roles = service.findAll();

        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(1, roles.size());
    }

}
