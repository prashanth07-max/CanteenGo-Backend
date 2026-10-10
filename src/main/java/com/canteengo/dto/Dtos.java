package com.canteengo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public class Dtos {
    public record RegisterRequest(
            @NotBlank(message = "Name is required") String name,
            @NotBlank(message = "Email is required") @Email(message = "Invalid email") String email,
            @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password) {}

    public record LoginRequest(@NotBlank(message = "Email is required") String email,
                               @NotBlank(message = "Password is required") String password) {}

    public record AuthResponse(String token, Long id, String name, String email, String role) {}

    public record CartLine(@NotNull Long foodId, @Min(1) int quantity) {}

    public record OrderRequest(@NotEmpty(message = "Cart is empty") @Valid List<CartLine> items) {}

    public record StatusRequest(@NotBlank String status) {}

    public record OrderItemView(String foodName, int quantity, double price) {}

    public record OrderView(Long id, String studentName, String status, double total,
                            LocalDateTime createdAt, List<OrderItemView> items) {}
}
