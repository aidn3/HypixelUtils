/**
 * Copyright (C) 2013-2018 Steveice10
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
/**
 * Wrappers help reading and writing data to {@link java.io.InputStream},
 * {@link java.io.OutputStream} and {@link java.nio.ByteBuffer}. It support
 * Convenient methods to spare the hassle of converting the data to bytes and to
 * save bandwidth by writing only necessary bytes. Do note that both the sender
 * and receiver should use the wrappers. Otherwise errors may start to rise.
 *
 * <p>
 * These classes has been copied from "https://github.com/Steveice10/PacketLib"
 * 
 * @see "https://github.com/Steveice10/PacketLib"
 */

package com.aidn5.hypixelutils.v1.tools.io;
