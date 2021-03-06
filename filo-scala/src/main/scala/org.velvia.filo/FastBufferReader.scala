package org.velvia.filo

import sun.misc.Unsafe
import java.nio.ByteBuffer

/**
 * TODO: use something like Agrona so the classes below will work with non-array-based ByteBuffers
 * Fast (machine-speed/intrinsic) readers for ByteBuffer values, assuming bytebuffers are vectors
 * of fixed size.
 */
object FastBufferReader {
  /**
   * Instantiates the correct BufferReader implementation:
   * - FastUnsafeArrayBufferReader is used if the ByteBuffer is backed by an array
   * - SlowBufferReader just uses ByteBuffer
   *
   * This allows future-proof implementations: for example for JDK9 / better Unsafe changes, and
   * for extending to DirectMemory allocated ByteBuffers, for example.
   *
   * @param buf the ByteBuffer containing an array of fixed values to wrap for fast access
   */
  def apply(buf: ByteBuffer): FastBufferReader = {
    if (buf.hasArray) { new FastUnsafeArrayBufferReader(buf) }
    else              { throw new RuntimeException("Cannot support this ByteBuffer") }
  }

  def apply(long: Long): FastBufferReader = new FastLongBufferReader(long)
}

object UnsafeUtils {
  val unsafe = scala.concurrent.util.Unsafe.instance

  val arayOffset = unsafe.arrayBaseOffset(classOf[Array[Byte]])

  /**
   * Generic methods to read and write data to any offset from a base object location.  Be careful, this
   * can easily crash the system!
   */
  final def getByte(obj: Any, offset: Long): Byte = unsafe.getByte(obj, offset)
  final def getShort(obj: Any, offset: Long): Short = unsafe.getShort(obj, offset)
  final def getInt(obj: Any, offset: Long): Int = unsafe.getInt(obj, offset)
  final def getLong(obj: Any, offset: Long): Long = unsafe.getLong(obj, offset)
  final def getDouble(obj: Any, offset: Long): Double = unsafe.getDouble(obj, offset)
  final def getFloat(obj: Any, offset: Long): Double = unsafe.getFloat(obj, offset)

  final def setByte(obj: Any, offset: Long, byt: Byte): Unit = unsafe.putByte(obj, offset, byt)
  final def setShort(obj: Any, offset: Long, s: Short): Unit = unsafe.putShort(obj, offset, s)
  final def setInt(obj: Any, offset: Long, i: Int): Unit = unsafe.putInt(obj, offset, i)
  final def setLong(obj: Any, offset: Long, l: Long): Unit = unsafe.putLong(obj, offset, l)
  final def setDouble(obj: Any, offset: Long, d: Double): Unit = unsafe.putDouble(obj, offset, d)
  final def setFloat(obj: Any, offset: Long, f: Float): Unit = unsafe.putFloat(obj, offset, f)
}

trait FastBufferReader {
  def readByte(i: Int): Byte
  def readShort(i: Int): Short
  def readInt(i: Int): Int
  def readLong(i: Int): Long
  def readDouble(i: Int): Double
  def readFloat(i: Int): Float
}

import UnsafeUtils._

class FastUnsafeArrayBufferReader(buf: ByteBuffer) extends FastBufferReader {
  val offset = arayOffset + buf.arrayOffset + buf.position
  val byteArray = buf.array()
  final def readByte(i: Int): Byte = unsafe.getByte(byteArray, (offset + i).toLong)
  final def readShort(i: Int): Short = unsafe.getShort(byteArray, (offset + i * 2).toLong)
  final def readInt(i: Int): Int = unsafe.getInt(byteArray, (offset + i * 4).toLong)
  final def readLong(i: Int): Long = unsafe.getLong(byteArray, (offset + i * 8).toLong)
  final def readDouble(i: Int): Double = unsafe.getDouble(byteArray, (offset + i * 8).toLong)
  final def readFloat(i: Int): Float = unsafe.getFloat(byteArray, (offset + i * 4).toLong)
}

class FastLongBufferReader(long: Long) extends FastBufferReader {
  def readByte(i: Int): Byte = (long >> (8 * i)).toByte
  def readShort(i: Int): Short = (long >> (16 * i)).toShort
  def readInt(i: Int): Int = (long >> (32 * i)).toInt
  def readLong(i: Int): Long = long
  def readDouble(i: Int): Double = ???
  def readFloat(i: Int): Float = ???
}
