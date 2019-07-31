
package com.aidn5.hypixelutils.v1.chatsocket.packets;

import java.util.HashMap;
import java.util.Map.Entry;

import com.aidn5.hypixelutils.v1.chatsocket.client.Connection;
import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.exceptions.HypixelUtilsInternalError;
import com.aidn5.hypixelutils.v1.tools.ReflectionUtil;

/**
 * Class contains all the registered packets for the current connection. Every
 * connection has its own registry. The registry can be used to register custom
 * packets.
 * 
 * <p>
 * The registered packets are based on
 * {@link Short} value as the key and the {@link Class} as the value.
 * 
 * <p>
 * Use {@link #addPacket(short, Class)} to register new packet. All the packets
 * must be extended from {@link BasePacket} and must fulfill all its conditions.
 * The registry can be retrieved to register the new packets by creating a new
 * connection and calling {@link Connection#getPacketsRegistry()}. The custom
 * packets will be ignored by default. To receive and process them, set a custom
 * {@link IPacketReceiver} by
 * {@link Connection#setCustomPacketReceiver(IPacketReceiver)}.
 * 
 * @author aidn5
 *
 * @since 1.0
 * 
 * @see IPacketReceiver
 * @see BasePacket
 */
@IHypixelUtils
public class PacketsRegistry {
  private final HashMap<Short, Class<? extends BasePacket>> packets = new HashMap<>();

  {
    addPacket((short) 1, ProtocolPacket.class);
    addPacket((short) 2, DataPacket.class);
    addPacket((short) 3, KeepAlivePacket.class);
  }

  /**
   * Get the associated packet's class with the key code.
   * 
   * @param code
   *          the key to use to look up for the packet.
   * @return
   *         the class of the packet.
   * @throws IllegalArgumentException
   *           if the packet is not registered in the registry.
   */
  @IBackend
  public Class<? extends BasePacket> getPacket(short code) throws IllegalArgumentException {
    Class<? extends BasePacket> packet = packets.get(code);

    if (packet == null) {
      throw new IllegalArgumentException("packet with the code " + code + " is not registed.");
    }

    return packet;
  }

  /**
   * get the key of the given packet.
   * 
   * @param packet
   *          the packet to look up for.
   * @return
   *         the associated key for the packet.
   * 
   * @throws IllegalArgumentException
   *           if the packet is not registered in the registry.
   */
  @IBackend
  public short getPacketKey(Class<? extends BasePacket> packet)
      throws IllegalArgumentException {
    for (Entry<Short, Class<? extends BasePacket>> packetEntry : packets.entrySet()) {
      if (packet.equals(packetEntry.getValue())) {
        return packetEntry.getKey();
      }
    }

    throw new IllegalArgumentException(
        "packet " + packet.getName() + " is not registered in the registry");
  }

  /**
   * Add a new packet to the registry.
   * 
   * @param key
   *          the associated code to the packet. must be unique.
   * @param packetClass
   *          the packet to register.
   * @throws IllegalArgumentException
   *           if the key/packet is already registered.
   */
  public void addPacket(short key, Class<? extends BasePacket> packetClass)
      throws IllegalArgumentException {

    Class<?> packet = packets.get(key);

    for (Entry<Short, Class<? extends BasePacket>> packetEntry : packets.entrySet()) {
      if (packetEntry.getKey() == key) {
        throw new IllegalArgumentException(
            "key " + key + " is already used for another packet " + packet.getName());
      }
      if (packetEntry.getValue().equals(packetClass)) {
        throw new IllegalArgumentException(
            "packet " + packetClass.getName()
                + " is already registerd with the key " + packetEntry.getKey());
      }
    }

    packets.put(key, packetClass);
  }

  /**
   * Create the packet from the given data.
   * 
   * @param packetClass
   *          the class to use as a wrapper for the data.
   * @param packetData
   *          packet's data to fill with.
   * @return the created packet.
   */
  @IBackend
  public static <T extends BasePacket> T getPacket(Class<T> packetClass, byte[] packetData) {
    try {
      T packet = ReflectionUtil.newInstance(packetClass);
      packet.readData(packetData);
      return packet;
    } catch (ReflectiveOperationException e) {
      throw new HypixelUtilsInternalError("The packet does not have an empty constructor.", e);
    }
  }
}
