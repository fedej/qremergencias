package ar.com.utn.proyecto.qremergencias.core.service;

import ar.com.utn.proyecto.qremergencias.core.domain.Role;
import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    private static final String ADMIN_ROLE_AUTHORITY = "ADMIN_ROLE";
    private static final String OPERATOR_ROLE_AUTHORITY = "OPERATOR_ROLE";

    private static final long TOTAL_USERS = 100L;

    private static final String PASSWORD_ENCODED = "Password!Encoded";

    @InjectMocks
    private UserService userService = new UserService();

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    private User userAdmin;
    private User userOperator;
    @Mock
    private Role adminRole;
    @Mock
    private Role operatorRole;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(adminRole.getAuthority()).thenReturn(ADMIN_ROLE_AUTHORITY);
        when(operatorRole.getAuthority()).thenReturn(OPERATOR_ROLE_AUTHORITY);

        userAdmin = createUser("admin");
        userAdmin.setId("1");
        userAdmin.getRoles().add(adminRole);

        userOperator = createUser("Operator");
        userOperator.setId("1");
        userOperator.getRoles().add(operatorRole);
    }

    @Test
    public void testSave() {
        when(passwordEncoder.encode(anyString())).thenReturn(PASSWORD_ENCODED);
        User userToreturn = createUser();
        userToreturn.setId("55");
        when(userRepository.save(any(User.class))).thenReturn(userToreturn);

        User user = createUser();
        User result = userService.save(user);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(result.getUsername(), user.getUsername());
        assertEquals(result.getEmail(), user.getEmail());
        // check password encoded is called
        assertEquals(PASSWORD_ENCODED, user.getPassword());
        // check defaults values for accounts
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        assertTrue(result.isEnabled());

    }

    @Test(expected = NullPointerException.class)
    public void testSaveFail() {
        User result = userService.save(null);
        assertNull(result);
    }

    @Test
    public void testUpdate() {
        User userChanged = createUser();
        userChanged.setUsername("other");
        userChanged.setId(userAdmin.getId());
        userChanged.setEmail("nose@mailar-gl.com");
        when(userRepository.save(any(User.class))).thenReturn(userChanged);
        when(userRepository.findOne(anyString())).thenReturn(userAdmin);
        User user = createUser();
        user.setUsername("other");
        user.setEmail("nose@mailar-gl.com");

        User result = userService.update(user);
        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    public void testUpdateAdmin() {

        User userChanged = createUser();
        userChanged.setUsername("other");
        userChanged.setId(userAdmin.getId());
        userChanged.setEmail("nose@mailar-gl.com");

        // when(userRepository.save(any(User.class))).thenReturn(userChanged);
        when(userRepository.findOne(anyString())).thenReturn(userAdmin);
        when(roleService.getRoleAdmin()).thenReturn(adminRole);

        User user = createUser();
        user.setUsername("other");
        user.setEmail("nose@mailar-gl.com");
        User result = userService.update(user);
        assertNull(result);
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateFail() {
        User userChanged = createUser();
        when(userRepository.save(any(User.class))).thenReturn(userChanged);
        when(userRepository.findOne(anyString())).thenThrow(new RuntimeException());
        User user = createUser();
        user.setUsername("other");
        user.setEmail("nose@mailar-gl.com");
        userService.update(user);
        fail();
    }

    @Test
    public void testFindAll() {
        final Pageable page = new PageRequest(0, 10);
        @SuppressWarnings("unchecked")
        Page<User> pageData = mock(Page.class);
        List<User> users = new ArrayList<>(10);
        users.add(userAdmin);
        when(pageData.getContent()).thenReturn(users);
        when(pageData.getTotalElements()).thenReturn(TOTAL_USERS);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(pageData);
        Page<User> usersPage = userService.findAll(page);
        assertNotNull(usersPage);
        assertEquals(TOTAL_USERS, usersPage.getTotalElements());
        assertNotNull(usersPage.getContent());
        assertTrue(usersPage.getContent().contains(userAdmin));
    }

    @Test
    public void testFindAllWithOutPage() {
        @SuppressWarnings("unchecked")
        Page<User> pageData = mock(Page.class);
        List<User> users = new ArrayList<>(10);
        users.add(userAdmin);
        when(pageData.getContent()).thenReturn(users);
        when(pageData.getTotalElements()).thenReturn(TOTAL_USERS);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(pageData);
        Page<User> usersPage = userService.findAll(null);
        assertNotNull(usersPage);
        assertEquals(TOTAL_USERS, usersPage.getTotalElements());
        assertNotNull(usersPage.getContent());
        assertTrue(usersPage.getContent().contains(userAdmin));
        assertNotNull(usersPage);
        assertNotNull(usersPage.getContent());
        assertTrue(usersPage.getContent().contains(userAdmin));
    }

    @Test
    public void testFindByRole() {
        final Pageable page = new PageRequest(0, 10);
        List<User> userAdminList = new ArrayList<>();
        userAdminList.add(userOperator);

        @SuppressWarnings("unchecked")
        Page<User> pageData = mock(Page.class);

        when(pageData.getContent()).thenReturn(userAdminList);
        when(pageData.getTotalElements()).thenReturn(TOTAL_USERS);

        when(userRepository.findByRolesContaining(any(Role.class), any(Pageable.class))).thenReturn(pageData);
        Page<User> usersAdmin = userService.findByRole(operatorRole, page);
        assertNotNull(usersAdmin);

        for (User user : usersAdmin.getContent()) {
            assertTrue(!user.getRoles().contains(adminRole));
            assertTrue(user.getRoles().contains(operatorRole));
        }
    }

    @Test
    public void testFindByUsername() {
        when(userRepository.findByUsername(anyString())).thenReturn(userAdmin);
        User result = userService.findByUsername("admin");
        assertNotNull(result);
        assertEquals(userAdmin.getUsername(), result.getUsername());
    }

    @Test
    public void testFindById() {
        when(userRepository.findOne(anyString())).thenReturn(userOperator);
        User user = userService.findById(userOperator.getId());
        assertNotNull(user);
        assertEquals(userOperator.getId(), user.getId());
    }

    @Test
    public void testDelete() {
        when(userRepository.findOne(anyString())).thenReturn(userOperator);
        when(roleService.getRoleAdmin()).thenReturn(adminRole);
        doNothing().when(userRepository).delete(any(User.class));
        boolean deleted = userService.delete(userOperator.getId(), userAdmin);
        assertTrue(deleted);
    }

    @Test
    public void testDeleteAdmin() {
        when(userRepository.findOne(anyString())).thenReturn(userAdmin);
        when(roleService.getRoleAdmin()).thenReturn(adminRole);
        doNothing().when(userRepository).delete(any(User.class));
        boolean deleted = userService.delete(userAdmin.getId(), userAdmin);
        assertFalse(deleted);
    }

    @Test
    public void testBlockUser() {
        when(userRepository.findByUsername(anyString())).thenReturn(userOperator);
        boolean userBlocked = userService.blockUser(userOperator.getUsername());
        assertTrue(userBlocked);
    }

    @Test
    public void testIsNotAdmin() {
        when(userRepository.findByUsername(anyString())).thenReturn(userAdmin);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        boolean notAdmin = userService.isNotAdmin(userAdmin.getPassword());
        assertFalse(notAdmin);
    }

    private User createUser() {
        return createUser("testUser");
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        String mail;
        if (username == null) {
            mail = "user@mailDeprueba.com";
        } else {
            mail = username + "@mailDeprueba.com";
        }
        user.setEmail(mail);
        user.setPassword("Password!");
        return user;
    }
}
