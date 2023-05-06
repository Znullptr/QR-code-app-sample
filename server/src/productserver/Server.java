package productserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.example.qrcodemanager.*;

public class Server {
	
    private final static String dbUrl = "jdbc:mysql://localhost:3306/tahardb";
    private final static String username = "root";
    private final static  String password = "tahar";
    
    
    public static void main(String[] args)  throws Exception{
        final int PORT = 5000; // Port number for server socket
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        PrintWriter out = null;
        BufferedReader in = null;
        

        try {
            // Initialize server socket and listen for incoming connections
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server listening on port " + PORT + "...");
            
            
            while (true) {
                // Accept incoming connection
                clientSocket = serverSocket.accept();
                System.out.println("Client connected from " + clientSocket.getInetAddress().getHostName() + "...");
                 out = new PrintWriter(clientSocket.getOutputStream(), true);
                 in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String command = in.readLine();
                if (command.equalsIgnoreCase("generate")) {
                ois = new ObjectInputStream(clientSocket.getInputStream());
                Product pd = (Product) ois.readObject();
                System.out.println("Received Product object: " + pd.toString());
                // Insert Product object into database
                insertProduct(pd);
                out.println("Success");}
                else if(command.equalsIgnoreCase("scan")) {
                    String qrData = in.readLine();
                    Product pd = retrieveProduct(qrData);
                    if(pd==null) {
                    	out.println("Failed");
                    }
                    else {
                  
                    out.println("Success");
                    oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    oos.writeObject(pd);
                    System.out.println("Sent Product object: " + pd.toString());
                    }
                    
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();}
       finally {
            // Close resources
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            if(in != null) {
            in.close();}
            if(out != null)
            {
            out.close();}
            
            if (clientSocket != null) {
                clientSocket.close();
            }
            
            
        }
    }

    
        
        private static void insertProduct(Product pd) {
            try {
                Connection conn = DriverManager.getConnection(dbUrl,username,password);
    	        String query = "INSERT INTO products (product_name, product_price, QRCodeData ) VALUES (?, ?, ?)";
    	        PreparedStatement pstmt = conn.prepareStatement(query);
    	        pstmt.setString(1, pd.getName());
    	        pstmt.setDouble(2, pd.getPrice());
    	        pstmt.setString(3, pd.getQRData());
    	        pstmt.executeUpdate();
    	        conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }}
        
        private static Product retrieveProduct(String qrData) {
            try {
                Connection conn = DriverManager.getConnection(dbUrl,username,password);
                String query = "SELECT * FROM products WHERE QRCodeData = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, qrData);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String name = rs.getString("product_name");
                    double price = rs.getDouble("product_price");

                    conn.close();

                    return new Product(name, price);
                } else {
                    conn.close();
                    return null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
}
            

