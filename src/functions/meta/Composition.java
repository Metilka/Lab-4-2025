package functions.meta;

import functions.Function;

public class Composition implements Function {
    private final Function outer, inner;

    public Composition(Function outer, Function inner) {
        if (outer == null || inner == null) throw new IllegalArgumentException("Functions must not be null");
        this.outer = outer; this.inner = inner;
    }

    @Override public double getLeftDomainBorder()  { return inner.getLeftDomainBorder(); }
    @Override public double getRightDomainBorder() { return inner.getRightDomainBorder(); }
    @Override public double getFunctionValue(double x) { return outer.getFunctionValue(inner.getFunctionValue(x)); }
}
