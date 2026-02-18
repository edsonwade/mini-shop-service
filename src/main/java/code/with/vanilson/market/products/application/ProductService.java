package code.with.vanilson.market.products.application;

import code.with.vanilson.market.products.api.ProductDto;
import code.with.vanilson.market.products.domain.Product;
import code.with.vanilson.market.products.domain.ProductRepository;
import code.with.vanilson.market.shared.domain.DomainException;
import code.with.vanilson.market.shared.domain.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductDto.Response createProduct(ProductDto.CreateRequest request) {
        if (productRepository.findBySku(request.getSku()).isPresent()) {
            throw new DomainException("Product with this SKU already exists");
        }

        Product product = Product.builder()
                .tenantId(request.getTenantId())
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .price(new Money(request.getPrice(), request.getCurrency()))
                .inventoryCount(request.getInventoryCount())
                .build();

        product = productRepository.save(product);
        return mapToResponse(product);
    }

    public ProductDto.Response getProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new DomainException("Product not found"));
        return mapToResponse(product);
    }

    public List<ProductDto.Response> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProductDto.Response mapToResponse(Product product) {
        ProductDto.Response response = new ProductDto.Response();
        response.setId(product.getId().toString());
        response.setName(product.getName());
        response.setSku(product.getSku());
        response.setPrice(product.getPrice().getAmount());
        response.setCurrency(product.getPrice().getCurrencyCode());
        response.setInventoryCount(product.getInventoryCount());
        return response;
    }
}
