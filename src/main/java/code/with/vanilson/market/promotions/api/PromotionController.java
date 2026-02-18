package code.with.vanilson.market.promotions.api;

import code.with.vanilson.market.promotions.application.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping("/coupons")
    public ResponseEntity<PromotionDto.Response> createCoupon(
            @RequestBody @Valid PromotionDto.CreateCouponRequest request) {
        return ResponseEntity.ok(promotionService.createCoupon(request));
    }
}
