package app.controllers;

import io.javalin.http.Handler;

public interface IToDoController {
    Handler getAllToDos();
    Handler getToDoByDate();

    Handler getToDoById();

    Handler createToDo();
    Handler updateToDo();
    Handler deleteToDo();
    Handler getAllRegistrationsForToDo();
    Handler getRegistrationById();
    Handler registerUserForToDo();
    Handler removeUserFromToDo();
    Handler getAllToDosByStatus();
    Handler getToDoByUserId();
    Handler getAllToDosById();
    Handler getUserTodos();
}
