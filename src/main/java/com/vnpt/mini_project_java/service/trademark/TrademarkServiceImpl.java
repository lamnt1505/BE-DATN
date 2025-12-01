package com.vnpt.mini_project_java.service.trademark;

import com.vnpt.mini_project_java.dto.TrademarkDTO;
import com.vnpt.mini_project_java.entity.Trademark;
import com.vnpt.mini_project_java.respository.TrademarkReopsitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrademarkServiceImpl implements TrademarkService{
    @Autowired
    private final TrademarkReopsitory trademarkReopsitory;

    public TrademarkServiceImpl(TrademarkReopsitory trademarkReopsitory) {
        this.trademarkReopsitory = trademarkReopsitory;
    }

    @Override
    public List<TrademarkDTO> getAllTrademarkDTO(){
        List<Trademark> trademarks = trademarkReopsitory.findAll();
        return trademarks.stream().map(TrademarkDTO::new).collect(Collectors.toList());
    }

    @Override
    public TrademarkDTO saveDTO(TrademarkDTO dto){
        if (trademarkReopsitory.existsByTradeNameIgnoreCase(dto.getTradeName())) {
            throw new IllegalArgumentException("Tên thương hiệu đã tồn tại!");
        }
        Trademark trademark = new Trademark();
        trademark.setTradeName(dto.getTradeName());

        Trademark savedtrademark = trademarkReopsitory.save(trademark);
        return new TrademarkDTO(savedtrademark);
    }

    @Override
    public Trademark getTrademarkById(long id){
        Optional<Trademark> result = trademarkReopsitory.findById(id);
        if (result.isPresent()){
            return result.get();
        }else {
            throw new RuntimeException("Trademark not found with Id:" + id);
        }
    }

    @Override
    public Trademark updateTramemark(long tradeID, TrademarkDTO dto){

        Optional<Trademark> optionalTrademark = trademarkReopsitory.findById(tradeID);
        if (!optionalTrademark.isPresent()){
            throw new RuntimeException("Trademark with ID" + tradeID + "not found");
        }
        Trademark trademark = optionalTrademark.get();
        trademark.setTradeName(dto.getTradeName());

        return trademarkReopsitory.save(trademark);
    }

    @Override
    public void deleteTrademarkById(long id){
        Trademark trademark = trademarkReopsitory.findById(id).orElseThrow(
                () -> new RuntimeException("Trademark not found with id:" + id));
        trademarkReopsitory.deleteById(id);
    }

    @Override
    @Transactional
    public void importTrademarkExcel(List<TrademarkDTO> dtos) {

        for (TrademarkDTO dto : dtos) {
            Optional<Trademark> existedByName = trademarkReopsitory.findByTradeName(dto.getTradeName());
            if (existedByName.isPresent() &&
                    !existedByName.get().getTradeID().equals(dto.getTradeID())) {
                throw new RuntimeException(
                        "Tên thương hiệu '" + dto.getTradeName() + "' đã tồn tại!"
                );
            }

            Trademark trademark = trademarkReopsitory
                    .findById(dto.getTradeID())
                    .orElse(new Trademark());

            trademark.setTradeID(dto.getTradeID());
            trademark.setTradeName(dto.getTradeName());

            trademarkReopsitory.save(trademark);
        }
    }
}
