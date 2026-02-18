package code.with.vanilson.market.promotions.application;

import code.with.vanilson.market.promotions.api.PromotionDto;
import code.with.vanilson.market.promotions.domain.Coupon;
import code.with.vanilson.market.promotions.domain.CouponRepository;
import code.with.vanilson.market.shared.domain.DomainException;
import code.with.vanilson.market.shared.domain.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final CouponRepository couponRepository;

    @Transactional
    public PromotionDto.Response createCoupon(PromotionDto.CreateCouponRequest request) {
        if (couponRepository.findByCode(request.getCode()).isPresent()) {
            throw new DomainException("Coupon code already exists");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .discount(new Money(request.getDiscountAmount(), request.getCurrency()))
                .expiryDate(request.getExpiryDate())
                .active(true)
                .build();

        coupon = couponRepository.save(coupon);
        return mapToResponse(coupon);
    }

    // validate logic

    private PromotionDto.Response mapToResponse(Coupon coupon) {
        PromotionDto.Response response = new PromotionDto.Response();
        response.setId(coupon.getId().toString());
        response.setCode(coupon.getCode());
        response.setDiscountAmount(coupon.getDiscount().getAmount());
        response.setActive(coupon.isActive());
        return response;
    }
}
