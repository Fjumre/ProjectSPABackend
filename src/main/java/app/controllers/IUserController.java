package app.controllers;
import io.javalin.http.Handler;
public interface IUserController {

    Handler getAllUsers();
    Handler createUser();

    Handler getUserById();

    Handler updateUser();

    Handler deleteUser();

    Handler logout();
    Handler getUserByUsername();
    Handler updateUserByUsername();



}
