
package com.aidn5.hypixelutils.v1.exceptions;

import com.aidn5.hypixelutils.v1.common.annotation.IBackend;
import com.aidn5.hypixelutils.v1.common.annotation.IHypixelUtils;

/**
 * Indicates that the library has encountered an internal error.
 * 
 * 
 * @author aidn5
 * 
 * @version 1.0
 * @since 1.0
 * 
 * @category Exception
 */
@IHypixelUtils
@IBackend
public class HypixelUtilsInternalError extends RuntimeException {
  public HypixelUtilsInternalError() {
    super();
  }

  public HypixelUtilsInternalError(String message) {
    super(message);
  }

  public HypixelUtilsInternalError(String message, Throwable cause) {
    super(message, cause);
  }

  public HypixelUtilsInternalError(Throwable cause) {
    super(cause);
  }
}
