package com.vnpt.mini_project_java.service.favorite;

import com.vnpt.mini_project_java.dto.FavoriteDTO;

import java.util.List;

public interface FavoriteService {
    String addProductToFavorite(Long accountID, Long productID);

    List<FavoriteDTO> getFavoritesByAccountId(Long accountId);

    boolean removeFavorite(Long accountId, Long productId);
}
