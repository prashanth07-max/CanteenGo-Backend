package com.canteengo.repository;

import com.canteengo.entity.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    List<FoodOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<FoodOrder> findAllByOrderByCreatedAtDesc();
}
