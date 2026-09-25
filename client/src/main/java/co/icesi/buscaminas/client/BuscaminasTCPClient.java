package co.icesi.buscaminas.client;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class BuscaminasTCPClient {

    private final Gson gson;

    public BuscaminasTCPClient() {
        gson = new Gson();
    }

    public Response sendRequest(String host, int port, Request request) throws IOException {

        try (
                Socket socket = new Socket(host, port);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())
                )
        ) {

            // Convertir el Request a JSON
            String jsonOut = gson.toJson(request);

            // Enviar el JSON terminado en salto de línea
            writer.write(jsonOut);
            writer.newLine();
            writer.flush();

            // Leer la respuesta del servidor
            String jsonIn = reader.readLine();

            // Convertir el JSON recibido a Response
            return gson.fromJson(jsonIn, Response.class);
        }
    }
}
