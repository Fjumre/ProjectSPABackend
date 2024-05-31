package app.controllers;

import app.config.HibernateConfig;
import app.dao.ToDoDAO;
import app.dao.UserDAO;
import app.dto.UserDTO;
import app.model.ToDo;
import app.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserController implements IUserController{

    private UserDAO userDAO = new UserDAO(HibernateConfig.getEntityManagerFactory());
    ObjectMapper objectMapper = new ObjectMapper();
    //UserDTO userDTO= new UserDTO();
   // User user= new User();
    EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private ToDoDAO toDoDAO;


    public UserController(UserDAO userDAO) {

        this.userDAO = userDAO;

    }
    private UserDTO convertToUserDTO(User user) {
        return new UserDTO(user);
    }
    @Override
    public Handler getAllUsers() {
        return (ctx) -> {
            try {
                // Fetch all users from the database
                List<User> users = userDAO.getAlleUser();

                // Convert each User to UserDTO
                List<UserDTO> userDTOs = users.stream()
                        .map(this::convertToUserDTO)
                        .collect(Collectors.toList());


                ctx.json(userDTOs);
            } catch (Exception e) {
                ctx.status(500);
                ctx.json(Map.of("msg", "Internal server error"));
                System.out.println(e);
            }
        };
    }


    @Override
    public Handler createUser() {
        return (ctx) -> {
            UserDTO userDTO = ctx.bodyAsClass(UserDTO.class);

            try {
                User newUser = userDAO.createUser(
                        userDTO.getUsername(),
                        BCrypt.hashpw(userDTO.getNewPassword(), BCrypt.gensalt()),
                        userDTO.getEmail(),
                        userDTO.getPhoneNumber()
                );

                ctx.status(201);
                ctx.json(new UserDTO(newUser));
            } catch (Exception e) {
                ctx.status(500);
                ctx.json("Internal server error " + e);
            }
        };
    }

    @Override
    public Handler getUserById() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int userId = Integer.parseInt(ctx.pathParam("id"));
                User userById = userDAO.getUserById(userId);

                if (userById == null) {
                    ctx.status(404);
                    returnObject.put("msg", "User not found");
                    ctx.json(returnObject);
                } else {
                    UserDTO userDTO = new UserDTO(userById);
                    ctx.json(userDTO);
                }
            } catch (Exception e) {
                ctx.status(500);
                System.out.println(e);
                returnObject.put("msg", "Internal server error");
                ctx.json(returnObject);
            }
        };
    }
    @Override
    public Handler getUserByUsername() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                String username = ctx.pathParam("username");
                User userByUsername = userDAO.findByUsername(username);

                if (userByUsername == null) {
                    ctx.status(404);
                    returnObject.put("msg", "User not found");
                    ctx.json(returnObject);
                } else {
                    UserDTO userDTO = convertToUserDTO(userByUsername);
                    ctx.json(userDTO);
                }
            } catch (Exception e) {
                ctx.status(500);
                System.out.println(e);
                returnObject.put("msg", "Internal server error");
                ctx.json(returnObject);
            }
        };
    }


    @Override
    public Handler updateUser() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int userId = Integer.parseInt(ctx.pathParam("id"));
                UserDTO userDTO = ctx.bodyAsClass(UserDTO.class);

                User user = userDAO.getUserById(userId);
                if (user == null) {
                    ctx.status(404).json(returnObject.put("msg", "User not found"));
                    return;
                }

                // Update user fields
                user.setUsername(userDTO.getUsername());
                user.setEmail(userDTO.getEmail());
                user.setPhoneNumber(userDTO.getPhoneNumber());
                if (userDTO.getPassword() != null) {
                    user.setPassword(userDTO.getPassword());
                }

                userDAO.updateUser(user);

                ctx.status(HttpStatus.OK).json(new UserDTO(user));
            } catch (Exception e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(returnObject.put("msg", "Internal server error: " + e.getMessage()));
                e.printStackTrace();
            }
        };
    }

    @Override
    public Handler updateUserByUsername() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                String username = ctx.pathParam("username");
                User userByUsername = userDAO.findByUsername(username);

                if (userByUsername == null) {
                    ctx.status(404);
                    returnObject.put("msg", "User not found");
                    ctx.json(returnObject);
                } else {
                    UserDTO userDTO = ctx.bodyAsClass(UserDTO.class);
                    userByUsername.setEmail(userDTO.getEmail());
                    userByUsername.setPhoneNumber(userDTO.getPhoneNumber());
                    if (userDTO.getNewPassword() != null) {
                        userByUsername.setPassword(userDTO.getNewPassword());
                    }
                    userDAO.updateUser(userByUsername);
                    ctx.json(convertToUserDTO(userByUsername));
                }
            } catch (Exception e) {
                ctx.status(500);
                System.out.println(e);
                returnObject.put("msg", "Internal server error");
                ctx.json(returnObject);
            }
        };
    }

    @Override
    public Handler deleteUser() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int userId = Integer.parseInt(ctx.pathParam("id"));
                JsonNode body = ctx.bodyAsClass(JsonNode.class);
                String password = body.get("password").asText();

                User user = userDAO.getUserById(userId);
                if (user == null) {
                    ctx.status(404).json(returnObject.put("msg", "User not found"));
                    return;
                }

                // Validate password
                if (!userDAO.checkPassword(user, password)) {
                    ctx.status(401).json(returnObject.put("msg", "Incorrect password"));
                    return;
                }

                // Delete associated todo records
                deleteTodosByUserId(userId);

                // Delete user
                userDAO.deleteUser(userId);
                ctx.status(204).json(returnObject.put("msg", "User deleted successfully"));
            } catch (Exception e) {
                ctx.status(500).json(returnObject.put("msg", "Internal server error: " + e.getMessage()));
                e.printStackTrace();
            }
        };
    }

    public void deleteTodosByUserId(int userId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Find the user by userId
            User user = em.find(User.class, userId);
            if (user != null) {
                // Iterate over the ToDos associated with the user and remove each
                List<ToDo> toDos = new ArrayList<>(user.getToDos());
                for (ToDo toDo : toDos) {
                    // Remove the ToDo from associated Users
                    for (User associatedUser : toDo.getUsers()) {
                        associatedUser.getToDos().remove(toDo);
                        em.merge(associatedUser);
                    }
                    em.remove(toDo);
                }
                em.getTransaction().commit();
            } else {
                em.getTransaction().rollback();
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }



    @Override
    public Handler logout() {
        return (ctx) -> {
            ctx.req().getSession().invalidate();
            ctx.redirect("/");
        };
    }



}
