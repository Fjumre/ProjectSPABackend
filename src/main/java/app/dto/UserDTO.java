package app.dto;

import app.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor
@Setter
@ToString
public class UserDTO {

    private String username;
    private String password;
    private String email;
    private Integer phoneNumber;

    private Set<String> roles = new HashSet<>();
    private Set<String> toDos = new HashSet<>();

    private String currentPassword;
    private String newPassword;

    public UserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UserDTO(String username, String password, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public UserDTO(String username, String password, String email, Integer phoneNumber, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.roles = roles;
    }

    public UserDTO(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.roles = user.getRolesAsStrings();
        this.toDos = user.getToDosAsStrings();
    }

    public UserDTO(String username, Set<String> roleSet){
        this.username = username;
        this.roles = roleSet;
    }

    public static List<UserDTO> toUserDTOList(List<User> users) {
        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user : users) {
            userDTOList.add(new UserDTO(user));
        }
        return userDTOList;
    }

    public Set<String> getToDos() {
        return toDos;
    }
}
