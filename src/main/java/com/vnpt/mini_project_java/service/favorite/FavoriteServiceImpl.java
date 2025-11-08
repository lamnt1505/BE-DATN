package com.vnpt.mini_project_java.service.favorite;

import com.vnpt.mini_project_java.dto.FavoriteDTO;
import com.vnpt.mini_project_java.entity.Account;
import com.vnpt.mini_project_java.entity.Favorite;
import com.vnpt.mini_project_java.entity.Product;
import com.vnpt.mini_project_java.respository.AccountRepository;
import com.vnpt.mini_project_java.respository.FavoriteRepository;
import com.vnpt.mini_project_java.respository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService{
    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public String addProductToFavorite(Long accountID, Long productID) {
        Account account = accountRepository.findById(accountID).orElse(null);
        Product product = productRepository.findById(productID).orElse(null);

        if (account == null || product == null) {
            return "Tài khoản hoặc sản phẩm không tồn tại";
        }


        Optional<Favorite> existingFavorite = favoriteRepository.findByAccountAndProduct(account, product);
        System.out.println(existingFavorite + "test");
        if (existingFavorite.isPresent()) {
            return "Sản phẩm đã có trong danh sách yêu thích";
        }


        Favorite favorite = new Favorite();
        favorite.setAccount(account);
        favorite.setProduct(product);
        favoriteRepository.save(favorite);

        return "Đã thêm sản phẩm vào danh sách yêu thích";
    }
    @Override
    public List<FavoriteDTO> getFavoritesByAccountId(Long accountId) {
        return favoriteRepository.findByAccount_AccountID(accountId)
                .stream()
                .map(favorite -> {
                    Product product = favorite.getProduct();
                    return new FavoriteDTO(
                            product.getProductID(),
                            product.getProductName(),
                            product.getImage(),
                            product.getPrice()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean removeFavorite(Long accountId, Long productId) {
        Optional<Favorite> favoriteOpt = favoriteRepository.findByAccount_AccountIDAndProduct_ProductID(accountId, productId);
        if (favoriteOpt.isPresent()) {
            favoriteRepository.delete(favoriteOpt.get());
            return true;
        }
        return false;
    }
}
