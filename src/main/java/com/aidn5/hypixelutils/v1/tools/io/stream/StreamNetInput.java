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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.aidn5.hypixelutils.v1.tools.io.NetInput;

/**
 * A NetInput implementation using an InputStream as a backend.
 * 
 * @since 1.0
 * 
 * @see "https://github.com/Steveice10/PacketLib"
 */
public class StreamNetInput implements NetInput {
  private InputStream in;

  /**
   * Creates a new StreamNetInput instance.
   *
   * @param in
   *          InputStream to read from.
   */
  public StreamNetInput(InputStream in) {
    this.in = in;
  }

  @Override
  public boolean readBoolean() throws IOException {
    return this.readByte() == 1;
  }

  @Override
  public byte readByte() throws IOException {
    return (byte) this.readUnsignedByte();
  }

  @Override
  public int readUnsignedByte() throws IOException {
    int b = this.in.read();
    if (b < 0) {
      throw new EOFException();
    }

    return b;
  }

  @Override
  public short readShort() throws IOException {
    return (short) this.readUnsignedShort();
  }

  @Override
  public int readUnsignedShort() throws IOException {
    int ch1 = this.readUnsignedByte();
    int ch2 = this.readUnsignedByte();
    return (ch1 << 8) + (ch2 << 0);
  }

  @Override
  public char readChar() throws IOException {
    int ch1 = this.readUnsignedByte();
    int ch2 = this.readUnsignedByte();
    return (char) ((ch1 << 8) + (ch2 << 0));
  }

  @Override
  public int readInt() throws IOException {
    int ch1 = this.readUnsignedByte();
    int ch2 = this.readUnsignedByte();
    int ch3 = this.readUnsignedByte();
    int ch4 = this.readUnsignedByte();
    return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
  }

  @Override
  public int readVarInt() throws IOException {
    int value = 0;
    int size = 0;
    int b;
    while (((b = this.readByte()) & 0x80) == 0x80) {
      value |= (b & 0x7F) << (size++ * 7);
      if (size > 5) {
        throw new IOException("VarInt too long (length must be <= 5)");
      }
    }

    return value | ((b & 0x7F) << (size * 7));
  }

  @Override
  public long readLong() throws IOException {
    byte[] read = this.readBytes(8);
    return ((long) read[0] << 56) + ((long) (read[1] & 255) << 48) + ((long) (read[2] & 255) << 40)
        + ((long) (read[3] & 255) << 32) + ((long) (read[4] & 255) << 24) + ((read[5] & 255) << 16)
        + ((read[6] & 255) << 8) + ((read[7] & 255) << 0);
  }

  @Override
  public long readVarLong() throws IOException {
    long value = 0;
    int size = 0;
    int b;
    while (((b = this.readByte()) & 0x80) == 0x80) {
      value |= (long) (b & 0x7F) << (size++ * 7);
      if (size > 10) {
        throw new IOException("VarLong too long (length must be <= 10)");
      }
    }

    return value | ((long) (b & 0x7F) << (size * 7));
  }

  @Override
  public float readFloat() throws IOException {
    return Float.intBitsToFloat(this.readInt());
  }

  @Override
  public double readDouble() throws IOException {
    return Double.longBitsToDouble(this.readLong());
  }

  @Override
  public byte[] readBytes(int length) throws IOException {
    if (length < 0) {
      throw new IllegalArgumentException("Array cannot have length less than 0.");
    }

    byte[] b = new byte[length];
    int n = 0;
    while (n < length) {
      int count = this.in.read(b, n, length - n);
      if (count < 0) {
        throw new EOFException();
      }

      n += count;
    }

    return b;
  }

  @Override
  public int readBytes(byte[] b) throws IOException {
    return this.in.read(b);
  }

  @Override
  public int readBytes(byte[] b, int offset, int length) throws IOException {
    return this.in.read(b, offset, length);
  }

  @Override
  public short[] readShorts(int length) throws IOException {
    if (length < 0) {
      throw new IllegalArgumentException("Array cannot have length less than 0.");
    }

    short[] s = new short[length];
    int read = this.readShorts(s);
    if (read < length) {
      throw new EOFException();
    }

    return s;
  }

  @Override
  public int readShorts(short[] s) throws IOException {
    return this.readShorts(s, 0, s.length);
  }

  @Override
  public int readShorts(short[] s, int offset, int length) throws IOException {
    for (int index = offset; index < offset + length; index++) {
      try {
        s[index] = this.readShort();
      } catch (EOFException e) {
        return index - offset;
      }
    }

    return length;
  }

  @Override
  public int[] readInts(int length) throws IOException {
    if (length < 0) {
      throw new IllegalArgumentException("Array cannot have length less than 0.");
    }

    int[] i = new int[length];
    int read = this.readInts(i);
    if (read < length) {
      throw new EOFException();
    }

    return i;
  }

  @Override
  public int readInts(int[] i) throws IOException {
    return this.readInts(i, 0, i.length);
  }

  @Override
  public int readInts(int[] i, int offset, int length) throws IOException {
    for (int index = offset; index < offset + length; index++) {
      try {
        i[index] = this.readInt();
      } catch (EOFException e) {
        return index - offset;
      }
    }

    return length;
  }

  @Override
  public long[] readLongs(int length) throws IOException {
    if (length < 0) {
      throw new IllegalArgumentException("Array cannot have length less than 0.");
    }

    long[] l = new long[length];
    int read = this.readLongs(l);
    if (read < length) {
      throw new EOFException();
    }

    return l;
  }

  @Override
  public int readLongs(long[] l) throws IOException {
    return this.readLongs(l, 0, l.length);
  }

  @Override
  public int readLongs(long[] l, int offset, int length) throws IOException {
    for (int index = offset; index < offset + length; index++) {
      try {
        l[index] = this.readLong();
      } catch (EOFException e) {
        return index - offset;
      }
    }

    return length;
  }

  @Override
  public String readString() throws IOException {
    int length = this.readVarInt();
    byte[] bytes = this.readBytes(length);
    return new String(bytes, "UTF-8");
  }

  @Override
  public UUID readUuid() throws IOException {
    return new UUID(this.readLong(), this.readLong());
  }

  @Override
  public int available() throws IOException {
    return this.in.available();
  }
}
