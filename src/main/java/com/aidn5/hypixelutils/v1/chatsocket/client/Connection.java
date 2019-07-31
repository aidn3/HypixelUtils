
package com.aidn5.hypixelutils.v1.chatsocket.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.aidn5.hypixelutils.v1.chatsocket.packets.BasePacket;
import com.aidn5.hypixelutils.v1.chatsocket.packets.DataPacket;
import com.aidn5.hypixelutils.v1.chatsocket.packets.IPacketReceiver;
import com.aidn5.hypixelutils.v1.chatsocket.packets.PacketsRegistry;
import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IChatWrapper;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.tools.TimeOut;
import com.aidn5.hypixelutils.v1.tools.io.NetInput;
import com.aidn5.hypixelutils.v1.tools.io.NetOutput;
import com.aidn5.hypixelutils.v1.tools.io.stream.StreamNetInput;
import com.aidn5.hypixelutils.v1.tools.io.stream.StreamNetOutput;

import scala.Char;

/**
 * Protocol, which used to communicate with the other end.
 * 
 * @author aidn5
 *
 * @since 1.0
 */
@IHypixelUtils
@IChatWrapper(usesLock = false)
public class Connection implements IPacketReceiver {
  @Nonnull
  private final ChatSocket parentConnection;

  @Nonnull
  private final TimeOut timeout;

  @Nonnull
  private final InputStreamChatSocket is = new InputStreamChatSocket();
  @Nonnull
  private final NetInput StreamNetInput = new StreamNetInput(is);
  // 30 bufferSize is used, because of chat limitations
  @Nonnull
  private final OutputStreamChatSocket os = new OutputStreamChatSocket(30);
  @Nonnull
  private final NetOutput streamNetOutput = new StreamNetOutput(os);

  @Nullable
  private IPacketReceiver customPacketReceiver = null;
  @Nullable
  private Runnable onTimeOut = null;

  private boolean connectionTimedOut = false;
  private boolean forceKeepAlive = false;

