package co.icesi.buscaminas.client;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final String HOST = "localhost";
    private static final int PORT = 12345;


    private static final Gson GSON = new Gson();

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        BuscaminasTCPClient client = new BuscaminasTCPClient();

        boolean running = true;

        while (running) {

            printMenu();
            System.out.print("Seleccione una opción: ");
            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    initGame(scanner, client);
                    break;
                case 2:
                    selectCell(scanner, client);
                    break;
                case 3:
                    markCell(scanner, client);
                    break;
                case 4:
                    getBoard(client);
                    break;
                case 5:
                    showAll(client);
                    break;
                case 6:
                    running = false;
                    System.out.println("Saliendo del cliente...");
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=============================================");
        System.out.println("     BUSCAMINAS DISTRIBUIDO - CLIENTE TCP");
        System.out.println("=============================================");
        System.out.println("[1] Iniciar nueva partida (Filas, Columnas, Minas)");
        System.out.println("[2] Destapar celda (Fila, Columna)");
        System.out.println("[3] Marcar / Desmarcar bandera (Fila, Columna)");
        System.out.println("[4] Consultar estado actual del tablero");
        System.out.println("[5] Rendirse y revelar tablero completo");
        System.out.println("[6] Salir");
        System.out.println();
    }

    private static void initGame(Scanner scanner, BuscaminasTCPClient client) {

        System.out.print("Filas: ");
        int rows = scanner.nextInt();

        System.out.print("Columnas: ");
        int columns = scanner.nextInt();

        System.out.print("Minas: ");
        int mines = scanner.nextInt();

        Map<String, String> data = new HashMap<>();
        data.put("n", String.valueOf(rows));
        data.put("m", String.valueOf(columns));
        data.put("minas", String.valueOf(mines));

        Request request = new Request();
        request.action = "INIT_GAME";
        request.data = data;

        sendAndPrint(client, request);
    }

    private static void selectCell(Scanner scanner, BuscaminasTCPClient client) {

        System.out.print("Fila: ");
        int row = scanner.nextInt();

        System.out.print("Columna: ");
        int column = scanner.nextInt();

        Map<String, String> data = new HashMap<>();
        data.put("i", String.valueOf(row));
        data.put("j", String.valueOf(column));

        Request request = new Request();
        request.action = "SELECT_CELL";
        request.data = data;

        sendAndPrint(client, request);
    }

    private static void markCell(Scanner scanner, BuscaminasTCPClient client) {

        System.out.print("Fila: ");
        int row = scanner.nextInt();

        System.out.print("Columna: ");
        int column = scanner.nextInt();

        Map<String, String> data = new HashMap<>();
        data.put("i", String.valueOf(row));
        data.put("j", String.valueOf(column));

        Request request = new Request();
        request.action = "MARK_CELL";
        request.data = data;

        sendAndPrint(client, request);
    }

    private static void getBoard(BuscaminasTCPClient client) {

        Request request = new Request();
        request.action = "GET_BOARD";
        request.data = new HashMap<>();

        sendAndPrint(client, request);
    }

    private static void showAll(BuscaminasTCPClient client) {

        Request request = new Request();
        request.action = "SOW_ALL";
        request.data = new HashMap<>();

        sendAndPrint(client, request);
    }

    private static void sendAndPrint(BuscaminasTCPClient client, Request request) {

        try {
            Response response = client.sendRequest(HOST, PORT, request);

            System.out.println();

            if (response == null) {
                System.out.println("No se recibió respuesta del servidor.");
                return;
            }

            System.out.println("Estado: " + response.status);

            if (response.data == null) {
                return;
            }

            if (response.data.containsKey("message")) {
                System.out.println(response.data.get("message"));
            }

            Cell[][] board = extractBoard(response);
            if (board != null) {
                BoardRenderer.printBoard(board);
            }

            handleGameEnd(response, client);

        } catch (Exception e) {
            System.out.println("Error de comunicación con el servidor: " + e.getMessage());
        }
    }

    /**
     * response.data es Map<String, Object>, así que Gson deserializa el
     * valor de "board" como List<List<LinkedTreeMap>>, no como Cell[][].
     * Para recuperar el tipo correcto, se vuelve a serializar ese objeto
     * genérico a JSON y se reinterpreta explícitamente como Cell[][].
     */
    private static Cell[][] extractBoard(Response response) {

        if (!response.data.containsKey("board")) {
            return null;
        }

        Object rawBoard = response.data.get("board");
        if (rawBoard == null) {
            return null;
        }

        String boardJson = GSON.toJson(rawBoard);
        return GSON.fromJson(boardJson, Cell[][].class);
    }

    private static void handleGameEnd(Response response, BuscaminasTCPClient client) {

        boolean gameEnd = Boolean.TRUE.equals(response.data.get("gameEnd"));
        if (!gameEnd) {
            return;
        }

        boolean win = Boolean.TRUE.equals(response.data.get("win"));

        if (win) {
            System.out.println("\u001B[32m*** ¡Felicidades, ganaste la partida! ***\u001B[0m");
        } else {
            System.out.println("\u001B[31m*** ¡BOOM! Pisaste una mina. Fin del juego. ***\u001B[0m");
            showAll(client);
        }
    }
}