package app.controllers;

import app.config.HibernateConfig;
import app.dao.DoToDAO;
import app.dto.ToDoDTO;
import app.dto.UserDTO;
import app.model.ToDo;
import app.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



public class EventController implements IEventController {
    DoToDAO doToDAO = new DoToDAO();
    ObjectMapper objectMapper = new ObjectMapper();
    SecurityController securityController = new SecurityController();


    EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    EntityManager em = emf.createEntityManager();

    public EventController(DoToDAO doToDAO) {
        this.doToDAO = doToDAO;
    }


    private List<UserDTO> convertToUserDTO(List<User> users) {
        return users.stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }
//    private UserDTO convertToUserDTO(User user) {
//        return new UserDTO(user);
//    }

    private ToDo convertToEntity(ToDoDTO toDoDTO) {

        return new ToDo(toDoDTO);
    }

    public Handler getAllEvents() {
        return (ctx) -> {
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
                List<ToDo> toDos = doToDAO.getAllToDos();
                List<ToDoDTO> toDoDTOS = toDos.stream().map(ToDoDTO::new).collect(Collectors.toList());
                ctx.json(toDoDTOS);
            } catch (Exception e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(returnObject.put("msg", "Internal server error: " + e.getMessage()));
            }
        };
    }



    @Override
    public Handler getEventById() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                ToDo toDo = doToDAO.getTodoById(id);
                ToDoDTO toDoDTO = new ToDoDTO(toDo);
                ctx.json(toDoDTO);
            } catch (Exception e) {
                ctx.status(500);
                ctx.json(returnObject.put("msg", "Internal server error"));
            }
        };
    }

    @Override
    public Handler createEvent() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                // Parse the incoming JSON to an ToDoDTO
                ToDoDTO eventInput = ctx.bodyAsClass(ToDoDTO.class);

                // Convert ToDoDTO to ToDo entity
                ToDo toDoToCreate = convertToEntity(eventInput);

                // Create the event in the database
                ToDo createdToDo = doToDAO.create(toDoToCreate);

                // Convert the created ToDo back to ToDoDTO for response
                ToDoDTO createdToDoDTO = new ToDoDTO(createdToDo);


                // Set status as CREATED and return the created event
                ctx.status(HttpStatus.CREATED).json(createdToDoDTO);
            } catch (Exception e) {

                e.printStackTrace();
                ctx.status(500).json(returnObject.put("msg", "Internal server error: " + e.getMessage()));
            }
        };
    }


    @Override
    public Handler updateEvent() {
        return ctx -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                // Parse the incoming JSON to an ToDoDTO
                ToDoDTO eventInput = ctx.bodyAsClass(ToDoDTO.class);

                // Retrieve the event to be updated
                int id = Integer.parseInt(ctx.pathParam("id"));
                ToDo toDoToUpdate = doToDAO.getTodoById(id);

                // Check if event exists
                if (toDoToUpdate == null) {
                    ctx.status(404).json(returnObject.put("msg", "ToDo not found"));
                    return;
                }

                // Update the event entity with new values from ToDoDTO
                updateEventEntityWithDTO(toDoToUpdate, eventInput);

                // Update the event in the database
                ToDo updatedToDo = doToDAO.update(toDoToUpdate);

                // Respond with the updated event ID
                ctx.json(returnObject.put("updatedEventId", updatedToDo.getToDoId()));

            } catch (NumberFormatException e) {
                ctx.status(400).json(returnObject.put("msg", "Invalid format for event ID"));
            } catch (Exception e) {
                ctx.status(500).json(returnObject.put("msg", "Internal server error: " + e.getMessage()));
            }
        };
    }

    private void updateEventEntityWithDTO(ToDo toDo, ToDoDTO dto) {
        toDo.setTitle(dto.getTitle());
        toDo.setDate(dto.getDate().atStartOfDay());
        toDo.setCapacity(dto.getCapacity());
        toDo.setPrice(dto.getPrice());
        toDo.setStatus(dto.getStatus());

    }


    @Override
    public Handler deleteEvent() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                doToDAO.delete(id);
                ctx.status(204);
            } catch (Exception e) {
                ctx.status(500);
                ctx.json(returnObject.put("msg", "Internal server error"));
            }
        };
    }

    @Override
    public Handler getAllRegistrationsForEvent() {
        return ctx -> {
            try {
                int eventId = Integer.parseInt(ctx.pathParam("event_id"));
                List<User> registrations = doToDAO.getRegistrationsForToDoById(eventId);
                List<UserDTO> registrationDTOs = convertToUserDTO(registrations);
                ctx.json(registrationDTOs);
            } catch (NumberFormatException e) {
                ctx.status(400).json(Map.of("msg", "Invalid event ID format"));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("msg", "Internal server error"));
                e.printStackTrace();
            }
        };
    }


    @Override
    public Handler getRegistrationById() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                int id = Integer.parseInt(ctx.pathParam("event_id"));
                ctx.json("There are " + doToDAO.getRegistrationsCountById(id) + " users registered");
            } catch (Exception e) {
                ctx.status(500);
                System.out.println(e);
                ctx.json(returnObject.put("msg", "Internal server error"));
            }
        };
    }


    @Override
    public Handler registerUserForEvent() {
        return ctx -> {


            int eventId = Integer.parseInt(ctx.pathParam("event_id"));
            JsonObject requestBody = JsonParser.parseString(ctx.body()).getAsJsonObject();

            int userId = requestBody.get("id").getAsInt();

            doToDAO.addUserToEvent(userId, eventId);

            ctx.status(200).result("User registered for the event successfully");
        };
    }

    @Override
    public Handler removeUserFromEvent() {
        return ctx -> {
            int eventId = Integer.parseInt(ctx.pathParam("event_id"));
            JsonObject requestBody = JsonParser.parseString(ctx.body()).getAsJsonObject();

            int userId = requestBody.get("id").getAsInt();

            doToDAO.removeUserEvent(userId, eventId);

            ctx.status(200).result("User removed for the event successfully");
        };


    }


    @Override
    public Handler getAllEventsByStatus() {
        return (ctx) -> {
            ObjectNode returnObject = objectMapper.createObjectNode();
            try {
                String status = ctx.pathParam("status");
                List<ToDo> toDos = doToDAO.getToDoByStatus(status);

                List<ToDoDTO> toDoDTOS = toDos.stream()
                        .map(ToDoDTO::new)
                        .collect(Collectors.toList());

                ctx.json(toDoDTOS);
            } catch (NumberFormatException e) {
                ctx.status(400).json(returnObject.put("msg", "Invalid category ID format"));
            } catch (Exception e) {
                ctx.status(500);
                System.out.println(e);
                ctx.json(returnObject.put("msg", "Internal server error"));
            }
        };
    }
}
