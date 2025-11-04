package functions;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
public final class TabulatedFunctions {
    private TabulatedFunctions() {
        // Запрещаем создание экземпляров
        throw new AssertionError("No instances");
    }

    // Сравнения с эпсилоном
    private static final double EPSILON = 1e-9;
    private static boolean le(double a, double b){ return a <= b + EPSILON; }
    private static boolean ge(double a, double b){ return a >= b - EPSILON; }

    // Табуляция функции на отрезке
    public static TabulatedFunction tabulate(Function function, double leftX, double rightX, int pointsCount) {
        if (function == null) {throw new IllegalArgumentException("function is null");}
        if (pointsCount < 2) {throw new IllegalArgumentException("pointsCount must be >= 2");}
        if (!(leftX < rightX)) {throw new IllegalArgumentException("leftX must be < rightX");}
        // Проверяем, что отрезок внутри области определения функции
        if (!ge(leftX, function.getLeftDomainBorder()) ||
            !le(rightX, function.getRightDomainBorder())) {
            throw new IllegalArgumentException("Tabulation segment lies outside function domain");}

        double step = (rightX - leftX) / (pointsCount - 1);
        FunctionPoint[] pts = new FunctionPoint[pointsCount];
        for (int i = 0; i < pointsCount; ++i) {
            double x = leftX + i * step;
            double y = function.getFunctionValue(x);
            pts[i] = new FunctionPoint(x, y);}
        return new ArrayTabulatedFunction(pts);}

     // Бинарный вывод пишет N, затем пары (x, y) для всех точек
    public static void outputTabulatedFunction(TabulatedFunction function, OutputStream out) {
        try {
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(out));
            int n = function.getPointsCount(); // Количество точек в функции
            dos.writeInt(n); // Сначала пишем это количество
            for (int i = 0; i < n; ++i) {
                // Для каждой точки записываем x и y как double
                dos.writeDouble(function.getPointX(i));
                dos.writeDouble(function.getPointY(i));
            }
            dos.flush(); // Принудительно записываем данные в поток
        } catch (IOException e) { // Если произошла ошибка
            throw new UncheckedIOException(e);
        }
    }


     // Бинарный ввод читает N, затем N пар (x, y), собирает TabulatedFunction
    public static TabulatedFunction inputTabulatedFunction(InputStream in) {
        try {
            // Оборачиваем поток для удобного чтения
            DataInputStream dis = new DataInputStream(new BufferedInputStream(in));
            int n = dis.readInt();
            FunctionPoint[] pts = new FunctionPoint[n];
            for (int i = 0; i < n; ++i) {
                double x = dis.readDouble();
                double y = dis.readDouble();
                pts[i] = new FunctionPoint(x, y);
            }
             // Возвращаем новую табулированную функцию на основе прочитанного массива точек
            return new ArrayTabulatedFunction(pts);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Запись табулированной функции в символьный поток
    public static void writeTabulatedFunction(TabulatedFunction function, Writer out) {
        PrintWriter pw = new PrintWriter(new BufferedWriter(out));
        int n = function.getPointsCount(); // Сначала выводим количество точек
        pw.print(n);
        for (int i = 0; i < n; ++i) {
            // Затем для каждой точки: x и y через пробел
            pw.print(' ');
            pw.print(function.getPointX(i));
            pw.print(' ');
            pw.print(function.getPointY(i));
        }
        pw.println(); // Завершаем строку
        pw.flush(); // Выгружаем данные в поток
    }

    // Ввод табулированной функции из символьного потока
    public static TabulatedFunction readTabulatedFunction(Reader in) {
        try {
            StreamTokenizer st = new StreamTokenizer(in);
            st.parseNumbers(); // включаем поддержку чисел
            int t = st.nextToken();
            if (t != StreamTokenizer.TT_NUMBER) {throw new IOException("Expected points count");}
            int n = (int) st.nval; // Считываем количество точек
            List<FunctionPoint> list = new ArrayList<>(n);
            for (int i = 0; i < n; ++i) {
                if (st.nextToken() != StreamTokenizer.TT_NUMBER) { throw new IOException("Expected x");}
                double x = st.nval;
                if (st.nextToken() != StreamTokenizer.TT_NUMBER) {throw new IOException("Expected y");}
                double y = st.nval;
                list.add(new FunctionPoint(x, y));}
                // Собираем функцию из считанных точек
            return new ArrayTabulatedFunction(list.toArray(new FunctionPoint[0]));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
