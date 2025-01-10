package lokts.springboot.controller;

import lokts.springboot.dto.ProductDTO;
import lokts.springboot.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {

        ProductDTO createProduct = productService.createProduct(productDTO);

        // ProductDTO createdProduct = productService.createProduct(productDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(productDTO);
    }

    @PutMapping
    public ResponseEntity<String> updateProduct(@RequestBody ProductDTO productDTO) {

        String updatedProduct = productService.updateProduct(productDTO);

        return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> buyProduct(@PathVariable Long id) {
        String buyProduct = productService.buyProduct(id);

        return ResponseEntity.status(HttpStatus.OK).body(buyProduct);
    }

}