  @IBackend
  Connection(@Nonnull ChatSocket parent) {
    this.parentConnection = Objects.requireNonNull(parent);

    timeout = new TimeOut(() -> {
      connectionTimedOut = true;

      try {
        parentConnection.closeConnection();
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (onTimeOut != null) {
        onTimeOut.run();
      }
    });

    final Thread forceKeepAliveThread = new Thread(() -> {
      try {
        while (true) {
          Thread.sleep(10);
          if (forceKeepAlive) {
            if (parentConnection.getLastTimeSentPacket() + timeout.getTimeOut() < System
                .currentTimeMillis() + 10000) {
              sendKeepAlive();
            }
          }
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
        Thread.currentThread().interrupt();
        return;
      }
    });

    forceKeepAliveThread.setDaemon(true);
    forceKeepAliveThread.start();
  }

  /**
   * id used to verify the messages between the two clients.
   * This id is randomly generated when the connection request initiated.
   * It is sent at the start of every packet and used for
   * the rest of the session (connection) or till timeout is flagged.
   * 
   * @return
   *         the used hash in the connection.
   */
  public int getConnectionId() {
    return parentConnection.connectionId;
  }

  /**
   * get the user, who at the other end of the connection.
   * Used as a destination for the packets.
   * 
   * @return
   *         the user, who is at the other end of the connection.
   */
  @Nonnull
  public String getUser() {
    return parentConnection.user;
  }

  /**
   * the id of the programs which are trying to communicate with each other.
   *
   * @return
   *         the id of the program.
   */
  @Nonnull
  public String getId() {
    return parentConnection.id;
  }

  /**
   * an extra field for the program.
   * used to define their intends from each other.
   *
   * @return
   *         the extra field.
   */
  @Nonnull
  public String getActionId() {
    return parentConnection.actionId;
  }

  /**
   * Get the class which controls the timeout.
   * 
   * @return
   *         the timeout controller.
   */
  @IBackend
  @Nonnull
  TimeOut getTimeout() {
    return timeout;
  }

  /**
   * set a listener to be called when the connection times out.
   */
  public void setOnTimeOutListener(@Nullable Runnable onTimeOut) {
    this.onTimeOut = onTimeOut;
  }

  /**
   * return a stream, which can used to receive data from the other end.
   * 
   * @return
   *         the stream, which receives data from the other end.
   * 
   * @see #getStreamNetInput()
   */
  @Nonnull
  public InputStream getInputStream() {
    return is;
  }

  /**
   * get the stream, which used to send data to the other end.<br>
   * <i>do {@link OutputStream#close()} at the end of stream to indicates the end
   * of the data.
   * Otherwise, the InputStream {@link #getInputStream()} from the other end of
   * the connection will not be able to inform the user.</i>
   * 
   * @return
   *         the output stream of this connection.
   * 
   * @throws IOException
   *           if the connection is closed.
   * 
   * @see #getStreamNetOutput()
   */
  @Nonnull
  public OutputStream getOutputStream() throws IOException {
    if (connectionClosed()) {
      throw new IOException("Connection closed");
    }

    return os;
  }

  /**
   * Get a wrapped {@link InputStream}, which ease reading of data, like
   * reading {@link Long}, {@link Char}, etc. from {@link Byte}.
   * 
   * @return
   *         a wrapped {@link InputStream} with a lot of convenient methods.
   * 
   * @see #getInputStream()
   */
  @Nonnull
  public NetInput getStreamNetInput() {
    return StreamNetInput;
  }

  /**
   * Get a wrapped {@link OutputStream}, which ease writing data, like writing
   * {@link Long}, {@link Char}, {@link UUID} to the stream as {@link Byte}.
   * 
   * @return
   *         a wrapped {@link OutputStream} with a lot of convenient methods.
   * 
   * @throws IOException
   *           if the connection is closed.
   * 
   * @see #getOutputStream()
   */
  @Nonnull
  public NetOutput getStreamNetOutput() throws IOException {
    if (connectionClosed()) {
      throw new IOException("Connection closed");
    }

    return streamNetOutput;
  }

  /**
   * Set custom packet receiver to receive and process the custom packets that are
   * registered by {@link PacketsRegistry}.
   * 
   * @param icpc
   *          the receiver to send the packets to.
   * 
   * @see #getPacketsRegistry()
   */
  public void setCustomPacketReceiver(@Nullable IPacketReceiver icpc) {
    this.customPacketReceiver = icpc;
  }

  /**
   * Get the packet registry to register custom packets, which can be used with
   * the custom packet receiver.
   * 
   * @return
   *         the packets registry that can be used to register new packets.
   * 
   * @see #setCustomPacketReceiver(IPacketReceiver)
   */
  @Nonnull
  public PacketsRegistry getPacketsRegistry() {
    return parentConnection.packetsRegistry;
  }

  /**
   * Flush the {@link OutputStream} from {@link #getOutputStream()} and then try
   * to close the connection. This method has no effect, if the connection is
   * closed {@link Connection#connectionClosed()} already.
   * 
   * @throws IOException
   *           if an error occurs while trying to {@link OutputStream#flush()}
   *           before closing the connection.
   */
  public void closeConnection() throws IOException {
    if (connectionClosed()) {
      return;
    }

    os.flush();
    parentConnection.closeConnection();
  }

  /**
   * Return whether the connection is already closed.
   * 
   * @return
   *         <code>true</code> if the connection is already closed/timed-out.
   * 
   * @see #connectionTimedOut()
   */
  public boolean connectionClosed() {
    return parentConnection.isConnectionClosed();
  }

  /**
   * Check whether the connection is closed, because of timing out.
   * 
   * @return
   *         <code>true</code> if connection timed-out and closed.
   * 
   * @see #connectionClosed()
   */
  public boolean connectionTimedOut() {
    return connectionClosed() && connectionTimedOut;
  }

  /**
   * ping the other end to prevent the connection from timing out.
   */
  public void sendKeepAlive() {
    parentConnection.sendConnectionKeepAlivePacket(true);
  }

  /**
   * Check whether the connection should be prevent from timing out after
   * inactivity.
   * 
   * @return
   *         <code>true</code> if the connection should be prevent from timing out
   *         after inactivity.
   */
  public boolean forceKeepAlive() {
    return forceKeepAlive;
  }

  /**
   * Prevent the connection from timing out after inactivity.
   * 
   * <p>
   * <b>This can cause problems, if it is used without further thoughts.</b>
   * 
   * @param forceKeepAlive
   *          whether the connection should not time out after inactivity.
   */
  public void setForceKeepAlive(boolean forceKeepAlive) {
    this.forceKeepAlive = forceKeepAlive;
  }

  /**
   * Process the received packet for this instance.
   * <p>
   * <b><i>This is a backend method. DO NOT USE IT</i></b>
   * 
   * @param packet
   *          the packet to process for this instance.
   */
  @Override
  @IBackend
  public <T extends BasePacket> void packetReceived(T packet) {
    if (packet instanceof DataPacket) {
      DataPacket dataPacket = (DataPacket) packet;

      for (byte b : dataPacket.getRawData()) {
        is.bytes.add(b);
      }
      if (dataPacket.isAtEnd()) {
        is.atEnd = true;
      }

    } else if (customPacketReceiver != null) {
      customPacketReceiver.packetReceived(packet);

    } else {
      throw new RuntimeException(
          "There is no processer that can process this packet " + packet.getClass().getName()
              + ". Packet ignored.");
    }
  }

  @IBackend
  @IHypixelUtils
  public class InputStreamChatSocket extends InputStream {
    private final List<Byte> bytes = new ArrayList<>();
    private boolean atEnd = false;

    @Override
    public int read() throws IOException {
      while (bytes.size() == 0 && !atEnd && !connectionClosed()) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          e.printStackTrace();
          Thread.currentThread().interrupt();
          throw new IOException("read interrupted", e);
        }
      }

      // TODO: send packet to the user about the reading every multiple times
      // to keep up the connection alive

      if (bytes.size() > 0) {
        return bytes.remove(0);
      }

      if (atEnd) {
        return -1;
      }

      if (connectionTimedOut()) {
        throw new IOException("Connection timed out.");
      }

      if (connectionClosed()) {
        throw new IOException("Connection closed");
      }

      // will this even happen?
      throw new IOException("InputStream: Bug has been found. please, Report this.");
    }

    /**
     * Flush the {@link OutputStream} from {@link #getOutputStream()} and then try
     * to close the connection. This method has no effect, if the connection is
     * closed {@link Connection#connectionClosed()} already.
     * 
     * <p>
     * This method calls {@link Connection#closeConnection()}.
     * 
     * @throws IOException
     *           if an error occurs while trying to {@link OutputStream#flush()}
     *           before closing the connection.
     * 
     * @see Connection#closeConnection()
     */
    @Override
    public void close() throws IOException {
      closeConnection();
    }

    @Override
    public int available() throws IOException {
      return bytes.size();
    }
  }

