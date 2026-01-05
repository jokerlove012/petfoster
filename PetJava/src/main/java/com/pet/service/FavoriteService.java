package com.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.PageResult;
import com.pet.entity.Favorite;
import com.pet.entity.Institution;
import com.pet.mapper.FavoriteMapper;
import com.pet.mapper.InstitutionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final InstitutionMapper institutionMapper;
    private final InstitutionService institutionService;

    public void addFavorite(String userId, String institutionId) {
        Favorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getInstitutionId, institutionId));
        if (existing != null) {
            return;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setInstitutionId(institutionId);
        favoriteMapper.insert(favorite);
    }

    public void removeFavorite(String userId, String institutionId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getInstitutionId, institutionId));
    }

    public PageResult<Map<String, Object>> getFavorites(String userId, int page, int pageSize) {
        Page<Favorite> favPage = favoriteMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreatedAt));

        List<Map<String, Object>> list = new ArrayList<>();
        for (Favorite fav : favPage.getRecords()) {
            Map<String, Object> detail = institutionService.getDetail(fav.getInstitutionId());
            if (detail != null) {
                list.add(detail);
            }
        }
        return PageResult.of(list, page, pageSize, favPage.getTotal());
    }
}
