package com.example.qrcodemanager;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;


public class MyserverConnection {
    private final Context context;
    private static final String serverAddress="192.168.1.3";

    public MyserverConnection(@Nullable Context context) {
        this.context = context;
    }

    public void addProduct(Product pd) {
        boolean success = false;
        try {
            Socket socket = new Socket(serverAddress, 5000);

            // Create output stream to send product object to server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("generate");
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(pd);

            // Receive response from server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();

            // Check if server successfully added the product to the database
            if (response.equals("Success")) {
                success = true;
            }
            // Close the socket and streams
            oos.close();
            in.close();
            socket.close();
            if (!success) {
                Toast.makeText(context, "Failed to insert data in database", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(context, "Added Successfully!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Product getProduct(String qrdata) {
        Product pd = null;
        ObjectInputStream ois=null;
        try {
            Socket socket = new Socket(serverAddress, 5000);
            PrintWriter out= new PrintWriter(socket.getOutputStream(), true);
            out.println("scan");
            out.println(qrdata);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String command=in.readLine();

            if (command.equalsIgnoreCase("failed")){
                Toast.makeText(context, "Product doesn't exist!", Toast.LENGTH_SHORT).show();
            }
            else{
                ois = new ObjectInputStream(socket.getInputStream());
                pd=(Product)ois.readObject();
            return pd;
            }
            out.close();
            in.close();
            if(ois!=null)
            {ois.close();}
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return pd;
    }
}

