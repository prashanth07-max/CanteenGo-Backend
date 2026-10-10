package com.canteengo;

import com.canteengo.entity.FoodItem;
import com.canteengo.entity.User;
import com.canteengo.repository.FoodItemRepository;
import com.canteengo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class CanteenGoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CanteenGoApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Seeds a default admin and a sample menu on first run. */
    @Bean
    CommandLineRunner seed(UserRepository users, FoodItemRepository foods, PasswordEncoder encoder) {
        return args -> {
            if (users.findByEmail("admin@canteengo.com").isEmpty()) {
                User admin = new User();
                admin.setName("Canteen Admin");
                admin.setEmail("admin@canteengo.com");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole("ADMIN");
                users.save(admin);
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
