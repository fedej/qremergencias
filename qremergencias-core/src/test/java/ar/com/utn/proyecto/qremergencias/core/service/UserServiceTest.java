package ar.com.utn.proyecto.qremergencias.core.service;

import ar.com.utn.proyecto.qremergencias.core.domain.User;
import ar.com.utn.proyecto.qremergencias.core.repository.UserRepository;
import org.junit.Assert;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.JUnitTestContainsTooManyAsserts"})
public class UserServiceTest {

    private static final String ADMIN_ROLE_AUTHORITY = "ADMIN_ROLE";
    private static final String OPERATOR_ROLE_AUTHORITY = "OPERATOR_ROLE";

    private static final long TOTAL_USERS = 100L;

    private static final String PASSWORD_ENCODED = "Password!Encoded";
    private static final String OTHER = "other";
    private static final String EMAIL = "nose@mailar-gl.com";

    @InjectMocks
    private final UserService userService = new UserService();

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    private User userAdmin;
    private User userOperator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        userAdmin = createUser("admin");
        userAdmin.setId("1");
        userAdmin.getRoles().add(ADMIN_ROLE_AUTHORITY);

        userOperator = createUser("Operator");
        userOperator.setId("1");
        userOperator.getRoles().add(OPERATOR_ROLE_AUTHORITY);
    }

    @Test
    public void testSave() {
        when(passwordEncoder.encode(anyString())).thenReturn(PASSWORD_ENCODED);

        final User userToreturn = createUser();
        userToreturn.setId("55");

        when(userRepository.save(any(User.class))).thenReturn(userToreturn);

        final User user = createUser();
        final User result = userService.save(user);

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
        final User result = userService.save(null);
        assertNull(result);
    }

    @Test
    public void testUpdate() {
        final User userChanged = createUser();
        userChanged.setUsername(OTHER);
        userChanged.setId(userAdmin.getId());
        userChanged.setEmail(EMAIL);

        when(userRepository.save(any(User.class))).thenReturn(userChanged);
        when(userRepository.findOne(anyString())).thenReturn(userAdmin);

        final User user = createUser();
        user.setUsername(OTHER);
        user.setEmail(EMAIL);

        final User result = userService.update(user);
        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    public void testUpdateAdmin() {

        final User userChanged = createUser();
        userChanged.setUsername(OTHER);
        userChanged.setId(userAdmin.getId());
        userChanged.setEmail(EMAIL);

        // when(userRepository.save(any(User.class))).thenReturn(userChanged);
        when(userRepository.findOne(anyString())).thenReturn(userAdmin);

        final User user = createUser();
        user.setUsername(OTHER);
        user.setEmail(EMAIL);
        final User result = userService.update(user);
        assertNull(result);
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateFail() {
        final User userChanged = createUser();

        when(userRepository.save(any(User.class))).thenReturn(userChanged);
        when(userRepository.findOne(anyString())).thenThrow(new RuntimeException());

        final User user = createUser();
        user.setUsername(OTHER);
        user.setEmail(EMAIL);
        userService.update(user);
        Assert.fail();
    }

    @Test
    public void testFindAll() {
        final Pageable page = new PageRequest(0, 10);
        @SuppressWarnings("unchecked")
        final Page<User> pageData = mock(Page.class);
        final List<User> users = new ArrayList<>(10);
        users.add(userAdmin);

        when(pageData.getContent()).thenReturn(users);
        when(pageData.getTotalElements()).thenReturn(TOTAL_USERS);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(pageData);

        final Page<User> usersPage = userService.findAll(page);

        assertNotNull(usersPage);
        assertEquals(TOTAL_USERS, usersPage.getTotalElements());
        assertNotNull(usersPage.getContent());
        assertTrue(usersPage.getContent().contains(userAdmin));
    }

    @Test
    public void testFindAllWithOutPage() {
        @SuppressWarnings("unchecked")
        final Page<User> pageData = mock(Page.class);
        final List<User> users = new ArrayList<>(10);
        users.add(userAdmin);

        when(pageData.getContent()).thenReturn(users);
        when(pageData.getTotalElements()).thenReturn(TOTAL_USERS);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(pageData);

        final Page<User> usersPage = userService.findAll(null);

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
        final List<User> userAdminList = new ArrayList<>();
        userAdminList.add(userOperator);

        @SuppressWarnings("unchecked")
        final Page<User> pageData = mock(Page.class);

        when(pageData.getContent()).thenReturn(userAdminList);
        when(pageData.getTotalElements()).thenReturn(TOTAL_USERS);

        when(userRepository.findByRolesContaining(any(String.class), any(Pageable.class)))
                .thenReturn(pageData);
        final Page<User> usersAdmin = userService.findByRole(OPERATOR_ROLE_AUTHORITY, page);
        assertNotNull(usersAdmin);

        for (final User user : usersAdmin.getContent()) {
            assertFalse(user.getRoles().contains(ADMIN_ROLE_AUTHORITY));
            assertTrue(user.getRoles().contains(OPERATOR_ROLE_AUTHORITY));
        }
    }

    @Test
    public void testFindByUsername() {
        when(userRepository.findByUsername(anyString())).thenReturn(userAdmin);
        final User result = userService.findByUsername("admin");
        assertNotNull(result);
        assertEquals(userAdmin.getUsername(), result.getUsername());
    }

    @Test
    public void testFindById() {
        when(userRepository.findOne(anyString())).thenReturn(userOperator);
        final User user = userService.findById(userOperator.getId());
        assertNotNull(user);
        assertEquals(userOperator.getId(), user.getId());
    }

    @Test
    public void testDelete() {
        when(userRepository.findOne(anyString())).thenReturn(userOperator);

        doNothing().when(userRepository).delete(any(User.class));
        final boolean deleted = userService.delete(userOperator.getId(), userAdmin);
        assertTrue(deleted);
    }

    @Test
    public void testDeleteAdmin() {
        when(userRepository.findOne(anyString())).thenReturn(userAdmin);

        doNothing().when(userRepository).delete(any(User.class));
        final boolean deleted = userService.delete(userAdmin.getId(), userAdmin);
        assertFalse(deleted);
    }

    @Test
    public void testBlockUser() {
        when(userRepository.findByUsername(anyString())).thenReturn(userOperator);
        final boolean userBlocked = userService.blockUser(userOperator.getUsername());
        assertTrue(userBlocked);
    }

    @Test
    public void testIsNotAdmin() {
        when(userRepository.findByUsername(anyString())).thenReturn(userAdmin);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        final boolean notAdmin = userService.isNotAdmin(userAdmin.getPassword());
        assertFalse(notAdmin);
    }

    private User createUser() {
        return createUser("testUser");
    }

    private User createUser(final String username) {
        final User user = new User();
        user.setUsername(username);
        final String mail = username == null ? "user@mailDeprueba.com" : username
                + "@mailDeprueba.com";
        user.setEmail(mail);
        user.setPassword("Password!");
        return user;
    }
}
