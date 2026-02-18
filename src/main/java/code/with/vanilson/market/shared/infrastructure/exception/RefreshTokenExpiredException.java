/**
 * Author: vanilson muhongo
 * Date:16/02/2026
 * Time:18:36
 * Version:1
 */

package code.with.vanilson.market.shared.infrastructure.exception;

public class RefreshTokenExpiredException extends RuntimeException{
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
