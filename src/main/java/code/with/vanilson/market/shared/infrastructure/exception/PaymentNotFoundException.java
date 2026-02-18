/**
 * Author: vanilson muhongo
 * Date:16/02/2026
 * Time:16:37
 * Version:1
 */

package code.with.vanilson.market.shared.infrastructure.exception;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
