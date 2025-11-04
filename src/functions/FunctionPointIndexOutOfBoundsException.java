package functions;

// Появляется при попытке обратиться к несуществующему индексу
public class FunctionPointIndexOutOfBoundsException extends IndexOutOfBoundsException {
    public FunctionPointIndexOutOfBoundsException() {
        super();
    }

    public FunctionPointIndexOutOfBoundsException(String s) {
        super(s);
    }
}
