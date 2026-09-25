package co.icesi.buscaminas.client;

public class BoardRenderer {

    public static void printBoard(Cell[][] board) {

        if (board == null || board.length == 0) {
            System.out.println("El tablero está vacío.");
            return;
        }

        int rows = board.length;
        int columns = board[0].length;

        // Cabecera de columnas
        System.out.print("    ");
        for (int j = 0; j < columns; j++) {
            System.out.printf("%3d ", j);
        }
        System.out.println();

        // Línea separadora
        printSeparator(columns);

        // Filas
        for (int i = 0; i < rows; i++) {
            System.out.printf("%2d |", i);
            for (int j = 0; j < columns; j++) {

                Cell cell = board[i][j];
                String value;

                if (cell == null) {
                    value = "?";
                } else if (cell.isMarked()) {
                    // Amarillo
                    value = "\u001B[33mM\u001B[0m";
                } else if (cell.isHide() && !cell.isShowAll()) {
                    value = ".";
                } else if (cell.isLandMine()) {
                    // Rojo
                    value = "\u001B[31m*\u001B[0m";
                } else {
                    int cellValue = cell.getValue();
                    value = (cellValue == 0) ? " " : String.valueOf(cellValue);
                }

                System.out.printf(" %s |", value);
            }
            System.out.println();
            printSeparator(columns);
        }
    }

    private static void printSeparator(int columns) {
        System.out.print("   +");
        for (int j = 0; j < columns; j++) {
            System.out.print("----+");
        }
        System.out.println();
    }
}