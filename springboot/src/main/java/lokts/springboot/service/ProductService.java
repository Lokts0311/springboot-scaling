package lokts.springboot.service;

import lokts.springboot.dto.ProductDTO;
import lokts.springboot.entity.Product;
import lokts.springboot.exception.ProductAlreadyExistsException;
import lokts.springboot.exception.ProductNotExistsException;
import lokts.springboot.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Environment environment;

    public ProductDTO createProduct(ProductDTO productDTO) {

        Optional<Product> existingProductOpt = productRepository.findByName(productDTO.getName());

        if (existingProductOpt.isPresent()) {
            throw new ProductAlreadyExistsException("Product " + productDTO.getName() + " already exists.");
        }

        Product product = new Product().builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .stock(productDTO.getStock())
                .build();
        product = productRepository.save(product);

        productDTO.setId(product.getId());

        return productDTO;
    }

    public String updateProduct(ProductDTO productDTO) {
        Optional<Product> existingProductOpt = productRepository.findById(productDTO.getId());

        if (existingProductOpt.isEmpty()) {
            throw new ProductNotExistsException("Product " + productDTO.getName() + " is not exist.");
        }

        Product existingProduct = existingProductOpt.get();

        if (!existingProduct.getName().equals(productDTO.getName())) {
            productRepository.findByName(productDTO.getName())
                    .orElseThrow(() -> new ProductAlreadyExistsException("Product with name " + productDTO.getName() + " already exists."));
        }

        existingProduct.setName(productDTO.getName());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());
        productRepository.save(existingProduct);

        return new String( "Updated");
    }
    @Transactional
    public String buyProduct(Long id) {
        Optional<Product> existingProductOpt = productRepository.findByIdForUpdate(id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotExistsException("Product Id" + id + " is not exist."));

        if(product.getStock() > 0) {
            product.setStock(product.getStock()-1);
        }
        product = productRepository.save(product);

        return new String( "Bought from Server " + environment.getProperty("local.server.port") + "! Only " + product.getStock() + " Stock Left!");
    }
}
