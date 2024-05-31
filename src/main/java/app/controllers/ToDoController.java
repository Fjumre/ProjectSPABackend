package app.controllers;

import app.config.HibernateConfig;
import app.dao.ToDoDAO;
import app.dao.UserDAO;
import app.dto.ToDoDTO;
import app.dto.UserDTO;
import app.model.ToDo;
import app.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ToDoController implements IToDoController {
    private final ToDoDAO toDoDAO;
    private UserDAO userDAO;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecurityController securityController = new SecurityController();
    private final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    public ToDoController(ToDoDAO toDoDAO) {
        this.toDoDAO = toDoDAO;
    }

    private List<UserDTO> convertToUserDTO(List<User> users) {
        return users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    private ToDo convertToEntity(ToDoDTO toDoDTO) {
        return new ToDo(toDoDTO);
    }

    public Handler getAllToDos() {

        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();

            // Check for authentication
            String authToken = ctx.header("Authorization");
            if (authToken == null || !authToken.startsWith("Bearer ")) {
                ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized"));
                return;
            }

            // Remove the "Bearer " prefix to get the actual token
            authToken = authToken.substring(7);

            UserDTO user;
            try {
                user = securityController.verifyToken(authToken);
                if (user == null) {
                    ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized"));
                    return;
                }
            } catch (Exception e) {
                ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized: " + e.getMessage()));
                return;
            }

            try {
                List<ToDo> toDos = toDoDAO.getAllToDos();
                List<ToDoDTO> toDoDTOS = toDos.stream().map(ToDoDTO::new).collect(Collectors.toList());
                ctx.json(toDoDTOS);
            } catch (Exception e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(returnObject.put("msg", "Internal server error: " + e.getMessage()));
            }
        };
    }

    public Handler getToDoByDate() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            String authToken = ctx.header("Authorization");
            if (authToken == null || !authToken.startsWith("Bearer ")) {
                ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized"));
                return;
            }

            authToken = authToken.substring(7);

            String username;
            try {
                username = securityController.verifyToken(authToken).getUsername();
                if (username == null) {
                    ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized"));
                    return;
                }
            } catch (Exception e) {
                ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized: " + e.getMessage()));
                return;
            }

            try {
                User user = userDAO.findByUsername(username);
                if (user == null) {
                    ctx.status(HttpStatus.BAD_REQUEST).json(returnObject.put("msg", "User not found"));
                    return;
                }

                LocalDate date = LocalDate.parse(ctx.pathParam("date"));
                List<ToDo> toDos = toDoDAO.getToDosByDateAndId(user.getId(), date);
                if (toDos.isEmpty()) {
                    ctx.status(HttpStatus.NOT_FOUND).json(returnObject.put("msg", "No to-dos found for the specified date"));
                    return;
                }

                List<ToDoDTO> toDoDTOS = toDos.stream().map(ToDoDTO::new).collect(Collectors.toList());
                ctx.json(toDoDTOS);
            } catch (Exception e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(returnObject.put("msg", "Internal server error: " + e.getMessage()));
            }
        };
    }

    @Override
    public Handler getUserTodos() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();

            // Check for authentication
            String authToken = ctx.header("Authorization");
            if (authToken == null || !authToken.startsWith("Bearer ")) {
                ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized"));
                return;
            }

            // Remove the "Bearer " prefix to get the actual token
            authToken = authToken.substring(7);

            UserDTO userDTO;
            try {
                userDTO = securityController.verifyToken(authToken);
                if (userDTO == null) {
                    ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized"));
                    return;
                }
            } catch (Exception e) {
                ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized: " + e.getMessage()));
                return;
            }

            try {
                User user = securityController.getUserByUsername(userDTO.getUsername());
                if (user == null) {
                    ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized"));
                    return;
                }
                List<ToDoDTO> toDoDTOS = user.getToDos().stream().map(ToDoDTO::new).collect(Collectors.toList());
                ctx.json(toDoDTOS);
            } catch (Exception e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(returnObject.put("msg", "Internal server error: " + e.getMessage()));
            }
        };
    }



    @Override
    public Handler getAllToDosById() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();

            // Check for authentication
            String authToken = ctx.header("Authorization");
            if (authToken == null || !authToken.startsWith("Bearer ")) {
                ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized"));
                return;
            }

            // Remove the "Bearer " prefix to get the actual token
            authToken = authToken.substring(7);

            UserDTO userDTO;
            try {
                userDTO = securityController.verifyToken(authToken);
                if (userDTO == null) {
                    ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized"));
                    return;
                }
            } catch (Exception e) {
                ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized: " + e.getMessage()));
                return;
            }

            try {
                User user = securityController.getUserByUsername(userDTO.getUsername());
                if (user == null) {
                    ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized"));
                    return;
                }
                List<ToDo> toDos = toDoDAO.getAllToDosByID(user.getId());
                List<ToDoDTO> toDoDTOS = toDos.stream().map(ToDoDTO::new).collect(Collectors.toList());
                ctx.json(toDoDTOS);
            } catch (Exception e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(returnObject.put("msg", "Internal server error: " + e.getMessage()));
            }
        };
    }


    @Override
    public Handler getToDoById() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                ToDo toDo = toDoDAO.getToDoById(id);
                ToDoDTO toDoDTO = new ToDoDTO(toDo);
                ctx.json(toDoDTO);
            } catch (Exception e) {
                ctx.status(500).json(returnObject.put("msg", "Internal server error"));
            }
        };
    }

    @Override
    public Handler getToDoByUserId() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int user_id = getLoggedInUserId(ctx);
                List<ToDo> toDoList = toDoDAO.getToDoByUserId(user_id);
                List<ToDoDTO> toDoDTOList = toDoList.stream()
                        .map(ToDoDTO::new)
                        .collect(Collectors.toList());
                ctx.json(toDoDTOList);
            } catch (Exception e) {
                ctx.status(500).json(returnObject.put("msg", "Internal server error"));
            }
        };
    }


    private int getLoggedInUserId(Context ctx) {
        String userId = ctx.sessionAttribute("user_id");
        if (userId != null) {
            return Integer.parseInt(userId);
        }
        throw new IllegalStateException("User not logged in");
    }

    public User getUserByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
    @Override
    public Handler createToDo() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            String authToken = ctx.header("Authorization");
            if (authToken == null || !authToken.startsWith("Bearer ")) {
                ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized"));
                return;
            }

            authToken = authToken.substring(7);

            UserDTO userDTO;
            try {
                userDTO = securityController.verifyToken(authToken);
                if (userDTO == null) {
                    ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized"));
                    return;
                }
            } catch (Exception e) {
                ctx.status(HttpStatus.UNAUTHORIZED).json(returnObject.put("msg", "Unauthorized: " + e.getMessage()));
                return;
            }

            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();
                // Parse the incoming JSON to a ToDoDTO
                ToDoDTO toDoInput = ctx.bodyAsClass(ToDoDTO.class);

                // Convert ToDoDTO to ToDo entity
                ToDo toDoToCreate = convertToEntity(toDoInput);

                // Fetch the user entity from the database
                User userEntity = getUserByUsername(userDTO.getUsername());
                if (userEntity == null) {
                    ctx.status(HttpStatus.BAD_REQUEST).json(returnObject.put("msg", "User not found"));
                    return;
                }

                // Merge the detached User entity into the current persistence context
                userEntity = em.merge(userEntity);

                // Set the user for the to-do
                toDoToCreate.setUser(userEntity);

                // Add the to-do to the user's set of to-dos
                userEntity.getToDos().add(toDoToCreate);

                // Persist the to-do entity
                em.persist(toDoToCreate);

                // Commit the transaction
                em.getTransaction().commit();

                // Convert the created ToDo back to ToDoDTO for response
                ToDoDTO createdToDoDTO = new ToDoDTO(toDoToCreate);

                // Set status as CREATED and return the created to-do
                ctx.status(HttpStatus.CREATED).json(createdToDoDTO);
            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(returnObject.put("msg", "Internal server error: " + e.getMessage()));
            } finally {
                em.close();
            }
        };
    }



    @Override
    public Handler updateToDo() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                // Parse the incoming JSON to a ToDoDTO
                ToDoDTO toDoInput = ctx.bodyAsClass(ToDoDTO.class);

                // Retrieve the to-do to be updated
                int id = Integer.parseInt(ctx.pathParam("id"));
                ToDo toDoToUpdate = toDoDAO.getToDoById(id);

                // Check if to-do exists
                if (toDoToUpdate == null) {
                    ctx.status(404).json(returnObject.put("msg", "ToDo not found"));
                    return;
                }

                // Update the to-do entity with new values from ToDoDTO
                updateToDoEntityWithDTO(toDoToUpdate, toDoInput);

                // Update the to-do in the database
                ToDo updatedToDo = toDoDAO.update(toDoToUpdate);

                // Respond with the updated to-do ID
                ctx.json(returnObject.put("updatedToDoId", updatedToDo.getToDoId()));
            } catch (NumberFormatException e) {
                ctx.status(400).json(returnObject.put("msg", "Invalid format for to-do ID"));
            } catch (Exception e) {
                ctx.status(500).json(returnObject.put("msg", "Internal server error: " + e.getMessage()));
            }
        };
    }

    private void updateToDoEntityWithDTO(ToDo toDo, ToDoDTO dto) {
        toDo.setTitle(dto.getTitle());
        toDo.setDate(dto.getDate());
        toDo.setCapacity(dto.getCapacity());
        toDo.setPrice(dto.getPrice());
        toDo.setStatus(dto.getStatus());
    }

    @Override
    public Handler deleteToDo() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                toDoDAO.delete(id);
                ctx.status(204);
            } catch (Exception e) {
                ctx.status(500).json(returnObject.put("msg", "Internal server error"));
            }
        };
    }

    @Override
    public Handler getAllRegistrationsForToDo() {
        return ctx -> {
            try {
                int toDoId = Integer.parseInt(ctx.pathParam("todo_id"));
                List<User> registrations = toDoDAO.getRegistrationsForToDoById(toDoId);
                List<UserDTO> registrationDTOs = convertToUserDTO(registrations);
                ctx.json(registrationDTOs);
            } catch (NumberFormatException e) {
                ctx.status(400).json(Map.of("msg", "Invalid to-do ID format"));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("msg", "Internal server error"));
            }
        };
    }

    @Override
    public Handler getRegistrationById() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int id = Integer.parseInt(ctx.pathParam("todo_id"));
                ctx.json("There are " + toDoDAO.getRegistrationsCountById(id) + " users registered");
            } catch (Exception e) {
                ctx.status(500).json(returnObject.put("msg", "Internal server error"));
            }
        };
    }

    @Override
    public Handler registerUserForToDo() {
        return ctx -> {
            int toDoId = Integer.parseInt(ctx.pathParam("todo_id"));
            JsonObject requestBody = JsonParser.parseString(ctx.body()).getAsJsonObject();
            int userId = requestBody.get("id").getAsInt();
            toDoDAO.addUserToToDo(userId, toDoId);
            ctx.status(200).result("User registered for the to-do successfully");
        };
    }

    @Override
    public Handler removeUserFromToDo() {
        return ctx -> {
            int toDoId = Integer.parseInt(ctx.pathParam("todo_id"));
            JsonObject requestBody = JsonParser.parseString(ctx.body()).getAsJsonObject();
            int userId = requestBody.get("id").getAsInt();
            toDoDAO.removeUserToDo(userId, toDoId);
            ctx.status(200).result("User removed from the to-do successfully");
        };
    }

    @Override
    public Handler getAllToDosByStatus() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                String status = ctx.pathParam("status");
                List<ToDo> toDos = toDoDAO.getToDoByStatus(status);
                List<ToDoDTO> toDoDTOS = toDos.stream().map(ToDoDTO::new).collect(Collectors.toList());
                ctx.json(toDoDTOS);
            } catch (NumberFormatException e) {
                ctx.status(400).json(returnObject.put("msg", "Invalid status format"));
            } catch (Exception e) {
                ctx.status(500).json(returnObject.put("msg", "Internal server error"));
            }
        };
    }
}
