package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Date;


public class ServerWorker extends Thread{
    private final Socket clientSocket;

    public ServerWorker(Socket clientSocket){
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
        OutputStream outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ( ( line = reader.readLine()) != null){
            //Had to download apache refer to point 1
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0) {


                String cmd = tokens[0];
                if ("quit".equalsIgnoreCase(line)) {
                    break;
                } else if ("login".equalsIgnoreCase(cmd))
                    handleLogin(outputStream, tokens);
            }else
                {
                String msg = "unknown: " + cmd + "\n";
                outputStream.write(msg.getBytes());

            }}
        }

        clientSocket.close();
    }


}


//Point 1- Search for "How to Install StringUtils" and click the first link. Then go into IntelliJ>file>project structure>modules>dependencies>+>navigate to apache folder