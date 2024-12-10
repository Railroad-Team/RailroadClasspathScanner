package dev.turtywurty;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final int PORT = 25565;

    public static void main(String[] args) {
        try(var serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while(serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch(IOException exception) {
            System.err.println("Error starting server: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final Kryo kryo = new Kryo();

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;

            kryo.setRegistrationRequired(false);
        }

        @Override
        public void run() {
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
            try(var in = clientSocket.getInputStream();
                var out = clientSocket.getOutputStream();
                Input kryoIn = new Input(in);
                Output kryoOut = new Output(out)) {
                while(true) {
                    CompilerQuery query = kryo.readObject(kryoIn, CompilerQuery.class);
                    System.out.println("Received query: " + query);

                    List<CompilerResponse.ResponseMethod> methods = new ArrayList<>();
                    try {
                        Class<?> clazz = Class.forName(query.getClassName());
                        for (Method method : clazz.getDeclaredMethods()) {
                            var responseMethod = new CompilerResponse.ResponseMethod(
                                    method.getName(),
                                    method.getReturnType().getName(),
                                    Arrays.stream(method.getParameters())
                                            .collect(Collectors.toMap(Parameter::getName, parameter -> parameter.getType().getName()))
                            );

                            methods.add(responseMethod);
                        }
                    } catch (ClassNotFoundException exception) {
                        System.err.println("Error finding class: " + exception.getMessage());
                        exception.printStackTrace();
                    }

                    var response = new CompilerResponse(methods);
                    kryo.writeObject(kryoOut, response);
                    kryoOut.flush();
                }
            } catch(IOException exception) {
                System.err.println("Error handling client: " + exception.getMessage());
                exception.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch(IOException exception) {
                    System.err.println("Error closing client socket: " + exception.getMessage());
                    exception.printStackTrace();
                }
            }
        }
    }
}