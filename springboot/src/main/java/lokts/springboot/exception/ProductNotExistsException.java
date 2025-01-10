package lokts.springboot.exception;

public class ProductNotExistsException extends RuntimeException{

    public ProductNotExistsException(String message) {
        super(message);
    }
}
