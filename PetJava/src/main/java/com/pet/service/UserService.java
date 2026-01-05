package com.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.dto.LoginRequest;
import com.pet.dto.RegisterRequest;
import com.pet.entity.Pet;
import com.pet.entity.User;
import com.pet.mapper.PetMapper;
import com.pet.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PetMapper petMapper;

    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.getPhone())
                .eq(User::getRole, request.getRole()));
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("user", toUserVOWithPets(user));
        result.put("token", UUID.randomUUID().toString().replace("-", ""));
        return result;
    }

    public Map<String, Object> register(RegisterRequest request) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, request.getPhone())
                .eq(User::getRole, request.getRole()));
        
        if (existing != null) {
            throw new RuntimeException("该手机号已注册");
        }
        
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());
        user.setName(request.getName() != null ? request.getName() : "用户" + request.getPhone().substring(7));
        user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + request.getPhone());
        
        userMapper.insert(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("user", toUserVOWithPets(user));
        return result;
    }

    public User getById(String id) {
        return userMapper.selectById(id);
    }

    public User getByPhone(String phone, String role) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .eq(User::getRole, role));
    }

    public Map<String, Object> getProfile(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return toUserVOWithPets(user);
    }

    public Map<String, Object> updateProfile(String userId, Map<String, Object> data) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新用户基本信息
        if (data.containsKey("name")) {
            user.setName((String) data.get("name"));
        }
        if (data.containsKey("nickname")) {
            user.setName((String) data.get("nickname"));
        }
        if (data.containsKey("email")) {
            user.setEmail((String) data.get("email"));
        }
        if (data.containsKey("avatar")) {
            user.setAvatar((String) data.get("avatar"));
        }
        if (data.containsKey("gender")) {
            user.setGender((String) data.get("gender"));
        }
        if (data.containsKey("birthday") && data.get("birthday") != null) {
            String birthdayStr = (String) data.get("birthday");
            if (!birthdayStr.isEmpty()) {
                user.setBirthday(java.time.LocalDate.parse(birthdayStr));
            }
        }
        if (data.containsKey("address")) {
            user.setAddress((String) data.get("address"));
        }

        userMapper.updateById(user);
        return toUserVOWithPets(user);
    }

    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(newPassword);
        userMapper.updateById(user);
    }

    private Map<String, Object> toUserVO(User user) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", user.getId());
        vo.put("email", user.getEmail());
        vo.put("phone", user.getPhone());
        vo.put("name", user.getName());
        vo.put("avatar", user.getAvatar());
        vo.put("gender", user.getGender());
        vo.put("birthday", user.getBirthday());
        vo.put("address", user.getAddress());
        vo.put("role", user.getRole());
        vo.put("createdAt", user.getCreatedAt());
        vo.put("updatedAt", user.getUpdatedAt());
        if ("institution_staff".equals(user.getRole())) {
            vo.put("institutionId", user.getInstitutionId());
            vo.put("position", user.getPosition());
        }
        if ("admin".equals(user.getRole())) {
            vo.put("adminLevel", user.getAdminLevel());
        }
        return vo;
    }

    private Map<String, Object> toUserVOWithPets(User user) {
        Map<String, Object> vo = toUserVO(user);
        
        // 获取用户的宠物列表
        if ("pet_owner".equals(user.getRole())) {
            List<Pet> pets = petMapper.selectList(new LambdaQueryWrapper<Pet>()
                    .eq(Pet::getUserId, user.getId()));
            List<Map<String, Object>> petList = new ArrayList<>();
            for (Pet pet : pets) {
                Map<String, Object> petVO = new HashMap<>();
                petVO.put("id", pet.getId());
                petVO.put("name", pet.getName());
                petVO.put("species", pet.getSpecies());
                petVO.put("breed", pet.getBreed());
                petVO.put("age", pet.getAge());
                petVO.put("weight", pet.getWeight());
                petVO.put("gender", pet.getGender());
                petVO.put("avatar", pet.getAvatar());
                petVO.put("healthNotes", pet.getHealthNotes());
                petVO.put("vaccinated", pet.getVaccinated());
                petVO.put("neutered", pet.getNeutered());
                petList.add(petVO);
            }
            vo.put("pets", petList);
        }
        
        return vo;
    }
}
