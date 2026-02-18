/**
 * Author: vanilson muhongo
 * Date:16/02/2026
 * Time:18:06
 * Version:1
 */

package code.with.vanilson.market.shared.infrastructure.exception;

public class KycAlreadyVerifiedException extends RuntimeException {

    public KycAlreadyVerifiedException(String message) {
        super(message);
    }
}
