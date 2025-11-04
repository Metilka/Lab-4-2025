package functions.meta;

import functions.Function;

public class Power implements Function {
    private final Function f;
    private final double p;

    public Power(Function f, double p) {
        if (f == null) throw new IllegalArgumentException("Function must not be null");
        this.f = f; this.p = p;
    }

    @Override public double getLeftDomainBorder()  { return f.getLeftDomainBorder(); }
    @Override public double getRightDomainBorder() { return f.getRightDomainBorder(); }
    @Override public double getFunctionValue(double x) { return Math.pow(f.getFunctionValue(x), p); }
}
