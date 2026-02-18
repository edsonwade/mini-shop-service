/**
 * Author: vanilson muhongo
 * Date:16/02/2026
 * Time:16:27
 * Version:1
 */

package code.with.vanilson.market.shared.infrastructure.exception;

/**
 * Thrown when an operation involves mismatched currencies.
 */
public class CurrencyMismatchException extends RuntimeException {

    public CurrencyMismatchException(String message) {
        super(message);
    }
}