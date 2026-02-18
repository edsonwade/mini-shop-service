package code.with.vanilson.market.shared.infrastructure;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        String idempotencyKey = request.getHeader("Idempotency-Key");

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String method = request.getMethod();
            String path = request.getRequestURI();
            String key = "idempotency:" + idempotencyKey;

            // Simple check: if key exists, return conflict or cached response
            // For this scaffold, we'll return 409 Conflict if processing/processed
            // A full implementation would cache the response and return it.

            Boolean set = redisTemplate.opsForValue().setIfAbsent(key, "PROCESSING", Duration.ofMinutes(5));

            if (Boolean.FALSE.equals(set)) {
                response.setStatus(409);
                response.getWriter().write("{\"error\": \"Duplicate request detected\"}");
                return;
            }

            try {
                filterChain.doFilter(request, response);
                // Mark as processed or store response
                redisTemplate.opsForValue().set(key, "PROCESSED", Duration.ofHours(24));
            } catch (Exception e) {
                redisTemplate.delete(key); // Release lock on failure
                throw e;
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
