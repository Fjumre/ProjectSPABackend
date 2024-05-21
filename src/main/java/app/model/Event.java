package app.model;

import app.dto.EventDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "event")
@Entity
@NamedQuery(name = "Event.findAll", query = "SELECT e FROM Event e")

public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "event_id", nullable = false, unique = true)
    private int EventId;
    @Column(name= "title")
    private String Title;
    @Column(name= "date")
    private LocalDateTime Date;
    @Column(name= "capacity")
    private int Capacity;
    @Column(name= "price")
    private double Price;
    @Column(name= "status")
    private String Status;
    @ManyToMany
    private  Set <User> users = new HashSet<>();

    public Event(int eventId, String title, LocalDateTime date, int capacity, double price, String status, Set<User> users) {
        EventId = eventId;
        Title = title;
        Date = date;
        Capacity = capacity;
        Price = price;
        Status = status;
        this.users = users;
    }

    public void addUser(User user) {
        users.add(user);
        user.getEvents().add(this);
    }
    public void removeUser(User user) {
        users.remove(user);
        user.getEvents().remove(this);
    }

    public Event(EventDTO eventDTO) {
        this.EventId = eventDTO.getEventId();
        this.Title = eventDTO.getTitle();
        this.Date = LocalDate.from(eventDTO.getDate()).atStartOfDay();
        this.Capacity = eventDTO.getCapacity();
        this.Price = eventDTO.getPrice();
        this.Status = eventDTO.getStatus();
    }
}