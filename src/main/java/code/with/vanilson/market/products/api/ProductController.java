package code.with.vanilson.market.products.api;

import code.with.vanilson.market.products.application.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Catalog", description = "Endpoints for managing products and inventory")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Register new product", description = "Adds a new product to the catalog for the current tenant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product data or SKU already exists")
    })
    @PostMapping
    public ResponseEntity<ProductDto.Response> createProduct(@RequestBody @Valid ProductDto.CreateRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @Operation(summary = "Get product by ID", description = "Retrieves details of a specific product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.Response> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @Operation(summary = "Get all products", description = "Returns a list of all products in the catalog.")
    @ApiResponse(responseCode = "200", description = "List of products retrieved successfully")
    @GetMapping
    public ResponseEntity<List<ProductDto.Response>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
}
