package com.canteengo.controller;

import com.canteengo.dto.Dtos.*;
import com.canteengo.entity.*;
import com.canteengo.repository.FoodItemRepository;
import com.canteengo.repository.FoodOrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final FoodOrderRepository orders;
    private final FoodItemRepository foods;

    public OrderController(FoodOrderRepository orders, FoodItemRepository foods) {
        this.orders = orders;
        this.foods = foods;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public OrderView place(@Valid @RequestBody OrderRequest req, HttpServletRequest http) {
        User user = (User) http.getAttribute("user");
        FoodOrder order = new FoodOrder();
        order.setUser(user);
        double total = 0;
        for (CartLine line : req.items()) {
            FoodItem food = foods.findById(line.foodId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item no longer exists"));
            if (!food.isAvailable()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, food.getName() + " is currently unavailable");
            }
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setFoodId(food.getId());
            oi.setFoodName(food.getName());
            oi.setQuantity(line.quantity());
            oi.setPrice(food.getPrice());
            order.getItems().add(oi);
            total += food.getPrice() * line.quantity();
        }
        order.setTotalAmount(total);
        return view(orders.save(order));
    }

    @GetMapping("/orders/my")
    @Transactional(readOnly = true)
    public List<OrderView> myOrders(HttpServletRequest http) {
        User user = (User) http.getAttribute("user");
        return orders.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::view).toList();
    }

    @GetMapping("/admin/orders")
    @Transactional(readOnly = true)
    public List<OrderView> allOrders() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList();
    }

    @PutMapping("/admin/orders/{id}/status")
    public OrderView updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest req) {
        FoodOrder o = orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        try {
            o.setStatus(OrderStatus.valueOf(req.status().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        }
        return view(orders.save(o));
    }

    private OrderView view(FoodOrder o) {
        return new OrderView(o.getId(), o.getUser().getName(), o.getStatus().name(), o.getTotalAmount(),
                o.getCreatedAt(),
                o.getItems().stream().map(i -> new OrderItemView(i.getFoodName(), i.getQuantity(), i.getPrice())).toList());
    }
}
