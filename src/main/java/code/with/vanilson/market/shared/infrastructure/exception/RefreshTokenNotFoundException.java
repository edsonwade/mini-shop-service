/**
 * Author: vanilson muhongo
 * Date:16/02/2026
 * Time:18:35
 * Version:1
 */

package code.with.vanilson.market.shared.infrastructure.exception;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException(String message) {
        super(message);
    }
}
