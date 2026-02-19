package code.with.vanilson.market.promotions.api;

import code.with.vanilson.market.promotions.application.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotions & Marketing", description = "Endpoints for managing discount coupons and marketing campaigns")
public class PromotionController {

    private final PromotionService promotionService;

    @Operation(summary = "Create new coupon", description = "Generates a new promotion coupon for the current tenant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid coupon data or code already exists")
    })
    @PostMapping("/coupons")
    public ResponseEntity<PromotionDto.Response> createCoupon(
            @RequestBody @Valid PromotionDto.CreateCouponRequest request) {
        return ResponseEntity.ok(promotionService.createCoupon(request));
    }
}
