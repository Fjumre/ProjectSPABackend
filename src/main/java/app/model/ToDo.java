package app.model;

import app.dto.ToDoDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "event")
@Entity
@NamedQuery(name = "Event.findAll", query = "SELECT e FROM ToDo e")

public class ToDo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "todoid", nullable = false, unique = true)
    private int ToDoId;
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

    public ToDo(int toDoId, String title, LocalDateTime date, int capacity, double price, String status, Set<User> users) {
        ToDoId = toDoId;
        Title = title;
        Date = date;
        Capacity = capacity;
        Price = price;
        Status = status;
        this.users = users;
    }

    public void addUser(User user) {
        users.add(user);
        user.getToDos().add(this);
    }
    public void removeUser(User user) {
        users.remove(user);
        user.getToDos().remove(this);
    }

    public ToDo(ToDoDTO toDoDTO) {
        this.ToDoId = toDoDTO.getToDoId();
        this.Title = toDoDTO.getTitle();
        this.Date = LocalDate.from(toDoDTO.getDate()).atStartOfDay();
        this.Capacity = toDoDTO.getCapacity();
        this.Price = toDoDTO.getPrice();
        this.Status = toDoDTO.getStatus();
    }
}