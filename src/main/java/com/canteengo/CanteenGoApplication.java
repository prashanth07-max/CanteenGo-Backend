package com.canteengo;

import com.canteengo.entity.FoodItem;
import com.canteengo.entity.User;
import com.canteengo.repository.FoodItemRepository;
import com.canteengo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class CanteenGoApplication {

    private static final Logger log = LoggerFactory.getLogger(CanteenGoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CanteenGoApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Seeds the initial admin and a sample menu on first run.
     * The admin password comes from ADMIN_EMAIL / ADMIN_PASSWORD (hashed with BCrypt).
     * An existing ADMIN is never modified, so passwords are not reset on every startup.
     */
    @Bean
    CommandLineRunner seed(UserRepository users,
                           FoodItemRepository foods,
                           PasswordEncoder encoder,
                           @Value("${app.admin.email:}") String adminEmail,
                           @Value("${app.admin.password:}") String adminPassword) {
        return args -> {
            if (!users.existsByRole("ADMIN")) {
                String email = adminEmail == null ? "" : adminEmail.trim().toLowerCase();
                if (email.isBlank() || adminPassword == null || adminPassword.isBlank()) {
                    throw new IllegalStateException(
                            "No ADMIN account exists and initial credentials are not configured. "
                            + "Set the ADMIN_EMAIL and ADMIN_PASSWORD environment variables before the "
                            + "first start against a fresh database.");
                }
                User admin = new User();
                admin.setName("Canteen Admin");
                admin.setEmail(email);
                admin.setPassword(encoder.encode(adminPassword));
                admin.setRole("ADMIN");
                users.save(admin);
                log.info("Initial ADMIN account created for {}", email);
            }
            if (foods.count() == 0) {
                add(foods, "Veg Biryani", "Meals", 80, "Fragrant basmati rice with vegetables and spices");
                add(foods, "Chicken Biryani", "Meals", 120, "Hyderabadi style with raita");
                add(foods, "Masala Dosa", "Breakfast", 50, "Crispy dosa with potato masala, chutney and sambar");
                add(foods, "Idli (3 pcs)", "Breakfast", 30, "Soft idlis with sambar and chutney");
                add(foods, "Samosa", "Snacks", 15, "Crispy fried pastry with spiced potato filling");
                add(foods, "Veg Puff", "Snacks", 20, "Flaky baked puff with vegetable filling");
                add(foods, "Veg Fried Rice", "Meals", 70, "Wok-tossed rice with fresh vegetables");
                add(foods, "Masala Chai", "Beverages", 10, "Hot spiced milk tea");
                add(foods, "Cold Coffee", "Beverages", 40, "Chilled blended coffee");
                add(foods, "Fresh Lime Soda", "Beverages", 25, "Sweet or salted");
            }
        };
    }

    private void add(FoodItemRepository repo, String name, String category, double price, String desc) {
        FoodItem f = new FoodItem();
        f.setName(name);
        f.setCategory(category);
        f.setPrice(price);
        f.setDescription(desc);
        f.setAvailable(true);
        repo.save(f);
    }
}
