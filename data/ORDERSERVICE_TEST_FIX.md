# ✅ OrderServiceTest Fix - totalAmount Issue

## Problem
The test `testPlaceOrder_MultipleProducts()` was failing with:
```
Cannot invoke "code.with.vanilson.market.shared.domain.Money.getAmount()" 
because the return value of "code.with.vanilson.market.orders.domain.Order.getTotalAmount()" is null
```

## Root Cause
The `OrderService.placeOrder()` method calls `order.calculateTotal()` which calculates and sets `totalAmount`. However, the mocked `savedOrder` didn't have `totalAmount` set, so when the service tried to access it for the event publisher, it was null.

## Solution
Set `totalAmount` on the mocked order to reflect what the real service would calculate:

### Before (❌ Failing):
```java
Order savedOrder = Order.builder()
    .tenantId(TENANT_ID)
    .customerId(CUSTOMER_ID)
    .status(OrderStatus.PLACED)
    .createdAt(Instant.now())
    .build();  // ❌ totalAmount is null
```

### After (✅ Working):
```java
// Total: 50.00 * 2 = 100.00
Order savedOrder = Order.builder()
    .tenantId(TENANT_ID)
    .customerId(CUSTOMER_ID)
    .status(OrderStatus.PLACED)
    .createdAt(Instant.now())
    .totalAmount(new Money(new BigDecimal("100.00"), "USD"))  // ✅ totalAmount set
    .build();
ReflectionTestUtils.setField(savedOrder, "id", ORDER_ID);
```

## Tests Fixed
✅ testPlaceOrder_Success - Total: 50.00 * 2 = 100.00
✅ testPlaceOrder_MultipleProducts - Total: (50.00 * 2) + (75.00 * 3) = 325.00
✅ testPlaceOrder_WithValidCoupon - Total: 100.00 - 20.00 = 80.00
✅ testPlaceOrder_WithoutCoupon - Total: 50.00 * 1 = 50.00

## Key Learning
When mocking entities that have business logic calculations (like Order.calculateTotal()), ensure the mock reflects the expected state after that business logic runs. The test should verify the service's contract - not just the direct return values, but also the side effects and subsequent operations.

## Coupon expiryDate Note
⚠️ Important: `Coupon.expiryDate` is `Instant`, NOT `LocalDate`
- ✅ Correct: `.expiryDate(Instant.now().plusSeconds(86400))`
- ❌ Wrong: `.expiryDate(LocalDate.now().plusDays(30))`

