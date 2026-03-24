package com.hostel.management.config;

import com.hostel.management.model.Room;
import com.hostel.management.model.User;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      RoomRepository roomRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // Create default warden account if none exists
            if (!userRepository.existsByUsername("warden")) {
                User warden = new User();
                warden.setUsername("warden");
                warden.setPassword(passwordEncoder.encode("warden123"));
                warden.setFullName("Hostel Warden");
                warden.setRole("WARDEN");
                warden.setEnabled(true);
                userRepository.save(warden);
                System.out.println("Default warden account created: username=warden, password=warden123");
            }

            // Create default rooms if none exist
            if (roomRepository.count() == 0) {
                String[] roomNumbers = {"101", "102", "103", "104", "105", "106",
                                        "201", "202", "203", "204", "205", "206"};
                int[] floors = {1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2};
                for (int i = 0; i < roomNumbers.length; i++) {
                    Room room = new Room();
                    room.setRoomNumber(roomNumbers[i]);
                    room.setFloor(floors[i]);
                    room.setCapacity(3);
                    room.setCurrentOccupancy(0);
                    roomRepository.save(room);
                }
                System.out.println("Default rooms created (101-106, 201-206)");
            }
        };
    }
}
