package functions;
import functions.meta.*;
public final class Functions {
    private Functions() {
        // Запрещаем создание экземпляров
        throw new AssertionError("No instances");
    }
    public static Function shift(Function f, double shiftX, double shiftY) {
        return new Shift(f, shiftX, shiftY);
    }

    public static Function scale(Function f, double scaleX, double scaleY) {
        return new Scale(f, scaleX, scaleY);
    }

    public static Function power(Function f, double power) {
        return new Power(f, power);
    }

    public static Function sum(Function f1, Function f2) {
        return new Sum(f1, f2);
    }

    public static Function mult(Function f1, Function f2) {
        return new Mult(f1, f2);
    }

    public static Function composition(Function outer, Function inner) {
        return new Composition(outer, inner);
    }
}
