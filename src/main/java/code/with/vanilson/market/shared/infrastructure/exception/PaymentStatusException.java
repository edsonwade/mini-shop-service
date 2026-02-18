/**
 * Author: vanilson muhongo
 * Date:16/02/2026
 * Time:16:54
 * Version:1
 */

package code.with.vanilson.market.shared.infrastructure.exception;

public class PaymentStatusException  extends RuntimeException {
    public PaymentStatusException(String message) {
        super(message);
    }
}
