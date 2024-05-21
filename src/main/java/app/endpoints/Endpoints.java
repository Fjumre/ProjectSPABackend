package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.controllers.*;
import app.dao.EventDAO;
import app.dao.UserDAO;
import app.security.RouteRoles;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.get;

public class Endpoints {
    private static ISecurityController securityController = new SecurityController();

    static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static EventDAO eventDAO= new EventDAO(emf);
    private static IEventController eventController= new EventController(eventDAO);

    private static UserDAO userDAO= new UserDAO(emf);
    private static IUserController userController= new UserController(userDAO);
    private static ObjectMapper om = new ObjectMapper();

    public static void startServer(int port){

        ObjectMapper om = new ObjectMapper();
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        ApplicationConfig applicationConfig = ApplicationConfig.getInstance();
        applicationConfig
                .initiateServer()
                .startServer(7070)
                .setExceptionHandling()
                .setupAccessManager()
                .configureCors()
                .setRoute(getSecurityRoutes())
                .setRoute(getSecuredRoutes())
                .setRoute(() -> {

                    getUserRoutes();

                })
                .checkSecurityRoles();
    }
    public static void getUserRoutes(){
        before(securityController.authenticate());
        path("/user", () -> {
            path("/", () -> {
                before(securityController.authenticate());
                get("/events", eventController.getAllEvents(), RouteRoles.USER, RouteRoles.ADMIN);
                get("/all", userController.getAllUsers(), RouteRoles.ADMIN);
                get("/{id}", userController.getUserById(), RouteRoles.ADMIN);
                post("/create", userController.createUser(), RouteRoles.ADMIN);
                put("/update/{id}", userController.updateUser(), RouteRoles.USER, RouteRoles.ADMIN);
                delete("/delete/{id}", userController.deleteUser(), RouteRoles.USER, RouteRoles.ADMIN);
                post("/logout", userController.logout(), RouteRoles.USER, RouteRoles.ADMIN);
                get("/error", ctx -> {
                    throw new Exception(String.valueOf(ApplicationConfig.getInstance().setExceptionHandling()));
                });
            });
        });
    }
    public static EndpointGroup getSecurityRoutes() {
        return ()->{
            path("/auth", ()->{
                post("/login", securityController.login(), RouteRoles.ANYONE);
                post("/register", securityController.register(),RouteRoles.ANYONE);
                post("/resetpassword", securityController.resetOfPassword(), RouteRoles.ANYONE);
            });
        };
    }

    public static EndpointGroup getSecuredRoutes(){
        return ()->{
            path("/protected", ()->{
                before(securityController.authenticate());
                get("/user",(ctx)->ctx.json(om.createObjectNode().put("msg",  "Hello from USER Protected")),RouteRoles.USER);
                get("/admin",(ctx)->ctx.json(om.createObjectNode().put("msg",  "Hello from ADMIN Protected")),RouteRoles.ADMIN);
            });
        };
    }
}
