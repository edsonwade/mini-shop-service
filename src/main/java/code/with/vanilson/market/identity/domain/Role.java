package code.with.vanilson.market.identity.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static code.with.vanilson.market.identity.domain.Permission.*;

@RequiredArgsConstructor
@Getter
public enum Role {
    CUSTOMER(Set.of(PLACE_ORDER, CANCEL_ORDER, APPLY_COUPON)),
    OPERATOR(Set.of(PLACE_ORDER, CANCEL_ORDER, SETTLE_ORDER, MANAGE_PRODUCTS)),
    ADMIN(Set.of(PLACE_ORDER, CANCEL_ORDER, SETTLE_ORDER, APPLY_COUPON, MANAGE_PRODUCTS, MANAGE_PROMOTIONS,
            VIEW_ANALYTICS, MANAGE_USERS));

    private final Set<Permission> permissions;
}
