package edu.seg2105.edu.server.backend;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 


import java.io.IOException;

import edu.seg2105.client.common.ChatIF;
import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 */
public class EchoServer extends AbstractServer 
{
  //Instance variables **********************************************	
  /**
    * The interface type variable.  It allows the implementation of 
	* the display method in the server.
	*/	
  ChatIF serverUI;
  
  /**
    * The type of key that we specified for the login of clients to the server.
    * Advantage is that it avoids typo mistakes when reusing the methods getInfo
    * and setInfo from ConnectionToClient. Also, we do not want it to be hard-coded.
	*/
  static final String loginKey="loginID";
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   * @param serverUI The interface type variable.
   */
  public EchoServer(int port, ChatIF serverUI){
    super(port);
    this.serverUI = serverUI;
  }

  
  //Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient
    (Object msg, ConnectionToClient client)
  { 
	System.out.println("Message received: " + msg + " from " + client.getInfo(loginKey)+".");
	String msgStr=(String) msg;
	if (msgStr.startsWith("#login")) {
		String loginID=msgStr.substring(6).trim();
		//Check if the client is already connected to the server
		if (client.getInfo(loginKey)!=null) {
			try {
				client.sendToClient("Error: You are already connected to the server. Terminating the connection.");
			}catch(IOException e) {
				serverUI.display("An error occured: Could not send message to client");
			}
			try {
				client.close();
			}catch(IOException e) {
				serverUI.display("An error occured: Could not disconnect client");
			}
			return;
		}
		//The loginID cannot be of length below 3 characters, close the client 
		//if the condition is not met
		if (loginID.length()<3) {
			try {
				client.sendToClient("Error: Login id should have a minimum length of 3.");
				client.close(); //Closes the connection to the client
			}catch(IOException e) {
				serverUI.display("An error occured: Could not disconnect client");
			}
		}
		else
			client.setInfo(loginKey, loginID);
		    System.out.println(loginID+" has logged on.");
	} else {
		String prefix=(String) client.getInfo(loginKey);
	    this.sendToAllClients(prefix+"> "+msg);
	} 
  }
  
  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */
  public void handleMessageFromServerUI(String message) {
	  try {
		  if (message.startsWith("#")) {
	    		handleCommand(message); //Helper method to handle the commands typed in the server console
	    	}
	    	else {
	    		serverUI.display(message);
	    		sendToAllClients("SERVER MSG> "+message); //Does not throw exception but the method it calls does
	    	}		
	  }catch(Exception e) {
		  serverUI.display("Could not send message to connected clients.");
	  }
  }
  
  /**
   * This method helps the handleMessageFromServerUI(...) method            
   * Defines what to do if a certain command has been typed
   */
  public void handleCommand(String command) throws Exception, NumberFormatException, IOException{
	  if (command.equals("#quit")) {
		  //#quit causes the server to quit gracefully
		  quit(); 
	  }
	  else if(command.equals("#stop")) {
		  //#stop causes the server to stop listening for new clients
		  stopListening();
	  }
	  else if(command.equals("#close")) {
		  //#close causes the server not only to stop listening for new clients, but also to disconnect 
		  //all existing clients
		  stopListening(); //Stops listening to clients
		  try {
			  close(); //Disconnects all existing clients //throws IOException
		  }catch(IOException e) {
			  serverUI.display("An error occured: Could not disconnect all existing clients. Try again.");
		  }		  
	  }
	  else if(command.startsWith("#setport")) {
		  //#setport <port> calls the setPort method in the server: only allowed if the server is closed
		  if (!isListening() && getNumberOfClients()==0) {
			  //If the server is not listening and has no connected clients, meaning that it is closed
			  String newPort=command.substring(8);
			  newPort=newPort.trim();
			  int port;
			  try {
				  port=Integer.parseInt(newPort); //throws NumberFormatException
				  setPort(port);
				  serverUI.display("You set the new port to be: "+port);
			  }catch(NumberFormatException ne) {
				 serverUI.display
				 (newPort+" is not an integer. To set a new port, please try again."); 
			  }
		  }
		  else {
			  serverUI.display
			  ("Server is not closed. To set a new port, use the #close command to close the server.");
		  }
	  }
	  else if(command.equals("#start")) {
		  //#start causes the server to start listening for new clients: only valid if the server is stopped
		  if (isListening()) {
			  serverUI.display("Server is already listening for new clients.");
			  return;
		  }
		  try {
			  listen();
		  }catch(IOException e) {
				  serverUI.display("An error occured while trying to listen for new clients. Please try again.");
		  }
	  }
	  else if(command.equals("#getport")) {
		  //#getport displays the current port number
		  serverUI.display("Current port number is: "+getPort());
	  }
	  else {
		  serverUI.display("Not a command.");
		  serverUI.display(command);
		  try {
	    	  sendToAllClients("SERVER MSG> "+command);
		  }catch(Exception e) {
			  serverUI.display("Could not send message to connected clients.");
		  }
	  }
  }
  
  /**
   * This method terminates the server.
   */
  public void quit()
  {
    try
    {
      close();
    }
    catch(IOException e) {}
    System.exit(0);
  }
    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println
      ("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println
      ("Server has stopped listening for connections.");
  }
  
  /**
   * Implements hook method in the superclass. run() method in ConnectionToClient calls
   * clientConnected(...) method below.
   * 
   * Prints out a nice message whenever a client connects.
   **/
  @Override
  protected void clientConnected(ConnectionToClient client){
	  System.out.println("A new client has connected to the server.");
  }
  
  /**
   * Implements hook method in the superclass. close() method in ConnectionToClient calls
   * clientDisconnected(...) method below. 
   * 
   * Prints out a nice message whenever a client disconnects.
   **/
  @Override
  synchronized protected void clientDisconnected(ConnectionToClient client) {
		// Since we don't track which ID belongs to this client directly,
		// remove by value.
	  System.out.println(client.getInfo(loginKey)+" has disconnected.");
	  super.clientDisconnected(client); //Since we do not have access to the private instance clientConnections
	}
  
  /**
   * Implements hook method in the superclass. Called by the OCSF framework when a client
   * disconnects abruptly. 
   * 
   * Prints out a nice message whenever a client disconnects unexpectedly.
   **/
  @Override
  synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
	  System.out.println(client.getInfo(loginKey)+" has disconnected unexpectedly: "+exception.getMessage());
	  super.clientDisconnected(client); //Since we do not have access to the private instance clientConnections
  }
}
//End of EchoServer class
