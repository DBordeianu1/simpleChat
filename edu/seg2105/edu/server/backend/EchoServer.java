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
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  //final public static int DEFAULT_PORT = 5555;
	
  //Instance variables **********************************************	
  /**
    * The interface type variable.  It allows the implementation of 
	* the display method in the server.
	*/	
  ChatIF serverUI;
  
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
    System.out.println("Message received: " + msg + " from " + client);
    this.sendToAllClients(msg);
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
			  serverUI.display("An error occured: could not disconnect all existing clients. Try again.");
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
	  System.out.println("New client "+client+" connected to the server");
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
	  System.out.println("Unknown client disconnected from the server");
	  /*
	   * Note: I did not write the line above as "System.out.println(client+"disconnected from the server");"
	   *      since it would print null as the client is null. Once we will be adding the Id (in exercise 3), 
	   *      we will have more adapted code. Until then, this is my solution to that problem.
	   * */
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
	  System.out.println("Unknown client disconnected unexpectedly from the server: "+exception.getMessage());
	  /*
	   * Note: I did not write the line above as "System.out.println(client+"disconnected unexpectedly from the server");"
	   *      since it would print null as the client is null. Once we will be adding the Id (in exercise 3), 
	   *      we will have more adapted code. Until then, this is my solution to that problem.
	   * */
	  super.clientDisconnected(client); //Since we do not have access to the private instance clientConnections
  }
  
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
  /*
  public static void main(String[] args) 
  {
    int port = 0; //Port to listen on

    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }
	
    EchoServer sv = new EchoServer(port);
    
    try 
    {
      sv.listen(); //Start listening for connections
    } 
    catch (Exception ex) 
    {
      System.out.println("ERROR - Could not listen for clients!");
    }
  }
  */
}
//End of EchoServer class
