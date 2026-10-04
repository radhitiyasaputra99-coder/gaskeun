package travel.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ConsoleStyle {

    private ConsoleStyle() {
    }

    public static String table(String[] headers, List<String[]> rows, int... rightAligned) {
        int cols = headers.length;
        int[] widths = new int[cols];
        for (int i = 0; i < cols; i++) {
            widths[i] = headers[i].length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < cols; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }

        Set<Integer> right = new HashSet<>();
        for (int index : rightAligned) {
            right.add(index);
        }

        String separator = separator(widths);
        StringBuilder sb = new StringBuilder();
        sb.append(separator).append('\n');
        sb.append(formatRow(headers, widths, Set.of())).append('\n');
        sb.append(separator).append('\n');
        for (String[] row : rows) {
            sb.append(formatRow(row, widths, right)).append('\n');
        }
        sb.append(separator);
        return sb.toString();
    }

    public static String box(String title, List<String> lines) {
        int width = title.length();
        for (String line : lines) {
            width = Math.max(width, line.length());
        }
        String border = "+" + "-".repeat(width + 2) + "+";
        StringBuilder sb = new StringBuilder();
        sb.append(border).append('\n');
        sb.append("| ").append(pad(title, width, false)).append(" |").append('\n');
        sb.append(border).append('\n');
        for (String line : lines) {
            sb.append("| ").append(pad(line, width, false)).append(" |").append('\n');
        }
        sb.append(border);
        return sb.toString();
    }

    private static String separator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append('+');
        }
        return sb.toString();
    }

    private static String formatRow(String[] cells, int[] widths, Set<Integer> right) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            sb.append(' ').append(pad(cells[i], widths[i], right.contains(i))).append(" |");
        }
        return sb.toString();
    }

    private static String pad(String text, int width, boolean alignRight) {
        return alignRight ? String.format("%" + width + "s", text) : String.format("%-" + width + "s", text);
    }
}
