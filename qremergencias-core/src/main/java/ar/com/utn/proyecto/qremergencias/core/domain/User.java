package ar.com.utn.proyecto.qremergencias.core.domain;

import ar.com.utn.proyecto.qremergencias.core.validation.Password;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(of = "username")
@javax.persistence.Entity
@SuppressWarnings("PMD.ImmutableField")
public class User implements Serializable, UserDetails {

    @Id
    @javax.persistence.Id
    private String id;

    @Version
    private Long version;

    @NotEmpty
    @Indexed(unique = true)
    private String username;

    @Email
    @NotEmpty
    private String email;

    @Password
    private String password;

    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled;

    private String repassword;

    @javax.persistence.ElementCollection
    private List<String> roles;

    @DBRef
    @javax.persistence.ElementCollection
    @OneToMany
    private List<PasswordChange> passwordChanges;

    @DBRef
    @javax.persistence.ElementCollection
    @OneToMany
    private List<ForgotPassword> forgotPasswords;

    public User() {
        this.roles = new ArrayList<>();
        this.passwordChanges = new ArrayList<>();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }


}
