import functions.*;
import functions.basic.*;
import static functions.Functions.*; // sum, mult, power, scale, shift, composition

import java.io.*;

public class Main {
    private static final double EPSILON = 1e-9;

    public static void main(String[] args) {
        // 1 Аналитика sin/cos
        System.out.println("=== 1) Аналитические sin и cos на [0, pi] шаг 0.1 ===");
        Function sin = new Sin();
        Function cos = new Cos();
        double L = 0.0, R = Math.PI, step = 0.1;
        System.out.printf("sin domain = [%.3g, %.3g]%n", sin.getLeftDomainBorder(), sin.getRightDomainBorder());
        System.out.printf("cos domain = [%.3g, %.3g]%n", cos.getLeftDomainBorder(), cos.getRightDomainBorder());
        for (double x = L; x <= R + 1e-12; x += step) {
            System.out.printf("x=%4.1f  sin=%.6f  cos=%.6f%n",
                    x, sin.getFunctionValue(x), cos.getFunctionValue(x));
        }

        // 2 Табуляция sin/cos, сравнение с аналитикой. Интерполяция и погрешность.
        System.out.println("\n=== 2) Табулированные sin/cos vs аналитические точки ===");
        TabulatedFunction tSin = TabulatedFunctions.tabulate(sin, L, R, 10);
        TabulatedFunction tCos = TabulatedFunctions.tabulate(cos, L, R, 10);
        double maxErrSin = 0.0, maxErrCos = 0.0;
        for (double x = L; x <= R + 1e-12; x += step) {
            double asin = sin.getFunctionValue(x), ts = tSin.getFunctionValue(x);
            double acos = cos.getFunctionValue(x), tc = tCos.getFunctionValue(x);
            maxErrSin = Math.max(maxErrSin, Math.abs(asin - ts));
            maxErrCos = Math.max(maxErrCos, Math.abs(acos - tc));
            System.out.printf("x=%4.1f  tsin=%.6f  sin=%.6f   tcos=%.6f  cos=%.6f%n", x, ts, asin, tc, acos);
        }
        System.out.printf("max|sin - tsin| = %.6g, max|cos - tcos| = %.6g%n", maxErrSin, maxErrCos);

        // 3 Анализ: (tsin)^2 + (tcos)^2 и влияние количества точек на точность
        System.out.println("\n=== 3) (tsin)^2 + (tcos)^2 и влияние количества  точек на точность вычислений ===");
        int[] counts = {5, 10, 25, 50, 75, 100};
        for (int n : counts) {
            TabulatedFunction tsin = TabulatedFunctions.tabulate(sin, L, R, n);
            TabulatedFunction tcos = TabulatedFunctions.tabulate(cos, L, R, n);
            Function f = sum(power(tsin, 2), power(tcos, 2));
            double maxDev = 0;
            for (double x = L; x <= R + 1e-12; x += 0.01) {
                maxDev = Math.max(maxDev, Math.abs(f.getFunctionValue(x) - 1.0));
            }
            System.out.printf("n=%2d  max|sin^2+cos^2 - 1| = %.6g%n", n, maxDev);
        }

        // 4 Пример работы функций-обёрток Sum, Mult, Power, Scale, Shift, Composition
        System.out.println("\n=== 4) Meta-функции Sum, Mult, Power, Scale, Shift, Composition ===");
        Function s = sum(sin, cos);
        Function m = mult(sin, cos);
        Function p = power(sin, 2);
        Function sc = scale(sin, -2.0, 3.0);        // 3 * sin(-2x)
        Function sh = shift(cos, 1.0, -0.5);        // cos(x - 1) - 0.5
        Function comp = composition(new Log(Math.E), new Exp()); // ln(exp(x))
        System.out.printf("sum domain        = [%.3g, %.3g]%n", s.getLeftDomainBorder(),  s.getRightDomainBorder());
        System.out.printf("mult domain       = [%.3g, %.3g]%n", m.getLeftDomainBorder(), m.getRightDomainBorder());
        System.out.printf("power(sin,2) dom  = [%.3g, %.3g]%n", p.getLeftDomainBorder(),  p.getRightDomainBorder());
        System.out.printf("scale dom         = [%.3g, %.3g]%n", sc.getLeftDomainBorder(), sc.getRightDomainBorder());
        System.out.printf("shift dom         = [%.3g, %.3g]%n", sh.getLeftDomainBorder(), sh.getRightDomainBorder());
        System.out.printf("composition dom   = [%.3g, %.3g]%n", comp.getLeftDomainBorder(), comp.getRightDomainBorder());
        double[] xs = {-Math.PI, -1, -0.5, 0, 0.5, 1, Math.PI};
        for (double x : xs) {
            System.out.printf("x=%6.3f  sum=%.6f  mult=%.6f  sin^2=%.6f  scale=%.6f  shift=%.6f  comp=%.6f%n",
                    x, s.getFunctionValue(x), m.getFunctionValue(x), p.getFunctionValue(x),
                    sc.getFunctionValue(x), sh.getFunctionValue(x), comp.getFunctionValue(x));
        }

        // 5 Проверка работы метода tabulate на границах области определения (ожидаются исключения)
        System.out.println("\n=== 5) Tabulate: проверки границ области определения ===");
        try {
            TabulatedFunctions.tabulate(new Log(Math.E), 0.0, 10.0, 11); // ln на 0 недопустим
            System.out.println("Ошибка: ожидалось исключение для ln на [0,10]");
        } catch (IllegalArgumentException ok) {
            System.out.println("OK: ln на [0,10] отклонён как должен");
        }
        try {
            TabulatedFunctions.tabulate(sin, 1.0, 1.0, 5); // левая граница = правой
            System.out.println("Ошибка: ожидалось исключение для degenerate отрезка");
        } catch (IllegalArgumentException ok) {
            System.out.println("OK: degenerate отрезок отклонён");
        }
        try {
            TabulatedFunctions.tabulate(sin, 0.0, Math.PI, 1); // меньше 2 точек
            System.out.println("Ошибка: ожидалось исключение для pointsCount < 2");
        } catch (IllegalArgumentException ok) {
            System.out.println("OK: pointsCount<2 отклонён");
        }

        // 6 Текстовый ввод-вывод, запись значений exp(x) в текстовый файл и чтение обратно
        System.out.println("\n=== 6) Text IO: write/read exp на [0,10] с шагом 1 ===");
        Function exp = new Exp();
        TabulatedFunction tExp = TabulatedFunctions.tabulate(exp, 0.0, 10.0, 11);
        TabulatedFunction tExpRead;
        // Записываем табулированную функцию exp(x) в текстовый файл (exp.txt), строка вида: n x1 y1 x2 y2
        try (Writer w = new FileWriter("exp.txt")) {
            TabulatedFunctions.writeTabulatedFunction(tExp, w);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи exp.txt", e);
        }
         // Читаем табулированную функцию из текстового файла (exp.txt), разбираем строку с помощью StreamTokenizer
        try (Reader r = new FileReader("exp.txt")) {
            tExpRead = TabulatedFunctions.readTabulatedFunction(r);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения exp.txt", e);
        }
        // Сравниваем значения исходной функции и прочитанной из файла для x = 0, 1, ..., 10
        for (double x = 0; x <= 10 + 1e-12; x += 1.0) {
            double a = tExp.getFunctionValue(x);
            double b = tExpRead.getFunctionValue(x);
            System.out.printf("x=%2.0f  write=%.9f  read=%.9f  diff=%.3g%n", x, a, b, Math.abs(a - b));
        }

        // 7 Бинарный ввод-вывод, сохранение и загрузка табулированного ln(x) в бинарном файле
        System.out.println("\n=== 7) Binary IO: output/input ln на [1e-4,10] с шагом 1 ===");
        Function ln = new Log(Math.E);
        TabulatedFunction tLn = TabulatedFunctions.tabulate(ln, 1e-4, 10.0, 11);
        TabulatedFunction tLnRead;
        // Записываем табулированную функцию ln(x) в бинарный файл
        try (OutputStream os = new FileOutputStream("log.bin")) {
            TabulatedFunctions.outputTabulatedFunction(tLn, os);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи log.bin", e);
        }
        // Считываем табулированную функцию из бинарного файла
        try (InputStream is = new FileInputStream("log.bin")) {
            tLnRead = TabulatedFunctions.inputTabulatedFunction(is);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения log.bin", e);
        }
        // Сравниваем значения исходной функции и прочитанной из файла
        for (double x = 1; x <= 10 + 1e-12; x += 1.0) {
            double a = tLn.getFunctionValue(x);
            double b = tLnRead.getFunctionValue(x);
            System.out.printf("x=%2.0f  write=%.9f  read=%.9f  diff=%.3g%n", x, a, b, Math.abs(a - b));
        }

        // 8 Сериализация: сохранение и восстановление объектов через Serializable и Externalizable
        System.out.println("\n=== 8) Сериализация: Serializable и Externalizable ===");
        // 8.1 Сериализация через Serializable, сохраняем ArrayTabulatedFunction в файл и читаем обратно
        TabulatedFunction arrF = TabulatedFunctions.tabulate(composition(new Log(Math.E), new Exp()), 0.0, 10.0, 11);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("arr_ser.bin"))) {
            oos.writeObject(arrF);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сериализации arr_ser.bin", e);
        }
        TabulatedFunction arrRead;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("arr_ser.bin"))) {
            arrRead = (TabulatedFunction) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Ошибка десериализации arr_ser.bin", e);
        }
        double maxDiffSer = 0.0;
        for (double x = 0; x <= 10 + EPSILON; x += 1.0) {
            maxDiffSer = Math.max(maxDiffSer, Math.abs(arrF.getFunctionValue(x) - arrRead.getFunctionValue(x)));
        }
        System.out.printf("Serializable check, max diff = %.3g%n", maxDiffSer);

        // 8.2 Сериализация через Externalizable: сохраняем и восстанавливаем LinkedListTabulatedFunction
        LinkedListTabulatedFunction listF = new LinkedListTabulatedFunction(0.0, 10.0, 11);
        for (int i = 0; i < listF.getPointsCount(); i++) {
            double x = listF.getPointX(i);
            listF.setPointY(i, Math.log(Math.exp(x))); // здесь y = x
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("list_ext.bin"))) {
            oos.writeObject(listF);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сериализации list_ext.bin", e);
        }
        LinkedListTabulatedFunction listRead;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("list_ext.bin"))) {
            listRead = (LinkedListTabulatedFunction) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Ошибка десериализации list_ext.bin", e);
        }
        double maxDiffExt = 0.0;
        for (double x = 0; x <= 10 + EPSILON; x += 1.0) {
            maxDiffExt = Math.max(maxDiffExt, Math.abs(listF.getFunctionValue(x) - listRead.getFunctionValue(x)));
        }
        System.out.printf("Externalizable check, max diff = %.3g%n", maxDiffExt);

    }
}
