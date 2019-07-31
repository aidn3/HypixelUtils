
package com.aidn5.hypixelutils.v1.chatsocket.protocols;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;
import com.aidn5.hypixelutils.v1.tools.ReflectionUtil;

/**
 * Registry holds all the protocols, which are responsible for sending and
 * receiving packets.
 * 
 * @author aidn5
 *
 * @since 1.0
 */
@IHypixelUtils
@IBackend
public class ProtocolsRegistry {
  @Nonnull
  private static final HashMap<Class<? extends BaseProtocol>, BaseProtocol> protocols = 
      new HashMap<>();

  /**
   * Register this protocol and initiate it to send and receive packets.
   * 
   * @param protocolClass
   *          the class of the protocol to initiate.
   * 
   * @throws ReflectiveOperationException
   *           if any Reflection error occurs like Not having an empty
   *           constructor.
   */
  public static void registerNewProtocol(@Nonnull Class<? extends BaseProtocol> protocolClass)
      throws ReflectiveOperationException {
    try {
      BaseProtocol newProtocol = ReflectionUtil.newInstance(Objects.requireNonNull(protocolClass));
      protocols.put(protocolClass, newProtocol);

    } catch (ReflectiveOperationException e) {
      throw new ReflectiveOperationException(
          "Are you sure the protocol "
              + protocolClass.getName() + " has an empty constructor?",
          e);
    }
  }

  /**
   * Get all the instances of the protocols.
   * 
   * @return
   *         all the instances of the protocols.
   */
  static Set<BaseProtocol> getProtocols() {
    Set<BaseProtocol> protocolsSet = new HashSet<>();

    for (Entry<Class<? extends BaseProtocol>, BaseProtocol> protocol : protocols.entrySet()) {
      if (protocol != null) {
        protocolsSet.add(protocol.getValue());
      }
    }

    return protocolsSet;
  }
}
