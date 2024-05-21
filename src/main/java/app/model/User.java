package app.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "id", nullable = false, unique = true)
    private int id;
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "phonenumber", nullable = false)
    private int phoneNumber;

    @ManyToMany
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_name", referencedColumnName = "username"),
            inverseJoinColumns = @JoinColumn(name = "role_name", referencedColumnName = "rolename"))
    private Set<Role> roles = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "user_events",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "event_id", referencedColumnName = "event_id"))
    private Set<Event> events = new HashSet<>();


    public User(String username, String password) {
        this.username = username;
        this.password = password;
        String salt = BCrypt.gensalt();
        this.password = BCrypt.hashpw(password, salt);
    }


    public User(String username, String password, String email, int phoneNumber) {
        this.username = username;
        this.password = password;
        String salt = BCrypt.gensalt();
        this.password = BCrypt.hashpw(password, salt);
        this.email= email;
        this.phoneNumber= phoneNumber;
    }

    public User(String password) {
        this.password = password;
        String salt = BCrypt.gensalt();
        this.password = BCrypt.hashpw(password, salt);
    }

    public User(String username, Set<Event> events) {
        this.username = username;
        this.events = events;
    }

    public boolean verifyUser(String password) {
        return BCrypt.checkpw(password, this.password);
    }


    public User(String username, String password, String email, int phoneNumber, Set<Role> roles, Set<Event> events) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.roles = roles;
        this.events = events;
    }

    public void addRole(Role role) {
        roles.add(role);
        role.getUsers().add(this);
    }
    public void removeRole(Role role) {
        roles.remove(role);
        role.getUsers().remove(this);
    }
    public void addEvent(Event event) {
        events.add(event);
        event.getUsers().add(this);
    }
    public void removeEvent(Event event) {
        events.remove(event);
        event.getUsers().remove(this);
    }


    public Set<String> getRolesAsStrings() {
        if (roles.isEmpty()) {
            return null;
        }
        Set<String> rolesAsStrings = new HashSet<>();
        roles.forEach((role) -> {
            rolesAsStrings.add(role.getRolename());
        });
        return rolesAsStrings;
    }

    public Set<String> getEventsAsStrings() {
        if (events.isEmpty()) {
            return null;
        }
        Set<String> eventsAsStrings = new HashSet<>();
        events.forEach((event) -> {
            eventsAsStrings.add(event.getTitle());
        });
        return eventsAsStrings;
    }

}