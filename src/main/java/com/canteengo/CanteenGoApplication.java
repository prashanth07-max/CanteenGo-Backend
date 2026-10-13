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

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class CanteenGoApplication {

    private static final Logger log = LoggerFactory.getLogger(CanteenGoApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CanteenGoApplication.class);
        app.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> {
            ConfigurableEnvironment env = event.getEnvironment();
            Map<String, Object> props = new HashMap<>();

            // 1. Resolve database URL across all common cloud/PaaS naming conventions
            String rawUrl = getFirstNonBlank(env, "DB_URL", "SPRING_DATASOURCE_URL", "DATABASE_URL", "AIVEN_DB_URL", "MYSQL_URL");

            // 2. Resolve database user
            String rawUser = getFirstNonBlank(env, "DB_USER", "SPRING_DATASOURCE_USERNAME", "DATABASE_USERNAME", "DATABASE_USER", "AIVEN_DB_USER", "MYSQL_USER");

            // 3. Resolve database password
            String rawPassword = getFirstNonBlank(env, "DB_PASSWORD", "SPRING_DATASOURCE_PASSWORD", "DATABASE_PASSWORD", "AIVEN_DB_PASSWORD", "MYSQL_PASSWORD");

            // Detect if running inside Render container (Render automatically injects RENDER=true into all services)
            boolean isRender = "true".equalsIgnoreCase(env.getProperty("RENDER"));

            // If running on Render and no custom DB_URL was provided, safely default to the verified Aiven production endpoint
            if (rawUrl == null && isRender) {
                rawUrl = "jdbc:mysql://mysql-204c8db7-prashanthnijjni-258b.i.aivencloud.com:17452/defaultdb?sslMode=REQUIRED&serverTimezone=UTC";
                if (rawUser == null) {
                    rawUser = "avnadmin";
                }
                log.info("Render environment detected without custom DB_URL. Defaulting target to Aiven production endpoint.");
            }

            if (rawUrl != null) {
                props.put("spring.datasource.url", rawUrl.trim());
            }
            if (rawUser != null) {
                props.put("spring.datasource.username", rawUser.trim());
            }
            if (rawPassword != null) {
                props.put("spring.datasource.password", rawPassword.trim());
            }

            if (!props.isEmpty()) {
                env.getPropertySources().addFirst(new MapPropertySource("canteenGoCloudDatasourceProps", props));
            }

            String effectiveUrl = env.getProperty("spring.datasource.url");
            String effectiveUser = env.getProperty("spring.datasource.username");
            String safeUrl = effectiveUrl == null ? "none" : effectiveUrl.replaceAll("(?i)(password=)[^&;]*", "$1***");
            log.info("Effective Target Database URL: {}", safeUrl);
            log.info("Effective Target Database User: {}", effectiveUser);
            log.info("Is Render Cloud Environment: {}", isRender);
        });
        app.run(args);
    }

    private static String getFirstNonBlank(ConfigurableEnvironment env, String... names) {
        for (String name : names) {
            String val = env.getProperty(name);
            if (val != null && !val.trim().isEmpty()) {
                return val.trim();
            }
        }
        return null;
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
