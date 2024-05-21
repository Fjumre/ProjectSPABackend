package app.dto;


import app.model.Event;
import app.model.Location;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@Setter
@ToString
public class EventDTO {
    private int EventId;
    private String Title;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate Date;
    private int Capacity;
    private double Price;
    private String Status;

    public EventDTO(int eventId, String title, LocalDate date, int capacity, double price, String status) {
        EventId = eventId;
        Title = title;
        Date = date;
        Capacity = capacity;
        Price = price;
        Status = status;
    }

    public EventDTO(String title, LocalDate date, int capacity, double price, String status) {
        Title = title;
        Date = date;
        Capacity = capacity;
        Price = price;
        Status = status;
    }

    public EventDTO(Event event) {
        EventId = event.getEventId();
        Title = event.getTitle();
        Date = LocalDate.from(event.getDate());
        Capacity = event.getCapacity();
        Price = event.getPrice();
        Status = event.getStatus();
    }

}
