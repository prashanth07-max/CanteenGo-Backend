package com.canteengo.controller;

import com.canteengo.entity.FoodItem;
import com.canteengo.repository.FoodItemRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FoodController {

    private final FoodItemRepository foods;

    public FoodController(FoodItemRepository foods) {
        this.foods = foods;
    }

    /** Public menu: only items currently available. */
    @GetMapping("/foods")
    public List<FoodItem> menu() {
        return foods.findByAvailableTrue();
    }

    @GetMapping("/admin/foods")
    public List<FoodItem> all() {
        return foods.findAll();
    }

    @PostMapping("/admin/foods")
    @ResponseStatus(HttpStatus.CREATED)
    public FoodItem create(@Valid @RequestBody FoodItem f) {
        f.setId(null);
        return foods.save(f);
    }

    @PutMapping("/admin/foods/{id}")
    public FoodItem update(@PathVariable Long id, @Valid @RequestBody FoodItem in) {
        FoodItem f = foods.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found"));
        f.setName(in.getName());
        f.setCategory(in.getCategory());
        f.setPrice(in.getPrice());
        f.setDescription(in.getDescription());
        f.setAvailable(in.isAvailable());
        return foods.save(f);
    }

    @DeleteMapping("/admin/foods/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!foods.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found");
        foods.deleteById(id);
    }
}
