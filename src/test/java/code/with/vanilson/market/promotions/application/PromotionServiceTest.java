package code.with.vanilson.market.promotions.application;

import code.with.vanilson.market.promotions.api.PromotionDto;
import code.with.vanilson.market.promotions.domain.Coupon;
import code.with.vanilson.market.promotions.domain.CouponRepository;
import code.with.vanilson.market.shared.domain.DomainException;
import code.with.vanilson.market.shared.domain.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromotionService Unit Tests")
class PromotionServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private PromotionService promotionService;

    private static final UUID COUPON_ID = UUID.randomUUID();
    private static final String COUPON_CODE = "SAVE20";
    private static final BigDecimal DISCOUNT_AMOUNT = new BigDecimal("20.00");
    private static final String CURRENCY = "USD";
    private static final Instant EXPIRY_DATE = Instant.now().plusSeconds(86400 * 30);

    // ========================= Create Coupon Tests =========================

    @Test
    @DisplayName("should successfully create a new coupon with valid request")
    void testCreateCoupon_Success() {
        // Arrange
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode(COUPON_CODE);
        request.setDiscountAmount(DISCOUNT_AMOUNT);
        request.setCurrency(CURRENCY);
        request.setExpiryDate(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Coupon savedCoupon = Coupon.builder()
                .code(COUPON_CODE)
                .discount(new Money(DISCOUNT_AMOUNT, CURRENCY))
                .expiryDate(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant())
                .active(true)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", COUPON_ID);


        when(couponRepository.findByCode(COUPON_CODE)).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        PromotionDto.Response response = promotionService.createCoupon(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(COUPON_ID.toString());
        assertThat(response.getCode()).isEqualTo(COUPON_CODE);
        assertThat(response.getDiscountAmount()).isEqualTo(DISCOUNT_AMOUNT);
        assertThat(response.isActive()).isTrue();

        verify(couponRepository, times(1)).findByCode(COUPON_CODE);
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("should throw DomainException when coupon code already exists")
    void testCreateCoupon_CodeAlreadyExists() {
        // Arrange
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode(COUPON_CODE);
        request.setDiscountAmount(DISCOUNT_AMOUNT);
        request.setCurrency(CURRENCY);
        request.setExpiryDate(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Coupon existingCoupon = Coupon.builder()
                .code(COUPON_CODE)
                .active(true)
                .build();
        ReflectionTestUtils.setField(existingCoupon, "id", COUPON_ID);

        when(couponRepository.findByCode(COUPON_CODE)).thenReturn(Optional.of(existingCoupon));

        // Act & Assert
        assertThatThrownBy(() -> promotionService.createCoupon(request))
                .isInstanceOf(DomainException.class)
                .hasMessage("Coupon code already exists");

        verify(couponRepository, times(1)).findByCode(COUPON_CODE);
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    @DisplayName("should successfully create coupon with small discount")
    void testCreateCoupon_SmallDiscount() {
        // Arrange
        BigDecimal smallDiscount = new BigDecimal("0.50");
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode("TINY");
        request.setDiscountAmount(smallDiscount);
        request.setCurrency("USD");
        request.setExpiryDate(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Coupon savedCoupon = Coupon.builder()

                .code("TINY")
                .discount(new Money(smallDiscount, "USD"))
                .expiryDate(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())
                .active(true)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", COUPON_ID);

        when(couponRepository.findByCode("TINY")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        PromotionDto.Response response = promotionService.createCoupon(request);

        // Assert
        assertThat(response.getDiscountAmount()).isEqualTo(smallDiscount);

        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("should successfully create coupon with large discount")
    void testCreateCoupon_LargeDiscount() {
        // Arrange
        BigDecimal largeDiscount = new BigDecimal("9999.99");
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode("HUGE");
        request.setDiscountAmount(largeDiscount);
        request.setCurrency("USD");
        request.setExpiryDate(EXPIRY_DATE);

        Coupon savedCoupon = Coupon.builder()

                .code("HUGE")
                .discount(new Money(largeDiscount, "USD"))
                .expiryDate(EXPIRY_DATE)
                .active(true)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", COUPON_ID);

        when(couponRepository.findByCode("HUGE")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        PromotionDto.Response response = promotionService.createCoupon(request);

        // Assert
        assertThat(response.getDiscountAmount()).isEqualTo(largeDiscount);

        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("should successfully create coupon with zero discount")
    void testCreateCoupon_ZeroDiscount() {
        // Arrange
        BigDecimal zeroDiscount = BigDecimal.ZERO;
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode("ZERO");
        request.setDiscountAmount(zeroDiscount);
        request.setCurrency("USD");
        request.setExpiryDate(EXPIRY_DATE);

        Coupon savedCoupon = Coupon.builder()

                .code("ZERO")
                .discount(new Money(zeroDiscount, "USD"))
                .expiryDate(EXPIRY_DATE)
                .active(true)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", COUPON_ID);

        when(couponRepository.findByCode("ZERO")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        PromotionDto.Response response = promotionService.createCoupon(request);

        // Assert
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("should successfully create coupon with today as expiry date")
    void testCreateCoupon_TodayExpiry() {
        // Arrange

        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode("TODAY");
        request.setDiscountAmount(DISCOUNT_AMOUNT);
        request.setCurrency("USD");
        request.setExpiryDate(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

        Coupon savedCoupon = Coupon.builder()

                .code("TODAY")
                .discount(new Money(DISCOUNT_AMOUNT, "USD"))
                .expiryDate(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant())
                .active(true)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", COUPON_ID);

        when(couponRepository.findByCode("TODAY")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        PromotionDto.Response response = promotionService.createCoupon(request);

        // Assert
        assertThat(response).isNotNull();
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("should successfully create coupon with future expiry date")
    void testCreateCoupon_FutureExpiry() {
        // Arrange
        Instant futureDate = LocalDate.now().plusDays(30).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode("FUTURE");
        request.setDiscountAmount(DISCOUNT_AMOUNT);
        request.setCurrency("USD");
        request.setExpiryDate(futureDate);

        Coupon savedCoupon = Coupon.builder()
                .code("FUTURE")
                .discount(new Money(DISCOUNT_AMOUNT, "USD"))
                .expiryDate(futureDate)
                .active(true)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", COUPON_ID);

        when(couponRepository.findByCode("FUTURE")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        PromotionDto.Response response = promotionService.createCoupon(request);

        // Assert
        assertThat(response).isNotNull();
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("should successfully create coupon with different currency")
    void testCreateCoupon_DifferentCurrency() {
        // Arrange
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode("EUR");
        request.setDiscountAmount(new BigDecimal("15.00"));
        request.setCurrency("EUR");
        request.setExpiryDate(EXPIRY_DATE);

        Coupon savedCoupon = Coupon.builder()
                .code("EUR")
                .discount(new Money(new BigDecimal("15.00"), "EUR"))
                .expiryDate(EXPIRY_DATE)
                .active(true)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", COUPON_ID);

        when(couponRepository.findByCode("EUR")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        PromotionDto.Response response = promotionService.createCoupon(request);

        // Assert
        assertThat(response.getDiscountAmount()).isEqualTo(new BigDecimal("15.00"));
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("should correctly map coupon to response DTO")
    void testCreateCoupon_CorrectMapping() {
        // Arrange
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode("MAPPING-TEST");
        request.setDiscountAmount(new BigDecimal("35.50"));
        request.setCurrency("GBP");
        request.setExpiryDate(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Coupon savedCoupon = Coupon.builder()

                .code("MAPPING-TEST")
                .discount(new Money(new BigDecimal("35.50"), "GBP"))
                .expiryDate(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant())
                .active(true)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", COUPON_ID);

        when(couponRepository.findByCode("MAPPING-TEST")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        PromotionDto.Response response = promotionService.createCoupon(request);

        // Assert
        assertThat(response.getId()).isEqualTo(COUPON_ID.toString());
        assertThat(response.getCode()).isEqualTo("MAPPING-TEST");
        assertThat(response.getDiscountAmount()).isEqualTo(new BigDecimal("35.50"));
        assertThat(response.isActive()).isTrue();

        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("should verify repository interactions during coupon creation")
    void testCreateCoupon_VerifyRepositoryInteractions() {
        // Arrange
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode(COUPON_CODE);
        request.setDiscountAmount(DISCOUNT_AMOUNT);
        request.setCurrency(CURRENCY);
        request.setExpiryDate(EXPIRY_DATE);

        Coupon savedCoupon = Coupon.builder()

                .code(COUPON_CODE)
                .discount(new Money(DISCOUNT_AMOUNT, CURRENCY))
                .active(true)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", COUPON_ID);

        when(couponRepository.findByCode(COUPON_CODE)).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        promotionService.createCoupon(request);

        // Assert
        verify(couponRepository).findByCode(COUPON_CODE);
        verify(couponRepository).save(any(Coupon.class));
        verifyNoMoreInteractions(couponRepository);
    }

    @Test
    @DisplayName("should set coupon as active by default")
    void testCreateCoupon_ActiveByDefault() {
        // Arrange
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode("ACTIVE-TEST");
        request.setDiscountAmount(DISCOUNT_AMOUNT);
        request.setCurrency("USD");
        request.setExpiryDate(EXPIRY_DATE);

        Coupon savedCoupon = Coupon.builder()

                .code("ACTIVE-TEST")
                .discount(new Money(DISCOUNT_AMOUNT, "USD"))
                .active(true)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", COUPON_ID);

        when(couponRepository.findByCode("ACTIVE-TEST")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        PromotionDto.Response response = promotionService.createCoupon(request);

        // Assert
        assertThat(response.isActive()).isTrue();

        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("should handle case-sensitive coupon codes")
    void testCreateCoupon_CaseSensitive() {
        // Arrange
        PromotionDto.CreateCouponRequest request1 = new PromotionDto.CreateCouponRequest();
        request1.setCode("SAVE20");
        request1.setDiscountAmount(DISCOUNT_AMOUNT);
        request1.setCurrency("USD");
        request1.setExpiryDate(EXPIRY_DATE);

        PromotionDto.CreateCouponRequest request2 = new PromotionDto.CreateCouponRequest();
        request2.setCode("save20");
        request2.setDiscountAmount(DISCOUNT_AMOUNT);
        request2.setCurrency("USD");
        request2.setExpiryDate(EXPIRY_DATE);

        Coupon coupon1 = Coupon.builder()
                .code("SAVE20")
                .discount(new Money(DISCOUNT_AMOUNT, "USD"))
                .active(true)
                .build();
        ReflectionTestUtils.setField(coupon1, "id", COUPON_ID);

        Coupon coupon2 = Coupon.builder()
                .code("save20")
                .discount(new Money(DISCOUNT_AMOUNT, "USD"))
                .active(true)
                .build();
        ReflectionTestUtils.setField(coupon2, "id", UUID.randomUUID());

        when(couponRepository.findByCode("SAVE20")).thenReturn(Optional.empty());
        when(couponRepository.findByCode("save20")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class)))
                .thenReturn(coupon1)
                .thenReturn(coupon2);

        // Act
        PromotionDto.Response response1 = promotionService.createCoupon(request1);
        PromotionDto.Response response2 = promotionService.createCoupon(request2);

        // Assert
        assertThat(response1.getCode()).isNotEqualTo(response2.getCode());
        verify(couponRepository, times(2)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("should handle numeric coupon codes")
    void testCreateCoupon_NumericCode() {
        // Arrange
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode("12345");
        request.setDiscountAmount(DISCOUNT_AMOUNT);
        request.setCurrency("USD");
        request.setExpiryDate(EXPIRY_DATE);

        Coupon savedCoupon = Coupon.builder()
                .code("12345")
                .discount(new Money(DISCOUNT_AMOUNT, "USD"))
                .active(true)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", COUPON_ID);

        when(couponRepository.findByCode("12345")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        PromotionDto.Response response = promotionService.createCoupon(request);

        // Assert
        assertThat(response.getCode()).isEqualTo("12345");
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("should handle special characters in coupon codes")
    void testCreateCoupon_SpecialCharactersCode() {
        // Arrange
        PromotionDto.CreateCouponRequest request = new PromotionDto.CreateCouponRequest();
        request.setCode("SAVE-20-2024");
        request.setDiscountAmount(DISCOUNT_AMOUNT);
        request.setCurrency("USD");
        request.setExpiryDate(EXPIRY_DATE);

        Coupon savedCoupon = Coupon.builder()

                .code("SAVE-20-2024")
                .discount(new Money(DISCOUNT_AMOUNT, "USD"))
                .active(true)
                .build();
        ReflectionTestUtils.setField(savedCoupon, "id", COUPON_ID);

        when(couponRepository.findByCode("SAVE-20-2024")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // Act
        PromotionDto.Response response = promotionService.createCoupon(request);

        // Assert
        assertThat(response.getCode()).isEqualTo("SAVE-20-2024");
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }
}

