
package com.aidn5.hypixelutils.v1.chatsocket;

import java.io.IOException;

import com.aidn5.hypixelutils.v1.chatsocket.client.Connection;
import com.aidn5.hypixelutils.v1.chatsocket.client.IResponseRequest;
import com.aidn5.hypixelutils.v1.chatsocket.client.RequestReceiveEvent;
import com.aidn5.hypixelutils.v1.chatsocket.client.RequestReceiveEvent.RequestReceived;
import com.aidn5.hypixelutils.v1.chatsocket.client.RequestSendEvent;
import com.aidn5.hypixelutils.v1.chatsocket.client.RequestSendEvent.RequestResponse;
import com.aidn5.hypixelutils.v1.tools.io.NetInput;
import com.aidn5.hypixelutils.v1.tools.io.NetOutput;

/**
 * <b>Full example to ChatSocket:</b><br>
 * <i>This example class is copy-pasted as a documentation to the class itself
 * to allow reading the example from javadoc.</i>
 * 
 * <pre>
 *  <code>
class ExampleChatSocket implements RequestReceived, IResponseRequest {
  private static final String idToListenTo = "testMod123";
  private static final String action = "DoStuff";

  public ExampleChatSocket() {
    // register a new listener to receive connections requests
    ChatSocketFactory.registerListener(idToListenTo, this);

    // create connection request.
    RequestSendEvent r = ChatSocketFactory.createRequest(idToListenTo, action);
    // send connection request
    r.sendNewRequest("UsernameToSendTo", this);
  }

  // handle responses about outgoing connection requests.
  &#64;Override
  public void response(RequestResponse rr, Connection ct) {
    if (rr == RequestResponse.TIMED_OUT) {
      System.out.println("connection timed-out");
      return;

    } else if (rr == RequestResponse.REJECTED) {
      System.out.println("connection has been rejected");
      return;
    }

    // connection has been accepted
    // and ready to send and receive data
    try {
      // InputStream is = cp.getInputStream();
      NetInput is = ct.getStreamNetInput();

      System.out.println("receiving data");
      System.out.println(is.readString());

      ct.closeConnection();

    } catch (IOException e) {
      e.printStackTrace();
      // connection will automatically be closed
      // after inactivity of 30 seconds
    }
  }

  // handle incoming connections requests
  &#64;Override
  public void get(RequestReceiveEvent re) {
    if (!re.canSend()) {
      // notify the user, this request can not be responded to.
      System.out.println("Connection can not be responded to");
    }

    if (!re.getActionId().equals(action)) {
      // notify the user, this is an invalid action.
      System.out.println("Unknown command to handle.");
      System.out.println("Rejecting teh request...");
      re.declineConnection();
      return;
    }

    System.out.println("accepting the connection...");
    Connection ct = re.acceptConnection();

    try {
      System.out.println("sending data...");

      // OutputStream os = ct.getOutputStream();
      NetOutput no = ct.getStreamNetOutput();
      no.writeString("Hello World!");

    } catch (IOException e) {
      e.printStackTrace();

    } finally {
      System.out.println("Closing the connection...");

      try {
        ct.closeConnection();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Even if closing the connection failed,
    // connection will automatically
    // time-out after 30 seconds of inactivity
  }
}
</code>
 * </pre>
 */
public class ExampleChatSocket implements RequestReceived, IResponseRequest {
  private static final String idToListenTo = "testMod123";
  private static final String action = "DoStuff";

  private ExampleChatSocket() {
    // register a new listener to receive connections requests
    ChatSocketFactory.registerListener(idToListenTo, this);

    // create connection request.
    RequestSendEvent r = ChatSocketFactory.createRequest(idToListenTo, action);
    // send connection request
    r.sendNewRequest("UsernameToSendTo", this);
  }

  // handle responses about outgoing connection requests.
  @Override
  public void response(RequestResponse rr, Connection ct) {
    if (rr == RequestResponse.TIMED_OUT) {
      System.out.println("connection timed-out");
      return;

    } else if (rr == RequestResponse.REJECTED) {
      System.out.println("connection has been rejected");
      return;
    }

    // connection has been accepted
    // and ready to send and receive data
    try {
      // InputStream is = cp.getInputStream();
      NetInput is = ct.getStreamNetInput();

      System.out.println("receiving data");
      System.out.println(is.readString());

      ct.closeConnection();

    } catch (IOException e) {
      e.printStackTrace();
      // connection will automatically be closed
      // after inactivity of 30 seconds
    }
  }

  // handle incoming connections requests
  @Override
  public void get(RequestReceiveEvent re) {
    if (!re.canSend()) {
      // notify the user, this request can not be responded to.
      System.out.println("Connection can not be responded to");
    }

    if (!re.getActionId().equals(action)) {
      // notify the user, this is an invalid action.
      System.out.println("Unknown command to handle.");
      System.out.println("Rejecting teh request...");
      re.declineConnection();
      return;
    }

    System.out.println("accepting the connection...");
    Connection ct = re.acceptConnection();

    try {
      System.out.println("sending data...");

      // OutputStream os = ct.getOutputStream();
      NetOutput no = ct.getStreamNetOutput();
      no.writeString("Hello World!");

    } catch (IOException e) {
      e.printStackTrace();

    } finally {
      System.out.println("Closing the connection...");

      try {
        ct.closeConnection();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Even if closing the connection failed,
    // connection will automatically
    // time-out after 30 seconds of inactivity
  }
}
