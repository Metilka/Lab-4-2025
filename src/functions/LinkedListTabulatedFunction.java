package functions;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/*
 * Исключения:
 *  IllegalArgumentException: некорректные параметры конструкторов
 *  FunctionPointIndexOutOfBoundsException: неверный индекс точки
 *  InappropriateFunctionPointException: нарушен порядок по x или повтор x
 *  IllegalStateException: попытка удалить точку при количестве < 3
 */
public class LinkedListTabulatedFunction implements TabulatedFunction, Externalizable {
    private static final long serialVersionUID = 1L;

    //  Внутренний элемент двусвязного циклического списка
    private static class FunctionNode {
        FunctionPoint point;
        FunctionNode prev;
        FunctionNode next;

        FunctionNode() {}
        FunctionNode(FunctionPoint p) { this.point = p; }
    }

    // Голова списка
    private transient FunctionNode head;

    // Текущее количество точек
    private int size;


    private static final double EPSILON = 1e-9;
    private static boolean eq(double a, double b) { return Math.abs(a - b) <= EPSILON; }
    private static boolean lt(double a, double b) { return a <  b - EPSILON; }
    private static boolean gt(double a, double b) { return a >  b + EPSILON; }
    private static boolean le(double a, double b) { return a <= b + EPSILON; }
    private static boolean ge(double a, double b) { return a >= b - EPSILON; }


    // Создаёт пустой список (head->head)
    public LinkedListTabulatedFunction() {
        initEmpty();
    }

    // Конструктор из массива точек
    public LinkedListTabulatedFunction(FunctionPoint[] arr) {
        this();
        if (arr == null || arr.length < 2) {
            throw new IllegalArgumentException("At least 2 points required");
        }
        // Строгий порядок по x
        for (int i = 1; i < arr.length; ++i) {
            if (!lt(arr[i - 1].getX(), arr[i].getX()))
                throw new IllegalArgumentException("Points must be strictly increasing by x");
        }
        // Добавляем копии точек
        for (FunctionPoint p : arr) {
            try {
                addPoint(new FunctionPoint(p));
            } catch (InappropriateFunctionPointException e) {
                throw new IllegalArgumentException("Invalid points order or duplicate x near " + p.getX(), e);
            }
        }
    }

    // Равномерная разметка, все y=0
    public LinkedListTabulatedFunction(double leftX, double rightX, int pointsCount) {
        this();
        if (rightX <= leftX) throw new IllegalArgumentException("leftX must be < rightX");
        if (pointsCount < 2) throw new IllegalArgumentException("pointsCount must be >= 2");
        double step = (rightX - leftX) / (pointsCount - 1);
        for (int i = 0; i < pointsCount; ++i) {
            addNodeToTail(new FunctionPoint(leftX + i * step, 0.0));
        }
    }

    // Равномерная разметка, y берутся из массива
    public LinkedListTabulatedFunction(double leftX, double rightX, double[] values) {
        this();
        if (rightX <= leftX) throw new IllegalArgumentException("leftX must be < rightX");
        if (values == null || values.length < 2) throw new IllegalArgumentException("values length must be >= 2");
        double step = (rightX - leftX) / (values.length - 1);
        for (int i = 0; i < values.length; i++) {
            addNodeToTail(new FunctionPoint(leftX + i * step, values[i]));
        }
    }



    private void initEmpty() {
        head = new FunctionNode();
        head.next = head;
        head.prev = head;
        size = 0;
    }

    private void clear() {
        // Обнуляем в один шаг
        initEmpty();
    }

    private FunctionNode getNodeByIndex(int index) {
        if (index < 0 || index >= size) {
            throw new FunctionPointIndexOutOfBoundsException("index=" + index);
        }
        if (index < size / 2) {
            FunctionNode cur = head.next;
            for (int i = 0; i < index; i++) cur = cur.next;
            return cur;
        } else {
            FunctionNode cur = head.prev;
            for (int i = size - 1; i > index; i--) cur = cur.prev;
            return cur;
        }
    }

    private FunctionNode addNodeToTail(FunctionPoint p) {
        FunctionNode last = head.prev;
        FunctionNode node = new FunctionNode(new FunctionPoint(p)); // копия
        node.prev = last;
        node.next = head;
        last.next = node;
        head.prev = node;
        size++;
        return node;
    }

    private FunctionNode addNodeByIndex(int index, FunctionPoint p) {
        if (index < 0 || index > size) {
            throw new FunctionPointIndexOutOfBoundsException("index=" + index);
        }
        if (index == size) {
            return addNodeToTail(p);
        }
        FunctionNode at = getNodeByIndex(index);
        FunctionNode before = at.prev;
        FunctionNode node = new FunctionNode(new FunctionPoint(p)); // копия
        node.prev = before;
        node.next = at;
        before.next = node;
        at.prev = node;
        size++;
        return node;
    }

