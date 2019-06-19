/**
 * Copyright 2019 robere2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aidn5.hypixelutils.v1.tools;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Class to delay code by a certain number of game ticks.
 * <p>
 * <b>Note:</b> This class is licensed under the Apache License 2.0
 * 
 * 
 * @since 1.0
 * @category Utils
 * @version 1.0
 * 
 * @author robere2
 * @link https://github.com/robere2/QuickPlay/blob/master/src/main/java/co/bugg/quickplay/util/TickDelay.java
 * @license https://github.com/robere2/QuickPlay/blob/master/LICENSE
 */
public class TickDelay {

  /**
   * Constructor.
   * 
   * @param fn
   *          Code to be delayed
   * @param ticks
   *          How many ticks to delay it
   * 
   * @since 1.0
   */
  public TickDelay(@Nonnull Runnable fn, int ticks) {
    this.fn = Objects.requireNonNull(fn);
    this.delay = ticks;

    MinecraftForge.EVENT_BUS.register(this);
  }

  /**
   * Default 20 ticks when unprovided.
   * 
   * @param fn
   *          Code to be delayed
   * 
   * @since 1.0
   */
  public TickDelay(@Nonnull Runnable fn) {
    this(fn, 20);
  }

  @Nonnull
  private Runnable fn;

  private int delay;

  /**
   * reduce {@link #delay} by 1 and
   * check whether the ticks are finished
   * and the {@link #fn} should be executed.
   * 
   * @param event
   *          the received event
   */
  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    // Delay expired
    if (delay < 1) {
      run();
      destroy();
    }
    delay--;
  }

  private void run() {
    fn.run();
  }

  /**
   * cancel and destroy the code block.
   * 
   * @since 1.0
   */
  public void destroy() {
    MinecraftForge.EVENT_BUS.unregister(this);
  }
}
