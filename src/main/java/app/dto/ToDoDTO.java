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
    private int ToDoId;
    private String Title;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate Date;
    private int Capacity;
    private double Price;
    private String Status;

    public ToDoDTO(int toDoId, String title, LocalDate date, int capacity, double price, String status) {
        ToDoId = toDoId;
        Title = title;
        Date = date;
        Capacity = capacity;
        Price = price;
        Status = status;
    }

    public ToDoDTO(String title, LocalDate date, int capacity, double price, String status) {
        Title = title;
        Date = date;
        Capacity = capacity;
        Price = price;
        Status = status;
    }

    public ToDoDTO(ToDo toDo) {
        this.ToDoId = toDo.getToDoId();
        this.Title = toDo.getTitle();
        this.Date = toDo.getDate();
        this.Capacity = toDo.getCapacity();
        this.Price = toDo.getPrice();
        this.Status = toDo.getStatus();
    }
}