    private FunctionNode deleteNodeByIndex(int index) {
        FunctionNode at = getNodeByIndex(index);
        at.prev.next = at.next;
        at.next.prev = at.prev;
        size--;
        at.next = null;
        at.prev = null;
        return at;
    }


    @Override public int getPointsCount() { return size; }

    @Override public double getLeftDomainBorder()  { return (size == 0) ? Double.NaN : head.next.point.getX(); }

    @Override public double getRightDomainBorder() { return (size == 0) ? Double.NaN : head.prev.point.getX(); }

    @Override
    public double getFunctionValue(double x) {
        if (size == 0) return Double.NaN;
        double left = head.next.point.getX();
        double right = head.prev.point.getX();
        if (lt(x, left) || gt(x, right)) return Double.NaN;

        // точное попадание
        FunctionNode cur = head.next;
        for (int i = 0; i < size; i++) {
            if (eq(x, cur.point.getX())) return cur.point.getY();
            cur = cur.next;
        }

        // линейная интерполяция на сегменте [x0, x1]
        cur = head.next;
        for (int i = 0; i < size - 1; i++) {
            double x0 = cur.point.getX(), y0 = cur.point.getY();
            double x1 = cur.next.point.getX(), y1 = cur.next.point.getY();
            if (ge(x, x0) && le(x, x1)) {
                return y0 + (y1 - y0) * (x - x0) / (x1 - x0);
            }
            cur = cur.next;
        }
        return Double.NaN;
    }

    @Override
    public FunctionPoint getPoint(int index) {
        FunctionNode n = getNodeByIndex(index);
        return new FunctionPoint(n.point); // копия
    }

    @Override
    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        if (point == null) return;
        FunctionNode node = getNodeByIndex(index);
        double newX = point.getX();

        if (index > 0) {
            double leftX = node.prev.point.getX();
            if (!gt(newX, leftX)) throw new InappropriateFunctionPointException("x must be > left neighbor");
        }
        if (index < size - 1) {
            double rightX = node.next.point.getX();
            if (!lt(newX, rightX)) throw new InappropriateFunctionPointException("x must be < right neighbor");
        }
        node.point = new FunctionPoint(point); // копия
    }

    @Override public double getPointX(int index) { return getNodeByIndex(index).point.getX(); }

    @Override
    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        FunctionNode node = getNodeByIndex(index);
        if (size == 1) { node.point.setX(x); return; }

        if (index == 0) {
            double nextX = node.next.point.getX();
            if (!lt(x, nextX)) throw new InappropriateFunctionPointException("x must be < right neighbor");
            node.point.setX(x);
            return;
        }
        if (index == size - 1) {
            double prevX = node.prev.point.getX();
            if (!gt(x, prevX)) throw new InappropriateFunctionPointException("x must be > left neighbor");
            node.point.setX(x);
            return;
        }
        double leftX = node.prev.point.getX();
        double rightX = node.next.point.getX();
        if (!(gt(x, leftX) && lt(x, rightX)))
            throw new InappropriateFunctionPointException("x must be strictly between neighbors");
        node.point.setX(x);
    }

    @Override public double getPointY(int index) { return getNodeByIndex(index).point.getY(); }

    @Override public void setPointY(int index, double y) { getNodeByIndex(index).point.setY(y); }

    @Override
    public void deletePoint(int index) {
        if (size < 3) throw new IllegalStateException("cannot delete when points count < 3");
        deleteNodeByIndex(index);
    }

    @Override
    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        if (point == null) return;
        double x = point.getX();

        int insertIndex = 0;
        FunctionNode cur = head.next;
        while (insertIndex < size && lt(cur.point.getX(), x)) {
            cur = cur.next;
            insertIndex++;
        }
        if (insertIndex < size && eq(cur.point.getX(), x))
            throw new InappropriateFunctionPointException("duplicate x");

        addNodeByIndex(insertIndex, point); // копия внутри
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            FunctionPoint p = getPoint(i);   // копия — ок
            out.writeDouble(p.getX());
            out.writeDouble(p.getY());
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        clear();                    // инициализируем head, size=0
        int n = in.readInt();
        for (int i = 0; i < n; i++) {
            double x = in.readDouble();
            double y = in.readDouble();
            // не бросает checked-исключений
            addNodeToTail(new FunctionPoint(x, y));
        }
    }

}
