package com.aidn5.hypixelutils.v1.exceptions;

import com.aidn5.hypixelutils.v1.HypixelUtils;

/**
 * Indicates that the client is not connected to the hpyixel network
 * <br>
 * aka. {@link HypixelUtils#onHypixel()} is <code>false</code>
 * 
 * @author aidn5
 * @version 1.0
 * @since 1.0
 * @category Exception
 */
public class NotOnHypixelNetwork extends RuntimeException {}
