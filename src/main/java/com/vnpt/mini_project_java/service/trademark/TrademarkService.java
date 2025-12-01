package com.vnpt.mini_project_java.service.trademark;

import com.vnpt.mini_project_java.dto.TrademarkDTO;
import com.vnpt.mini_project_java.entity.Trademark;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public interface TrademarkService {
    List<TrademarkDTO> getAllTrademarkDTO();

    TrademarkDTO saveDTO(TrademarkDTO dto);

    Trademark getTrademarkById(long id);

    Trademark updateTramemark(long tradeID, TrademarkDTO dto);

    void deleteTrademarkById(long id);

    @Transactional
    void importTrademarkExcel(List<TrademarkDTO> dtos);
}
