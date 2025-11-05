// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package edu.seg2105.client.backend;

import ocsf.client.*;

import java.io.*;

import edu.seg2105.client.common.*;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 */
public class ChatClient extends AbstractClient
{
  //Instance variables **********************************************
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the client.
   */
  ChatIF clientUI; 
  
  String loginID;

  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the chat client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   */
  
  public ChatClient(String loginID, String host, int port, ChatIF clientUI) 
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.loginID=loginID;
    this.clientUI = clientUI;
    openConnection();
  }

  
  //Instance methods ************************************************
    
  /**
   * This method handles all data that comes in from the server.
   *
   * @param msg The message from the server.
   */
  public void handleMessageFromServer(Object msg) 
  {
    clientUI.display(msg.toString());
    
    
  }

  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */
  public void handleMessageFromClientUI(String message)
  {
    try
    {   //If #login is received at any other time, the server should send an error message 
    	//back to the client and terminate the connection. Therefore, we do not have to 
    	//verify if the client is connected to the server.
    	if (message.startsWith("#login")) {
    		sendToServer(message);
    	}
    	else if (message.startsWith("#")) {
    		handleCommand(message); //Helper method to handle the commands typed by the client
    	}
    	else sendToServer(message);
    }
    catch(IOException e)
    {
      clientUI.display
        ("Could not send message to server. Terminating client.");
      quit();
    }
  }
  
  /**
   * This method helps the handleMessageFromClientUI(...) method            
   * Defines what to do if a certain command has been typed
   */
  public void handleCommand(String command) throws IOException, NumberFormatException {
	  if (command.equals("#quit")) {
		  //#quit causes the client to terminate gracefully. Make sure the connection to the server is 
		  //terminated before exiting the program.
		  quit();
	  }
	  else if (command.equals("#logoff")) {
		  //#logoff causes the client to disconnect from the server, but not quit.  
		  try {
			  closeConnection();
    	  }
    	  catch(IOException e) {
    		  clientUI.display ("An error occured during log off, please try again.");
    	  }
	  }
	  else if (command.startsWith("#sethost")) {
		  //#sethost <host> calls the setHost method in the client. Only allowed if the client is logged 
		  //off; displays an error message otherwise. 
		  if (!isConnected()) { //Checks if the client is logged off
			  String newHost=command.substring(8);
			  newHost=newHost.trim();
			  //There are no constraints to the name of the host
			  setHost(newHost);
			  clientUI.display("You set the new host to be: "+newHost);
		  }
		  else {
			  clientUI.display
			  ("You are already logged in. To set a new host, use the #logoff command to disconnect from the server.");
		  }
	  }
      else if (command.startsWith("#setport")) {
    	  //#setport <port> Calls the setPort method in the client, with the same constraints as #sethost, 
    	  //however, port can only be an integer
    	  if (!isConnected()) { //Checks if the client is logged off
			  String newPort=command.substring(8);
			  newPort=newPort.trim();
			  int port;
			  try {
				  port=Integer.parseInt(newPort); //throws NumberFormatException
				  setPort(port);
				  clientUI.display("You set the new port to be: "+port);
			  }catch(NumberFormatException ne) {
				 clientUI.display
				 (newPort+" is not an integer. To set a new port, please try again."); 
			  }
		  }
		  else {
			  clientUI.display
			  ("You are already logged in. To set a new port, use the #logoff command to disconnect from the server.");
		  }
		  
	  }
	  /* do not need this anymore bc of the Exercise 3, Cliend Side, Part c), iv)
      else if (command.equals("#login")) {
    	  //#login causes the client to connect to the server. Only allowed if the client is not already 
    	  //connected; displays an error message otherwise.
    	  if (isConnected()) {
    		  clientUI.display
			  ("You are already logged in. To log in again, use the #logoff command beforehand.");
    		  return;
    	  }
    	  try {
    		  openConnection(); //throws IOException
    		  clientUI.display("Login successful");
    	  }catch(IOException e) {
    		  clientUI.display("An error occured during login, please try again.");
    	  }  
      }
	  */
      else if (command.equals("#gethost")) {
    	  //#gethost displays the current host name
    	  clientUI.display("Current host name is: "+getHost());
      }
      else if (command.equals("#getport")) {
    	  //#getport displays the current port number
    	  clientUI.display("Current port number is: "+getPort());
      }
      else {
    	  clientUI.display("Not a command.");
    	  try {
        	  sendToServer(command); //throws IOException
    	  }
    	  catch(IOException e) {
    		  clientUI.display ("Could not send message to server. Terminating client.");
    	      quit();
    	  }
      }
  }
  
  /**
   * This method terminates the client.
   */
  public void quit()
  {
    try
    {
      closeConnection();
    }
    catch(IOException e) {}
    System.exit(0);
  }
  
  /**
	 * Implements hook method called each time an exception is thrown by the client's
	 * thread that is waiting for messages from the server. The method may be
	 * overridden by subclasses.
	 * 
	 * @param exception
	 *            the exception raised.
	 */
    @Override
    protected void connectionException(Exception exception) {
	    clientUI.display("Server has shut down");
	    quit();
    }
  
  /**
	 * Implements hook method called after the connection has been closed. The default
	 * implementation does nothing. The method may be overridden by subclasses to
	 * perform special processing such as cleaning up and terminating, or
	 * attempting to reconnect.
	 */
    @Override
	protected void connectionClosed() {
    	clientUI.display("Connection closed");
	}
    
    /**
	 * Implements hook method called after a connection has been established. The default
	 * implementation does nothing. It may be overridden by subclasses to do
	 * anything they wish.
	 */
    @Override
	protected void connectionEstablished() {
    	try {
    		sendToServer("#login "+loginID);
    	}catch(IOException e) {
    		clientUI.display("An error occured while trying to send message to server");
    	}
	}
}
//End of ChatClient class
