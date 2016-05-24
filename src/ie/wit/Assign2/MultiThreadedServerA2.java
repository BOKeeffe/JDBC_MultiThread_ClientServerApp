package ie.wit.Assign2;

import java.io.*;

import java.net.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

/**
 * @author williamokeeffe
 * 
 * Class MultiThreadedServerA2 server that implements Runnable
 * 
 */

public class MultiThreadedServerA2 extends JFrame implements Runnable {

	
	public static final int PORT = 8000;
	static InetAddress inetAddress;
	private boolean clientRegistered;
	
	// Text area for displaying contents
	private static JTextArea jta;
	
	//IO variables
	int reciveAccNum;
	double reciveRate; 
	double reciveYears;
	double reciveAmt ;
	double totalAmount;
	double monthlyPayment;
	int number;
	String firstName;
	String lastName;
	
	Socket socket;
	
	public MultiThreadedServerA2(Socket socket){
		this.socket = socket;
	}
	
	/**
	 * @author williamokeeffe
	 * class constructor
	 */
	public MultiThreadedServerA2(){
		jta = new JTextArea();
		jta.setEditable(false);
		DefaultCaret c = (DefaultCaret)jta.getCaret();
		c.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		// Place text area on the frame
	    setLayout(new BorderLayout());
	    add(new JScrollPane(jta), BorderLayout.CENTER);

	    setTitle("Server");
	    setSize(500, 300);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setVisible(true); // It is necessary to show the frame here!
		
	}

	/**
	 * 
	 * @author williamokeeffe
	 * 
	 * 
	 *
	 */
	public static void main(String[] args) throws ClassNotFoundException {
		
		ServerSocket serverSocket;
		//create new MultiThreadedServerA2
		new MultiThreadedServerA2();
		
		//setting up connections and threads
	 try {
			// Create a server socket
		 	Class.forName("com.mysql.jdbc.Driver");
		 
			serverSocket = new ServerSocket(PORT);
			inetAddress = serverSocket.getInetAddress();
			System.out.println("Listening " + "\n");
			jta.append("Server started at " + new Date() + '\n' + " Listening on port: " + PORT + "\n");

			while (true) {

				Socket cSocket = serverSocket.accept();							// Connect a new client
				inetAddress = cSocket.getInetAddress();							//Clients address
				Thread thread = new Thread(new MultiThreadedServerA2(cSocket)); //thread created
				thread.start();	
				jta.append("Client at " +inetAddress + " Connected " + "\n");	//thread started
				System.out.println("Connected");
				System.out.println("thread started");
			}
		} catch (IOException ex) {
			System.err.println(ex);
			System.out.println("Check PORT 800,  Port may be in use");
		}
	}

	
	/**
	 * Calculate monthly loan and total payments
	 * This method takes in three parameter from the clients output stream
	 *  which are used to calculate the monthly and total loan amounts
	 */
	private void calculateLoan(double rate, double amount, double years){
		
		double monthsInOneYear = 12;
		double numMonths = years*monthsInOneYear;
	
		//monthly payment with interest applied using formula below
		monthlyPayment = (amount * rate / 1200) /
				(1 - Math.pow(1 / (1 + rate / 1200), numMonths));
		
		//Monthly payments * by 12 months and * total years to calculate total payment with interest applied
		totalAmount = (monthlyPayment * monthsInOneYear )* years; 
		
		//Keep doubles values to one decimal place
		DecimalFormat decimalFor = new DecimalFormat("#.##");
		monthlyPayment = Double.valueOf(decimalFor.format(monthlyPayment));
		totalAmount = Double.valueOf(decimalFor.format(totalAmount));
	}
	

	/**
	 * @author williamokeeffe
	 * 
	 *  This method returns a type boolean
	 *  
	 *  If a user is in the bank database a boolean type true is returned and the user credentials are 
	 *	and appended to server textArea
	 *  
	 *  If a user is not in database it returns boolean type false is returned;
	 *  
	 */
	private Boolean queryDataBase() {
		
		Connection connection; 
		
		try {
			connection = DriverManager.getConnection("jdbc:mysql:///BankDatabase", "root",
				"root");
			
			if (!connection.isClosed()){
				System.out.println("Successfully connected to MySQL server..."); 
			}
			
			//Query database statement for matching account number as client
			PreparedStatement statment = connection.prepareStatement("SELECT * FROM RegisteredApplicants WHERE AccountNum = ('"+reciveAccNum+"') " );
			ResultSet resultSet = statment.executeQuery();
			
			System.out.println(resultSet);
			
			if(resultSet.next()){
				clientRegistered = true;
				jta.append("Client at " + inetAddress + " is a registered account holder " + "\n\n");
				
			}else 
				clientRegistered = false;
			
			statment = connection.prepareStatement("SELECT AccountNum, FirstName, LastName FROM RegisteredApplicants WHERE AccountNum = ('"+reciveAccNum+"') " );
			resultSet = statment.executeQuery();
			if(resultSet.next()){
				
				number = resultSet.getInt("AccountNum");
				firstName = resultSet.getString("FirstName");
				lastName = resultSet.getString("LastName");
				jta.append("Client at " + inetAddress + " details ->"  + " Account Number: " + number + ", Name: " + firstName + " " + lastName + " \n\n");
				
			}
			
			resultSet.close();
			statment.close();
			connection.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return clientRegistered;
	}
	
	/**
	 * @author williamokeeffe
	 * 
	 * This method handles the data streams being being exchanged with clients
	 * Methods queryDataBase() and calculateLoan are called from this method
	 */
	public void dataTransfer(){
			try {
				//Server Data input output streams
				DataInputStream serverIn = new DataInputStream(socket.getInputStream());
				DataOutputStream serverOut = new DataOutputStream(socket.getOutputStream());
				
				while(true){
					//data received from client
					reciveAccNum = serverIn.readInt();
					reciveRate = serverIn.readDouble();
					reciveYears =  serverIn.readDouble();
					reciveAmt = serverIn.readDouble();
					//queryDataBase();
				
					
					//if queryDataBase() returns true, Client number exists.....(send data to client)
					if(queryDataBase()){
							calculateLoan(reciveRate, reciveAmt, reciveYears);
							
							//Send computed data and information retrieved from Bank DataBase back to client
							serverOut.writeDouble(reciveRate);
							serverOut.writeDouble(totalAmount);
							serverOut.writeDouble(monthlyPayment);
							serverOut.writeUTF(firstName);
							serverOut.writeUTF(lastName);
							serverOut.writeBoolean(clientRegistered);
							
							
							//*********Debugging received info Delete later*************
							jta.append("Account Number: " + reciveAccNum + "\n");
							jta.append("Years: " + reciveYears + "\n");
							jta.append("Rate: " + reciveRate + "\n");
							jta.append("Amount: " + reciveAmt + "\n\n");
							jta.append("Monthly payment is: " + monthlyPayment + "  Total Amount to pay: " + totalAmount);	
							
					}
					else{
						//Client waiting for reply even if not registered, Send 0 values and message
						
						serverOut.writeInt(0);
						serverOut.writeDouble(0);
						serverOut.writeDouble(0);
						serverOut.writeDouble(0);
						//serverOut.writeUTF(firstName);
						//serverOut.writeUTF(lastName);
						serverOut.writeBoolean(clientRegistered);
						jta.append("Client is not registered " + "\n");
						
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	/**
	 * @author williamokeeffe
	 * 
	 * implemented method for Runnable
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		dataTransfer();
	}
	
}