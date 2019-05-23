package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


public class ServerWorker extends Thread{
    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();

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
                if ("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                }else if ("join".equalsIgnoreCase(cmd)){

                    handleJoin(tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokensMsg);
                }else if ("leave".equalsIgnoreCase(cmd)){
                    handleLeave(tokens);
                }else
                {
                String msg = "unknown: " + cmd + "\n";
                outputStream.write(msg.getBytes());

            }}
        }

        clientSocket.close();
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1){
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    public boolean isMemberOfTopic(String topic){
        return topicSet.contains(topic);
    }


    private void handleJoin(String[] tokens) throws IOException {
        List<ServerWorker> workerList = server.getWorkerList();


            if (tokens.length > 1) {
                String topic = tokens[1];
                topicSet.add(topic);
                String topicAdded = "New topic: " + topicSet + " added\n";
                //outputStream.write(topicAdded.getBytes());
               // worker.send(topicAdded);

            }
            
        }



    //Format: msg <user> <message>
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String msg = tokens[2];

        boolean isTopic = sendTo.charAt(0) == '#';

        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker: workerList){
            if (isTopic){
                if (worker.isMemberOfTopic(sendTo)){
                    String outmsg = "Message from group " + sendTo + ": From User " + login + ": " + msg + "\n";
                    worker.send(outmsg);
                }
            }
            if(sendTo.equalsIgnoreCase(worker.getLogin())){
                String outmsg = "Message from " + login + ": " + msg + "\n";
                worker.send(outmsg);
            }
        }



    }

    private void handleLogoff() throws IOException{
        server.removeworker(this);
        List<ServerWorker> workerList = server.getWorkerList();

        for (ServerWorker worker : workerList){
            if (!login.equals(worker.getLogin())){
                String onlineMsg = "offline: " + login + "\n";
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }


    public String getLogin() {

        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException, InterruptedException {
        if (tokens.length == 3){
            String login = tokens[1];
            String password = tokens[2];

            if((login.equals("guest") && password.equals("guest")) || (login.equals("SupremeOverlord") && password.equals("adminBest"))){
                String msg = "ok login" + "\n";
                outputStream.write(msg.getBytes());

                this.login = login;

                List<ServerWorker> workerList = server.getWorkerList();


                //Send current user all current online users
                for (ServerWorker worker : workerList) {
                    if (!login.equals(worker.getLogin())) {
                        if (worker.getLogin() != null) {
                            String msg2 = "online: " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }
                //Send other online users current users status
                String onlineMsg = "online: " + login + "\n";

                for (ServerWorker worker : workerList){
                    if (!login.equals(worker.getLogin())){
                        worker.send(onlineMsg);
                    }
                }
            }else{
                String msg = "error login" + "\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for" + login);
            }
        }
    }

    private void send(String msg) throws IOException {
        if (login != null) {

            outputStream.write(msg.getBytes());

        }

    }


}


//Point 1- Search for "How to Install StringUtils" and click the first link. Then go into IntelliJ>file>project structure>modules>dependencies>+>navigate to apache folder