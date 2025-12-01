package com.vnpt.mini_project_java.service.productvotes;

import com.vnpt.mini_project_java.dto.ProductVoteDTO;
import com.vnpt.mini_project_java.entity.Account;
import com.vnpt.mini_project_java.entity.Product;
import com.vnpt.mini_project_java.entity.ProductVote;
import com.vnpt.mini_project_java.respository.AccountRepository;
import com.vnpt.mini_project_java.respository.ProductRepository;
import com.vnpt.mini_project_java.respository.ProductVotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductVotesServiceImpl implements ProductVotesService{

    @Autowired
    private ProductVotesRepository productVoteRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ProductVote saveVote(ProductVoteDTO voteDTO) {
        ProductVote vote = new ProductVote();
        vote.setRating(voteDTO.getRating());
        vote.setComment(voteDTO.getComment());

        if (voteDTO.getAccountID() != null) {
            Account account = accountRepository.findById(voteDTO.getAccountID())
                    .orElseThrow(() -> new EntityNotFoundException("Account not found"));
            vote.setAccount(account);
        } else {
            vote.setAccount(null);
        }

        Product product = productRepository.findById(voteDTO.getProductID())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        vote.setProduct(product);

        return productVoteRepository.save(vote);
    }

    public List<ProductVote> findByProductId(Long productId) {
        return productVoteRepository.findByProduct_ProductID(productId);
    }

    @Override
    public List<ProductVoteDTO> getAllvoteDTO(){
        List<ProductVote> votes = productVoteRepository.findAll();
        return votes.stream().map(ProductVoteDTO::new).collect(Collectors.toList());
    }

    @Override
    public Page<ProductVoteDTO> getPaginatedProducts(Pageable pageable) {
        Page<ProductVote> productPage = productVoteRepository.findAll(pageable);

        List<ProductVoteDTO> productDTOs = productPage.getContent().stream()
                .map(ProductVoteDTO::new)
                .collect(Collectors.toList());

        return new PageImpl<>(productDTOs, pageable, productPage.getTotalElements());
    }

    @Override
    public List<ProductVoteDTO> getVotesByProductId(Long productId) {
        List<ProductVote> votes = productVoteRepository.findByProductId(productId);

        return votes.stream()
                .map(ProductVoteDTO::new)  // dùng constructor DTO(ProductVote)
                .collect(Collectors.toList());
    }
}
