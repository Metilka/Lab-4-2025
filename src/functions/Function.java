package functions;

public interface Function {
    double getLeftDomainBorder(); // возвращает значение левой границы области определения функции
    double getRightDomainBorder(); // возвращает значение правой границы области определения функции
    double getFunctionValue(double x); // возвращает значение функции в заданной точке
}
