package app.dto;

import app.model.ToDo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Setter
@ToString
public class ToDoDTO {
    private int toDoId;
    private String title;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private int capacity;
    private double price;
    private String status;

    public ToDoDTO(int toDoId, String title, LocalDate date, int capacity, double price, String status) {
        this.toDoId = toDoId;
        this.title = title;
        this.date = date;
        this.capacity = capacity;
        this.price = price;
        this.status = status;
    }

    public ToDoDTO(ToDo toDo) {
        this.toDoId = toDo.getToDoId();
        this.title = toDo.getTitle();
        this.date = toDo.getDate();
        this.capacity = toDo.getCapacity();
        this.price = toDo.getPrice();
        this.status = toDo.getStatus();
    }
}
