/**
 * Author: vanilson muhongo
 * Date:16/02/2026
 * Time:16:36
 * Version:1
 */

package code.with.vanilson.market.shared.infrastructure.exception;

public class CustomerAlreadyExistsException extends RuntimeException {
    public CustomerAlreadyExistsException(String message) {
        super(message);
    }
}
