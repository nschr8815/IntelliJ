package com.muc;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;


import java.net.ServerSocket;

public class ServerMain {

    public static void main(String[] args) {
        int port = 8818;
        try {

            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                System.out.println("Accepting Connection...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from" + clientSocket);
                OutputStream outputStream = clientSocket.getOutputStream();
                outputStream.write("Hello World this is Nic\n".getBytes());
                for (int i = 0; i < 10; i++) {
                    outputStream.write(("Time is now " + new Date() + "\n").getBytes());
                    Thread.sleep(100);
                }
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
