package ru.payroll;

/**
 * Утилита форматирования сумм в копейках.
 */
public final class Money {

    private Money() {
    }

    /**
     * Форматирует копейки как «1 234,56» (рубли с пробелом тысяч и запятой).
     *
     * @param kopecks сумма в копейках
     * @return строка
     */
    public static String format(long kopecks) {
        long abs = Math.abs(kopecks);
        long rub = abs / 100;
        long kop = abs % 100;
        String sign = kopecks < 0 ? "−" : "";
        return sign + withSpaces(rub) + "," + String.format("%02d", kop);
    }

    private static String withSpaces(long n) {
        String s = Long.toString(n);
        var sb = new StringBuilder();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) {
                sb.append(' ');
            }
            sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    /**
     * Рубли → копейки.
     */
    public static long ofRubles(long rubles) {
        return rubles * 100;
    }

    /**
     * Рубли и копейки → копейки.
     */
    public static long of(long rubles, int kopecks) {
        if (kopecks < 0 || kopecks > 99) {
            throw new IllegalArgumentException("kopecks 0..99: " + kopecks);
        }
        return rubles * 100 + kopecks;
    }
}