  @IBackend
  @IHypixelUtils
  public class OutputStreamChatSocket extends OutputStream {
    private final int bufferSize;
    private byte[] buff;
    private int currentPointer = 0;

    private boolean atEnd = false;

    private OutputStreamChatSocket(int bufferSize) {
      if (bufferSize <= 0) {
        throw new IllegalArgumentException("bufferSize must be bigger than null");
      }

      this.bufferSize = bufferSize;
      buff = new byte[bufferSize];
    }

    /**
     * Flush the {@link OutputStream} from {@link #getOutputStream()} and then try
     * to close the connection. This method has no effect, if the connection is
     * closed {@link Connection#connectionClosed()} already.
     * 
     * <p>
     * This method calls {@link Connection#closeConnection()}.
     * 
     * @throws IOException
     *           if an error occurs while trying to {@link OutputStream#flush()}
     *           before closing the connection.
     * 
     * @see Connection#closeConnection()
     */
    @Override
    public void close() throws IOException {
      closeConnection();
    }

    /**
     * Writes the specified byte to this output stream.
     * write {@code -1} to indicate the end of the data and to flush the current
     * buffer. After indicating the end of the data, any followed data will throw
     * {@link IOException}.
     */
    @Override
    public synchronized void write(int b) throws IOException {
      if (connectionClosed()) {
        throw new IOException("Connection closed");
      }
      if (atEnd) {
        throw new IOException("Can not write anymore data. -1 has already been written.");
      }

      if (b == -1) {
        atEnd = true;
        flush();
        return;
      }

      if (currentPointer >= buff.length) {
        flush();
      }

      buff[currentPointer++] = (byte) b;
    }

    @Override
    public synchronized void flush() throws IOException {
      if (!parentConnection.isConnectionOpened()) {
        throw new IOException("Connection not opened yet to send data.");
      }

      if (currentPointer == 0) {
        return;
      }

      byte[] dataToSend = new byte[currentPointer];
      System.arraycopy(buff, 0, dataToSend, 0, currentPointer);

      buff = new byte[bufferSize];
      currentPointer = 0;

      try {
        parentConnection.sendPacket(new DataPacket(dataToSend, -1, -1, atEnd));

      } catch (Exception e) {
        throw new IOException("Could not flush the output stream of the connection.", e);
      }
    }
  }
}
