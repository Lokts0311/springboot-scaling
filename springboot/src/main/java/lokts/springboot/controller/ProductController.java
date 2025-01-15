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

        ProductDTO createdProductDTO = productService.createProduct(productDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdProductDTO);
    }

    @PutMapping
    public ResponseEntity<ProductDTO> updateProduct(@RequestBody ProductDTO productDTO) {

        ProductDTO updatedProductDTO = productService.updateProduct(productDTO);

        return ResponseEntity.status(HttpStatus.OK).body(updatedProductDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {

        ProductDTO productDTO = productService.getProduct(id);

        return ResponseEntity.status(HttpStatus.OK).body(productDTO);
    }

}
