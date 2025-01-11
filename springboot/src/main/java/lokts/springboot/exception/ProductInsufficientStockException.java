package lokts.springboot.exception;

public class ProductInsufficientStockException extends RuntimeException {
    public ProductInsufficientStockException(String message) {
        super(message);
    }

}
