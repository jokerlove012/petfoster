package com.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.entity.Pet;
import com.pet.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PetService {
    private final PetMapper petMapper;

    public List<Map<String, Object>> getByUserId(String userId) {
        List<Pet> pets = petMapper.selectList(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getUserId, userId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Pet pet : pets) {
            result.add(toPetVO(pet));
        }
        return result;
    }

    public Map<String, Object> getById(String id) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            return null;
        }
        return toPetVO(pet);
    }

    public Map<String, Object> create(String userId, Map<String, Object> data) {
        Pet pet = new Pet();
        pet.setUserId(userId);
        pet.setName((String) data.get("name"));
        pet.setSpecies((String) data.get("species"));
        pet.setBreed((String) data.get("breed"));
        pet.setAge(data.get("age") != null ? ((Number) data.get("age")).intValue() : null);
        pet.setWeight(data.get("weight") != null ? new java.math.BigDecimal(data.get("weight").toString()) : null);
        pet.setGender((String) data.get("gender"));
        pet.setAvatar((String) data.get("avatar"));
        pet.setHealthNotes((String) data.get("healthNotes"));
        pet.setVaccinated(data.get("vaccinated") != null ? (Boolean) data.get("vaccinated") : false);
        pet.setNeutered(data.get("neutered") != null ? (Boolean) data.get("neutered") : false);

        petMapper.insert(pet);
        return toPetVO(pet);
    }

    public Map<String, Object> update(String id, Map<String, Object> data) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            throw new RuntimeException("宠物不存在");
        }

        if (data.containsKey("name")) pet.setName((String) data.get("name"));
        if (data.containsKey("species")) pet.setSpecies((String) data.get("species"));
        if (data.containsKey("breed")) pet.setBreed((String) data.get("breed"));
        if (data.containsKey("age")) pet.setAge(data.get("age") != null ? ((Number) data.get("age")).intValue() : null);
        if (data.containsKey("weight")) pet.setWeight(data.get("weight") != null ? new java.math.BigDecimal(data.get("weight").toString()) : null);
        if (data.containsKey("gender")) pet.setGender((String) data.get("gender"));
        if (data.containsKey("avatar")) pet.setAvatar((String) data.get("avatar"));
        if (data.containsKey("healthNotes")) pet.setHealthNotes((String) data.get("healthNotes"));
        if (data.containsKey("vaccinated")) pet.setVaccinated((Boolean) data.get("vaccinated"));
        if (data.containsKey("neutered")) pet.setNeutered((Boolean) data.get("neutered"));

        petMapper.updateById(pet);
        return toPetVO(pet);
    }

    public void delete(String id) {
        petMapper.deleteById(id);
    }

    private Map<String, Object> toPetVO(Pet pet) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", pet.getId());
        vo.put("userId", pet.getUserId());
        vo.put("name", pet.getName());
        vo.put("species", pet.getSpecies());
        vo.put("breed", pet.getBreed());
        vo.put("age", pet.getAge());
        vo.put("weight", pet.getWeight());
        vo.put("gender", pet.getGender());
        vo.put("avatar", pet.getAvatar());
        vo.put("healthNotes", pet.getHealthNotes());
        vo.put("vaccinated", pet.getVaccinated());
        vo.put("neutered", pet.getNeutered());
        vo.put("createdAt", pet.getCreatedAt());
        vo.put("updatedAt", pet.getUpdatedAt());
        return vo;
    }
}
