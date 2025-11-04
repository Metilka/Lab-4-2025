package functions.basic;

import functions.Function;

public abstract class TrigonometricFunction implements Function {
    @Override
    public final double getLeftDomainBorder() {
        return Double.NEGATIVE_INFINITY;
    }
    @Override
    public final double getRightDomainBorder() {
        return Double.POSITIVE_INFINITY;
    }
    @Override
    public abstract double getFunctionValue(double x);
}
