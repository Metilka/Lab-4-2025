package functions.basic;
import functions.Function;
public class Log implements Function {
    private static final double EPSILON = 1e-12;
    private final double base;
    public Log(double base) {
        if (base <= 0.0 || Math.abs(base - 1.0) < EPSILON) {
            throw new IllegalArgumentException("Log base must be > 0 and != 1");
        }
        this.base = base;
    }
    @Override
    public double getLeftDomainBorder() {return Double.MIN_VALUE;}
    @Override
    public double getRightDomainBorder() {return Double.POSITIVE_INFINITY;}
    @Override
    public double getFunctionValue(double x) {
        if (x <= 0.0) return Double.NaN;    // вне области определения
        return Math.log(x) / Math.log(base);
    }
}