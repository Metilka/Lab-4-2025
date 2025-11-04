package functions;

import java.io.Serializable;

public class FunctionPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;

    public FunctionPoint(double x, double y) { this.x = x; this.y = y; }
    public FunctionPoint(FunctionPoint other) { this(other.x, other.y); }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}
