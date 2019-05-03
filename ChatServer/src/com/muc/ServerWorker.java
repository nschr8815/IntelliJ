package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.List;


public class ServerWorker extends Thread{
    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;

    public ServerWorker(Server server, Socket clientSocket){
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public void run(){
        try {
            clientHandle();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }  private void clientHandle() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ( ( line = reader.readLine()) != null){
            //Had to download apache refer to point 1
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("quit".equalsIgnoreCase(line)) {
                    break;
                } else if ("login".equalsIgnoreCase(cmd)){
                    handleLogin(outputStream, tokens);
                }else
                {
                String msg = "unknown: " + cmd + "\n";
                outputStream.write(msg.getBytes());

            }}
        }

        clientSocket.close();
    }


    public String getLogin() {
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3){
            String login = tokens[1];
            String password = tokens[2];

            if((login.equals("guest") && password.equals("guest")) || (login.equals("SupremeOverlord") && password.equals("adminBest"))){
                String msg = "Login Valid" + "\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User " + login + " logged in succesfully" );

                String onlineMsg = "User " +login + " is now online " + "\n";
                List<ServerWorker> workerList = server.getWorkerList();
                for (ServerWorker worker : workerList){
                    worker.send(onlineMsg);
                }
            }else{
                String msg = "Login Invalid" + "\n";
                outputStream.write(msg.getBytes());
            }
        }
    }

    private void send(String msg) throws IOException {
        outputStream.write(msg.getBytes());
    }


}


//Point 1- Search for "How to Install StringUtils" and click the first link. Then go into IntelliJ>file>project structure>modules>dependencies>+>navigate to apache folder