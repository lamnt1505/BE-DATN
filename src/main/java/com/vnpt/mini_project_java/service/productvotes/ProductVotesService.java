package com.vnpt.mini_project_java.service.productvotes;

import com.vnpt.mini_project_java.dto.ProductVoteDTO;
import com.vnpt.mini_project_java.entity.ProductVote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductVotesService {
    ProductVote saveVote(ProductVoteDTO voteDTO);

    List<ProductVoteDTO> getAllvoteDTO();

    Page<ProductVoteDTO> getPaginatedProducts(Pageable pageable);

    List<ProductVoteDTO> getVotesByProductId(Long productId);
}
