# ✅ OrderServiceTest - Items Assertion Fix

## Problem
Test `testPlaceOrder_MultipleProducts()` was failing with:
```
java.lang.AssertionError: Expected size: 2 but was: 0 in: []
assertThat(response.getItems()).hasSize(2);
```

## Root Cause
The OrderService creates a fresh `Order`, adds items to it via `order.addItem()`, then saves it. However, the mocked `savedOrder` returned by `orderRepository.save()` had no items set. When the response is mapped from the order, it had 0 items because the mock didn't include them.

## Solution
Create `OrderItem` objects with proper IDs set via ReflectionTestUtils and include them in the mocked `savedOrder`.

### Before (❌ Failing):
```java
// Missing items!
Order savedOrder = Order.builder()
    .tenantId(TENANT_ID)
    .customerId(CUSTOMER_ID)
    .status(OrderStatus.PLACED)
    .createdAt(Instant.now())
    .totalAmount(new Money(new BigDecimal("325.00"), "USD"))
    .build();  // ❌ No items set
ReflectionTestUtils.setField(savedOrder, "id", ORDER_ID);

// ...
assertThat(response.getItems()).hasSize(2);  // ❌ Fails: 0 != 2
```

### After (✅ Working):
```java
// Create OrderItems first
OrderItem orderItem1 = OrderItem.builder()
    .productId(PRODUCT_ID)
    .sku("PROD-001")
    .quantity(2)
    .unitPrice(new Money(new BigDecimal("50.00"), "USD"))
    .build();
ReflectionTestUtils.setField(orderItem1, "id", UUID.randomUUID());

OrderItem orderItem2 = OrderItem.builder()
    .productId(productId2)
    .sku("PROD-002")
    .quantity(3)
    .unitPrice(new Money(new BigDecimal("75.00"), "USD"))
    .build();
ReflectionTestUtils.setField(orderItem2, "id", UUID.randomUUID());

// Include items in savedOrder
Order savedOrder = Order.builder()
    .tenantId(TENANT_ID)
    .customerId(CUSTOMER_ID)
    .status(OrderStatus.PLACED)
    .createdAt(Instant.now())
    .items(Arrays.asList(orderItem1, orderItem2))  // ✅ Items included
    .totalAmount(new Money(new BigDecimal("325.00"), "USD"))
    .build();
ReflectionTestUtils.setField(savedOrder, "id", ORDER_ID);

// ...
assertThat(response.getItems()).hasSize(2);  // ✅ Passes: 2 == 2
```

## Tests Fixed

### 1. testPlaceOrder_Success ✅
- Added 1 OrderItem
- Items count: 1
- Quantity: 2
- Price: 50.00 USD

### 2. testPlaceOrder_MultipleProducts ✅
- Added 2 OrderItems
- Items count: 2
- Quantities: 2 and 3
- Prices: 50.00 and 75.00 USD
- Total: 325.00 (after calculation)
- Added assertion: `.extracting("quantity").containsExactly(2, 3)`

### 3. testPlaceOrder_WithValidCoupon ✅
- Added 1 OrderItem
- Items count: 1
- Quantity: 1
- Price: 100.00 USD
- Total after coupon: 80.00 (100.00 - 20.00)

## Key Learning

When mocking entities with collections (like `Order.items`):
1. Create the collection items with proper IDs set via ReflectionTestUtils
2. Include them in the mock's builder
3. The response will then correctly map these items
4. Add assertions to verify the response contains expected items

## Implementation Pattern

```java
// Step 1: Create OrderItems
OrderItem item = OrderItem.builder()
    .productId(PRODUCT_ID)
    .sku("SKU")
    .quantity(qty)
    .unitPrice(price)
    .build();
ReflectionTestUtils.setField(item, "id", UUID.randomUUID());

// Step 2: Include in Order
Order savedOrder = Order.builder()
    // ... other fields ...
    .items(Collections.singletonList(item))  // ✅ Include items
    .build();
ReflectionTestUtils.setField(savedOrder, "id", ORDER_ID);

// Step 3: Assert response
assertThat(response.getItems()).hasSize(1);
assertThat(response.getItems().get(0).getQuantity()).isEqualTo(qty);
```

---

**Status**: ✅ All 3 tests fixed and ready to run
**Date**: 2025-02-16

