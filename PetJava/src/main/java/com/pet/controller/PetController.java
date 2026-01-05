package com.pet.controller;

import com.pet.common.Result;
import com.pet.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
public class PetController {
    private final PetService petService;

    @GetMapping
    public Result<List<Map<String, Object>>> getMyPets(@RequestHeader("X-User-Id") String userId) {
        return Result.success(petService.getByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable String id) {
        Map<String, Object> pet = petService.getById(id);
        if (pet == null) {
            return Result.error(404, "宠物不存在");
        }
        return Result.success(pet);
    }

    @PostMapping
    public Result<Map<String, Object>> create(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> data) {
        return Result.success(petService.create(userId, data));
    }

    @PutMapping("/{id}")
    public Result<Map<String, Object>> update(
            @PathVariable String id,
            @RequestBody Map<String, Object> data) {
        return Result.success(petService.update(id, data));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        petService.delete(id);
        return Result.success();
    }
}
