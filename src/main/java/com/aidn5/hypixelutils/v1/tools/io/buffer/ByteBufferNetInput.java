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

package com.aidn5.hypixelutils.v1.tools.io.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import com.aidn5.hypixelutils.v1.tools.io.NetInput;

/**
 * A NetInput implementation using a ByteBuffer as a backend.
 * 
 * @since 1.0
 * 
 * @see "https://github.com/Steveice10/PacketLib"
 */
public class ByteBufferNetInput implements NetInput {
  private ByteBuffer buffer;

  public ByteBufferNetInput(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  public ByteBuffer getByteBuffer() {
    return this.buffer;
  }

  @Override
  public boolean readBoolean() throws IOException {
    return this.buffer.get() == 1;
  }

  @Override
  public byte readByte() throws IOException {
    return this.buffer.get();
  }

  @Override
  public int readUnsignedByte() throws IOException {
    return this.buffer.get() & 0xFF;
  }

  @Override
  public short readShort() throws IOException {
    return this.buffer.getShort();
  }

  @Override
  public int readUnsignedShort() throws IOException {
    return this.buffer.getShort() & 0xFFFF;
  }

  @Override
  public char readChar() throws IOException {
    return this.buffer.getChar();
  }

  @Override
  public int readInt() throws IOException {
    return this.buffer.getInt();
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
    return this.buffer.getLong();
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
    return this.buffer.getFloat();
  }

  @Override
  public double readDouble() throws IOException {
    return this.buffer.getDouble();
  }

  @Override
  public byte[] readBytes(int length) throws IOException {
    if (length < 0) {
      throw new IllegalArgumentException("Array cannot have length less than 0.");
    }

    byte[] b = new byte[length];
    this.buffer.get(b);
    return b;
  }

  @Override
  public int readBytes(byte[] b) throws IOException {
    return this.readBytes(b, 0, b.length);
  }

  @Override
  public int readBytes(byte[] b, int offset, int length) throws IOException {
    int readable = this.buffer.remaining();
    if (readable <= 0) {
      return -1;
    }

    if (readable < length) {
      length = readable;
    }

    this.buffer.get(b, offset, length);
    return length;
  }

  @Override
  public short[] readShorts(int length) throws IOException {
    if (length < 0) {
      throw new IllegalArgumentException("Array cannot have length less than 0.");
    }

    short[] s = new short[length];
    for (int index = 0; index < length; index++) {
      s[index] = this.readShort();
    }

    return s;
  }

  @Override
  public int readShorts(short[] s) throws IOException {
    return this.readShorts(s, 0, s.length);
  }

  @Override
  public int readShorts(short[] s, int offset, int length) throws IOException {
    int readable = this.buffer.remaining();
    if (readable <= 0) {
      return -1;
    }

    if (readable < length * 2) {
      length = readable / 2;
    }

    for (int index = offset; index < offset + length; index++) {
      s[index] = this.readShort();
    }

    return length;
  }

  @Override
  public int[] readInts(int length) throws IOException {
    if (length < 0) {
      throw new IllegalArgumentException("Array cannot have length less than 0.");
    }

    int[] i = new int[length];
    for (int index = 0; index < length; index++) {
      i[index] = this.readInt();
    }

    return i;
  }

  @Override
  public int readInts(int[] i) throws IOException {
    return this.readInts(i, 0, i.length);
  }

  @Override
  public int readInts(int[] i, int offset, int length) throws IOException {
    int readable = this.buffer.remaining();
    if (readable <= 0) {
      return -1;
    }

    if (readable < length * 4) {
      length = readable / 4;
    }

    for (int index = offset; index < offset + length; index++) {
      i[index] = this.readInt();
    }

    return length;
  }

  @Override
  public long[] readLongs(int length) throws IOException {
    if (length < 0) {
      throw new IllegalArgumentException("Array cannot have length less than 0.");
    }

    long[] l = new long[length];
    for (int index = 0; index < length; index++) {
      l[index] = this.readLong();
    }

    return l;
  }

  @Override
  public int readLongs(long[] l) throws IOException {
    return this.readLongs(l, 0, l.length);
  }

  @Override
  public int readLongs(long[] l, int offset, int length) throws IOException {
    int readable = this.buffer.remaining();
    if (readable <= 0) {
      return -1;
    }

    if (readable < length * 2) {
      length = readable / 2;
    }

    for (int index = offset; index < offset + length; index++) {
      l[index] = this.readLong();
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
    return this.buffer.remaining();
  }
}
