
package com.aidn5.hypixelutils.v1.exceptions;

import com.aidn5.hypixelutils.v1.HypixelUtils;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * Indicates that the client is not connected to the hypixel network.
 * <br>
 * aka. {@link HypixelUtils#onHypixel()} is <code>false</code>
 * 
 * @author aidn5
 * 
 * @version 1.0
 * @since 1.0
 * 
 * @category Exception
 */
@IHypixelUtils
public class NotOnHypixelNetwork extends RuntimeException {}
