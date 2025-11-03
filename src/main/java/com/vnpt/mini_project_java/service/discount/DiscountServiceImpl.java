package com.vnpt.mini_project_java.service.discount;


import com.vnpt.mini_project_java.dto.DiscountDTO;
import com.vnpt.mini_project_java.entity.Discount;
import com.vnpt.mini_project_java.entity.Product;
import com.vnpt.mini_project_java.respository.DiscountRepository;
import com.vnpt.mini_project_java.respository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiscountServiceImpl implements DiscountService {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);


    @Autowired
    DiscountRepository discountRepository;

    @Autowired
    ProductRepository productRepository;

    @Override
    public Discount createDiscountCode(DiscountDTO discountDTO) {
        LocalDate startDate = LocalDate.parse(discountDTO.getDateStart(),
                dateTimeFormatter);
        LocalDate endDate = LocalDate.parse(discountDTO.getDateFinish(),
                dateTimeFormatter);

        if (discountRepository.existsByDiscountCodeIgnoreCase(discountDTO.getDiscountName())) {
            throw new IllegalArgumentException("⚠ Mã giảm giá đã tồn tại!");
        }

        if (discountDTO.getDateStart() == null || discountDTO.getDateFinish() == null) {
            throw new IllegalArgumentException("Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc!");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc!");
        }

        List<Discount> overlapping = discountRepository.findOverlappingDiscounts(startDate, endDate);

        if (discountRepository.existsByDiscountNameIgnoreCase(discountDTO.getDiscountName())) {
            throw new IllegalArgumentException("⚠ Tên chương trình khuyến mãi đã tồn tại!");
        }

        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("⚠ Đã có chương trình khuyến mãi trong khoảng thời gian này!");
        }

        String code = generateReadableCode(discountDTO.getDiscountName());

        Discount discount = new Discount();
        discount.setDiscountCode(code);
        discount.setDiscountName(discountDTO.getDiscountName());
        discount.setDiscountPercent(discountDTO.getDiscountPercent());
        discount.setDateStart(LocalDate.parse(discountDTO.getDateStart(),dateTimeFormatter));
        discount.setDateFinish(LocalDate.parse(discountDTO.getDateFinish(),dateTimeFormatter));
        discount.setActive(true);

        return discountRepository.save(discount);
    }

    @Override
    public Discount updateDiscount(Long id, DiscountDTO dto) {
        Discount existing = discountRepository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setDiscountName(dto.getDiscountName());
        existing.setDiscountPercent(dto.getDiscountPercent());
        existing.setDiscountCode(dto.getDiscountCode());
        existing.setDateStart(LocalDate.parse(dto.getDateStart(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        existing.setDateFinish(LocalDate.parse(dto.getDateFinish(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        existing.setActive(dto.getActive());

        if (dto.getProductID() != null) {
            Product product = productRepository.findById(dto.getProductID()).orElse(null);
            existing.setProduct(product);
        } else {
            existing.setProduct(null);
        }

        return discountRepository.save(existing);
    }

    private String generateReadableCode(String name) {
        String prefix = (name != null && !name.isEmpty())
                ? name.toUpperCase().replaceAll("\\s+", "")
                : "SALE";
        String year = String.valueOf(LocalDate.now().getYear());
        int random = (int) (Math.random() * 90 + 10);
        return prefix + year + random;
    }

    @Override
    public Optional<Discount> validateDiscountCode(String discountCode) {
        Optional<Discount> discount = discountRepository.findByDiscountCode(discountCode);
        if (discount.isPresent()) {
            LocalDate today = LocalDate.now();
            if (!discount.get().getDateFinish().isBefore(today)) {
                return discount;
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByCode(String code) {
        return discountRepository.existsByDiscountCodeIgnoreCase(code);
    }

    @Override
    public double applyDiscount(double price, String discountCode) {
        Discount discount = discountRepository.findByDiscountCode(discountCode)
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại: " + discountCode));

        LocalDate today = LocalDate.now();

        if (discount.getDateStart() != null && today.isBefore(discount.getDateStart())) {
            throw new IllegalArgumentException("⚠ Mã giảm giá chưa được áp dụng.");
        }

        if (discount.getDateFinish() != null && today.isAfter(discount.getDateFinish())) {
            throw new IllegalArgumentException("⚠ Mã giảm giá đã hết hạn.");
        }

        double discountAmount = (discount.getDiscountPercent());
        return price * (1 - discountAmount / 100);
    }

    @Override
    public Optional<Discount> getLatestDiscount() {
        return discountRepository.findTopByOrderByDateStartDesc();
    }

    @Override
    public List<DiscountDTO> getAllDiscounts() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return discountRepository.findAll()
                .stream()
                .map(discount -> {
                    DiscountDTO dto = new DiscountDTO();
                    dto.setDiscountID(discount.getDiscountID());
                    dto.setDiscountName(discount.getDiscountName());
                    dto.setDiscountPercent(discount.getDiscountPercent());
                    dto.setDateStart(discount.getDateStart() != null ? discount.getDateStart().format(formatter) : null);
                    dto.setDateFinish(discount.getDateFinish() != null ? discount.getDateFinish().format(formatter) : null);
                    dto.setDiscountCode(discount.getDiscountCode());
                    dto.setProductID(discount.getProduct() != null ? discount.getProduct().getProductID() : null);
                    dto.setActive(discount.getActive());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public String toggleActive(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá ID: " + id));

        discount.setActive(!discount.getActive());
        discountRepository.save(discount);

        return discount.getActive() ? "Đã kích hoạt mã giảm giá!" : "Đã vô hiệu hoá mã giảm giá!";
    }

    @Override
    public Discount findById(Long id) {
        return discountRepository.findById(id).orElse(null);
    }

    @Override
    public boolean deleteDiscount(Long id) {
        Optional<Discount> discountOpt = discountRepository.findById(id);
        if (!discountOpt.isPresent()) {
            return false;
        }
        discountRepository.deleteById(id);
        return true;
    }
}
