package app.controllers;

import io.javalin.http.Handler;

public interface IEventController {
    Handler getAllEvents();
    Handler getEventById();
    Handler createEvent();
    Handler updateEvent();
    Handler deleteEvent();
    Handler getAllRegistrationsForEvent();

    Handler getRegistrationById();

    Handler registerUserForEvent();

    Handler removeUserFromEvent();
    Handler getAllEventsByStatus();
}
