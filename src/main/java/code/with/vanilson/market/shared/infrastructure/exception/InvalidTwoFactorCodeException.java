/**
 * Author: vanilson muhongo
 * Date:16/02/2026
 * Time:18:37
 * Version:1
 */

package code.with.vanilson.market.shared.infrastructure.exception;

public class InvalidTwoFactorCodeException extends RuntimeException{
    public InvalidTwoFactorCodeException(String message) {
        super(message);
    }
}
