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

package com.aidn5.hypixelutils.v1.tools.io.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import com.aidn5.hypixelutils.v1.tools.io.NetOutput;

/**
 * A NetOutput implementation using an OutputStream as a backend.
 * 
 * @since 1.0
 * 
 * @see "https://github.com/Steveice10/PacketLib"
 */
public class StreamNetOutput implements NetOutput {
  private OutputStream out;

  /**
   * Creates a new StreamNetOutput instance.
   *
   * @param out
   *          OutputStream to write to.
   */
  public StreamNetOutput(OutputStream out) {
    this.out = out;
  }

  @Override
  public void writeBoolean(boolean b) throws IOException {
    this.writeByte(b ? 1 : 0);
  }

  @Override
  public void writeByte(int b) throws IOException {
    this.out.write(b);
  }

  @Override
  public void writeShort(int s) throws IOException {
    this.writeByte((byte) ((s >>> 8) & 0xFF));
    this.writeByte((byte) ((s >>> 0) & 0xFF));
  }

  @Override
  public void writeChar(int c) throws IOException {
    this.writeByte((byte) ((c >>> 8) & 0xFF));
    this.writeByte((byte) ((c >>> 0) & 0xFF));
  }

  @Override
  public void writeInt(int i) throws IOException {
    this.writeByte((byte) ((i >>> 24) & 0xFF));
    this.writeByte((byte) ((i >>> 16) & 0xFF));
    this.writeByte((byte) ((i >>> 8) & 0xFF));
    this.writeByte((byte) ((i >>> 0) & 0xFF));
  }

  @Override
  public void writeVarInt(int i) throws IOException {
    while ((i & ~0x7F) != 0) {
      this.writeByte((i & 0x7F) | 0x80);
      i >>>= 7;
    }

    this.writeByte(i);
  }

  @Override
  public void writeLong(long l) throws IOException {
    this.writeByte((byte) (l >>> 56));
    this.writeByte((byte) (l >>> 48));
    this.writeByte((byte) (l >>> 40));
    this.writeByte((byte) (l >>> 32));
    this.writeByte((byte) (l >>> 24));
    this.writeByte((byte) (l >>> 16));
    this.writeByte((byte) (l >>> 8));
    this.writeByte((byte) (l >>> 0));
  }

  @Override
  public void writeVarLong(long l) throws IOException {
    while ((l & ~0x7F) != 0) {
      this.writeByte((int) (l & 0x7F) | 0x80);
      l >>>= 7;
    }

    this.writeByte((int) l);
  }

  @Override
  public void writeFloat(float f) throws IOException {
    this.writeInt(Float.floatToIntBits(f));
  }

  @Override
  public void writeDouble(double d) throws IOException {
    this.writeLong(Double.doubleToLongBits(d));
  }

  @Override
  public void writeBytes(byte[] b) throws IOException {
    this.writeBytes(b, b.length);
  }

  @Override
  public void writeBytes(byte[] b, int length) throws IOException {
    this.out.write(b, 0, length);
  }

  @Override
  public void writeShorts(short[] s) throws IOException {
    this.writeShorts(s, s.length);
  }

  @Override
  public void writeShorts(short[] s, int length) throws IOException {
    for (int index = 0; index < length; index++) {
      this.writeShort(s[index]);
    }
  }

  @Override
  public void writeInts(int[] i) throws IOException {
    this.writeInts(i, i.length);
  }

  @Override
  public void writeInts(int[] i, int length) throws IOException {
    for (int index = 0; index < length; index++) {
      this.writeInt(i[index]);
    }
  }

  @Override
  public void writeLongs(long[] l) throws IOException {
    this.writeLongs(l, l.length);
  }

  @Override
  public void writeLongs(long[] l, int length) throws IOException {
    for (int index = 0; index < length; index++) {
      this.writeLong(l[index]);
    }
  }

  @Override
  public void writeString(String s) throws IOException {
    if (s == null) {
      throw new IllegalArgumentException("String cannot be null!");
    }

    byte[] bytes = s.getBytes("UTF-8");
    if (bytes.length > 32767) {
      throw new IOException(
          "String too big (was " + s.length() + " bytes encoded, max " + 32767 + ")");
    }

    this.writeVarInt(bytes.length);
    this.writeBytes(bytes);
  }

  @Override
  public void writeUuid(UUID uuid) throws IOException {
    this.writeLong(uuid.getMostSignificantBits());
    this.writeLong(uuid.getLeastSignificantBits());
  }

  @Override
  public void flush() throws IOException {
    this.out.flush();
  }
}
