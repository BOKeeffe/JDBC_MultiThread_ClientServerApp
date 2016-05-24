package ie.wit.Assign2;

import java.awt.EventQueue;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;

import java.awt.Color;

/**
 * @author williamokeeffe
 * 
 *	Class ClientA2 
 */

public class ClientA2 extends Thread{
	
	private final int PORT = 8000; 
	private final String localhost = "localhost"; 

	//The ip address of the server
	static InetAddress inetAddress;
	static Socket socket;
	
	//Jframe Variables
	private JFrame frame;
	private JTextField interestField;
	private JTextField yearsField;
	private JTextField amountField;
	private JTextField accountNumField;
	
	private JTextArea jta;
	
	//submit Button variable
	private JButton btnSubmit;
	
	 // IO streams
	 private DataOutputStream clientOut;
	 private DataInputStream clientIn;


	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					ClientA2 window = new ClientA2();
					window.frame.setVisible(true);
					inetAddress = socket.getInetAddress();
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	/**
	 * Client constructor
	 */
	public ClientA2() {
		
		initialize();
	    try {
	      // Create a socket to connect to the server
	       socket = new Socket(localhost, PORT);
	      
	      // Create an input stream to receive data from the server
	      clientIn = new DataInputStream(socket.getInputStream());

	      // Create an output stream to send data to the server
	      clientOut = new DataOutputStream(socket.getOutputStream());
	    }
	    catch (IOException ex) {
	      jta.append(ex.toString() + '\n');
	    }
	}

	/**
	 * @author williamokeeffe
	 * Method to construct the Jframe and contents 
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle("Client");
		frame.setBounds(100, 100, 450, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblAccountNumber = new JLabel("Account Number");
		lblAccountNumber.setBounds(6, 6, 148, 16);
		frame.getContentPane().add(lblAccountNumber);
		
		JLabel lblAccount = new JLabel("Annual Interest Rate");
		lblAccount.setBounds(6, 36, 148, 16);
		frame.getContentPane().add(lblAccount);
		
		JLabel lblNumberfYears = new JLabel("Number of Years");
		lblNumberfYears.setBounds(6, 69, 148, 16);
		frame.getContentPane().add(lblNumberfYears);
		
		JLabel lblLoanAmount = new JLabel("Loan Amount");
		lblLoanAmount.setBounds(6, 102, 148, 16);
		frame.getContentPane().add(lblLoanAmount);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 164, 438, 186);
		frame.getContentPane().add(scrollPane);
		
		jta = new JTextArea();
		jta.setEditable(false);
		jta.setWrapStyleWord(true);
		jta.setLineWrap(true);
		jta.setBackground(Color.LIGHT_GRAY);
		scrollPane.setViewportView(jta);
		
		interestField = new JTextField();
		interestField.setBounds(166, 31, 148, 26);
		frame.getContentPane().add(interestField);
		interestField.setColumns(10);
		
		yearsField = new JTextField();
		yearsField.setBounds(166, 64, 148, 26);
		frame.getContentPane().add(yearsField);
		yearsField.setColumns(10);
		
		amountField = new JTextField();
		amountField.setBounds(166, 97, 148, 26);
		frame.getContentPane().add(amountField);
		amountField.setColumns(10);
		
		accountNumField = new JTextField();
		accountNumField.setBounds(166, 1, 148, 26);
		frame.getContentPane().add(accountNumField);
		accountNumField.setColumns(10);
		
		btnSubmit = new JButton("Submit");
		btnSubmit.setBounds(327, 69, 117, 52);
		frame.getContentPane().add(btnSubmit);
		btnSubmit.addActionListener(new Listener());
	}
	
	
	/**
	 * @author williamokeeffe
	 * 
	 * Method to determine which action was performed in JFrame, 
	 * i.e button was pressed
	 *
	 */
	private class Listener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			//check if submit button was pressed
			if (e.getSource() == btnSubmit) {
				
				//Check if textfields contain data before sending to server
				if (yearsField.equals(null) || interestField.equals(null) || amountField.equals(null)) {

					// Do Nothing... textField empty
					
				} else {

					try {
						System.out.println("Submit button working");

						// Convert data from textFields and Send to server 
						double rate = Double.parseDouble(interestField.getText().trim());
						double year = Double.parseDouble(yearsField.getText().trim());
						double loanAmt = Double.parseDouble(amountField.getText().trim());
						int accNum = Integer.parseInt(accountNumField.getText().trim());
						
						
						
						clientOut.writeInt(accNum);			// Send the account number to the server					
						clientOut.writeDouble(rate);		// Send the amount of years to the server					
						clientOut.writeDouble(year);		// Send the Interest Rate to the server
						clientOut.writeDouble(loanAmt); 	// Send the number of amount to the server
						clientOut.flush();
						
						//Receive computed data from server;
						double interestRate = clientIn.readDouble();
						double totalLoanAmount = clientIn.readDouble();
						double montlhyRepay = clientIn.readDouble();
						
						//name of client retrieved from database
						String firstName = clientIn.readUTF();
						String lastName = clientIn.readUTF();
						
						//keep calculations to two decimal place
						DecimalFormat decimalFormart = new DecimalFormat("#.##");
						interestRate = Double.valueOf(decimalFormart.format(interestRate));
						totalLoanAmount = Double.valueOf(decimalFormart.format(totalLoanAmount));
						montlhyRepay = Double.valueOf(decimalFormart.format(montlhyRepay));
						
						//boolean from server to confirm client is registered
						boolean clientRegistered = clientIn.readBoolean();
						
						//client registered check and display received information from server
						if(clientRegistered){
							
							// Append calculated input from server to the clients Java text area (jta)
							double costOfLoan = totalLoanAmount-loanAmt;
							costOfLoan = Double.valueOf(decimalFormart.format(costOfLoan));
							
							jta.append("\n" + "****Message from Server at " + inetAddress + " ****" + "\n\n");
							jta.append("Welcome Back " + firstName + " " + lastName + ", Requested information is listed below" + "\n\n");
							jta.append("With " + interestRate + "% interest applied " + "\n");
							jta.append("Monthly repayments: " + montlhyRepay + "\n" + "Total Repayment: " + totalLoanAmount + "\n" + "Cost of loan: "+ costOfLoan + "\n");
							
						}else{
							jta.append("\n" + "****Message from Server at " + inetAddress + " ****" + "\n\n");
							jta.append("" + "Sorry our records show that " + accNum +" is not a registered account ");
							System.out.println("Not registered");
							System.out.println("Boolean is " + clientRegistered);
						}
					} catch (IOException ex) {
						System.err.println(ex);
						System.out.println("error in get action btnSubmit");
					}
				}
			}
		}
	}
}


