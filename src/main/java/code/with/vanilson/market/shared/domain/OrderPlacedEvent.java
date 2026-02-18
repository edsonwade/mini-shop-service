package code.with.vanilson.market.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlacedEvent implements Serializable {
    private UUID orderId;
    private UUID customerId;
    private BigDecimal totalAmount;
    private String currency;
}
