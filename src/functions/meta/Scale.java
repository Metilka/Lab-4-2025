package functions.meta;
import functions.Function;

public class Scale implements Function {
    private static final double EPSILON = 1e-12;

    private final Function f;
    private final double scaleX, scaleY;

    public Scale(Function f, double scaleX, double scaleY) {
        if (f == null) throw new IllegalArgumentException("Function must not be null");
        if (Math.abs(scaleX) < EPSILON) throw new IllegalArgumentException("scaleX must not be 0");
        this.f = f;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }
    @Override
    public double getLeftDomainBorder() {
        double L = f.getLeftDomainBorder();
        double R = f.getRightDomainBorder();
        double a = L * scaleX;
        double b = R * scaleX;
        return Math.min(a, b); // учитываем возможный отрицательный scaleX
    }

    @Override
    public double getRightDomainBorder() {
        double L = f.getLeftDomainBorder();
        double R = f.getRightDomainBorder();
        double a = L * scaleX;
        double b = R * scaleX;
        return Math.max(a, b);
    }
    @Override // Масштабирование графика
    public double getFunctionValue(double x) {
        return scaleY * f.getFunctionValue(x / scaleX);}
}
