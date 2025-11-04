package functions;

// Появляется при попытке добавить или изменить точку функции некорректным образом
// (например, x выходит из интервала между соседями или дублируется x).
public class InappropriateFunctionPointException extends Exception {
    public InappropriateFunctionPointException() {
        super();
    }

    public InappropriateFunctionPointException(String message) {
        super(message);
    }
}
