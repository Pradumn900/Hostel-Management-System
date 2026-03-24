package com.hostel.management.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String roomNumber;

    @Column(nullable = false)
    private int floor = 1;

    @Column(nullable = false)
    private int capacity = 3;

    @Column(nullable = false)
    private int currentOccupancy = 0;
}
