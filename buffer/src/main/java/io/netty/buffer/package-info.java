/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/**
 * 字节缓冲区的抽象 - 基本的数据结构来表示二进制和文本数据。
 * Abstraction of a byte buffer - the fundamental data structure
 * to represent a low-level binary and text message.
 *
 * Netty 使用自己的缓冲区API代替NIO {@link java.nio.ByteBuffer} 来表示字节序列。
 * 这种方法比使用 {@link java.nio.ByteBuffer} 有显著优势。
 * Netty 的新缓冲区类型 {@link io.netty.buffer.ByteBuf}, 从设计上解决 {@link java.nio.ByteBuffer} 的问题并满足
 * 网络应用开发者的日常需求。
 * Netty uses its own buffer API instead of NIO {@link java.nio.ByteBuffer} to
 * represent a sequence of bytes. This approach has significant advantage over
 * using {@link java.nio.ByteBuffer}.  Netty's new buffer type,
 * {@link io.netty.buffer.ByteBuf}, has been designed from ground
 * up to address the problems of {@link java.nio.ByteBuffer} and to meet the
 * daily needs of network application developers.  To list a few cool features:
 * <ul>
 *   <li>You can define your buffer type if necessary.</li>
 *   通过内置的composite buffer类型实现透明的zero copy。
 *   <li>Transparent zero copy is achieved by built-in composite buffer type.</li>
 *   提供开箱即用的动态缓冲区类型, 其容量按需扩展, 就像 {@link java.lang.StringBuffer}。
 *   <li>A dynamic buffer type is provided out-of-the-box, whose capacity is
 *       expanded on demand, just like {@link java.lang.StringBuffer}.</li>
 *   <li>There's no need to call the {@code flip()} method anymore.</li>
 *   <li>It is often faster than {@link java.nio.ByteBuffer}.</li>
 * </ul>
 *
 * 可扩展性
 * <h3>Extensibility</h3>
 *
 * {@link io.netty.buffer.ByteBuf} 有丰富方法集用于快速操作。
 * 例如 {@link io.netty.buffer.ByteBuf} 提供多个方法用于访问无符号值和字符串、搜索特定字节。
 * 还可以扩展或包装现有的缓冲区类型添加便捷的操作。
 * 自定义缓冲区类型仍需实现 {@link io.netty.buffer.ByteBuf} 接口而不是引入不兼容的类型。
 * {@link io.netty.buffer.ByteBuf} has rich set of operations
 * optimized for rapid protocol implementation.  For example,
 * {@link io.netty.buffer.ByteBuf} provides various operations
 * for accessing unsigned values and strings and searching for certain byte
 * sequence in a buffer.  You can also extend or wrap existing buffer type
 * to add convenient accessors.  The custom buffer type still implements
 * {@link io.netty.buffer.ByteBuf} interface rather than
 * introducing an incompatible type.
 *
 * 透明零拷贝
 * <h3>Transparent Zero Copy</h3>
 *
 * 要将网络应用程序的性能提升到极致, 需要减少内存复制操作的次数。
 * 你可能有一组可以分片、重组以组成整个消息的缓冲区。 Netty提供了composite buffer, 允许创建新的buffer,
 * 包含任意数量的现有buffers, 而不需要内存拷贝。
 * 例如, 一个消息可以由两部分组成: 标题和正文。 在模块化应用程序中, 这两个部分可以由不同的模块产生,
 * 然后稍后在发送消息时组装。
 * To lift up the performance of a network application to the extreme, you need
 * to reduce the number of memory copy operation.  You might have a set of
 * buffers that could be sliced and combined to compose a whole message.  Netty
 * provides a composite buffer which allows you to create a new buffer from the
 * arbitrary number of existing buffers with no memory copy.  For example, a
 * message could be composed of two parts; header and body.  In a modularized
 * application, the two parts could be produced by different modules and
 * assembled later when the message is sent out.
 * <pre>
 * +--------+----------+
 * | header |   body   |
 * +--------+----------+
 * </pre>
 * If {@link java.nio.ByteBuffer} were used, you would have to create a new big
 * buffer and copy the two parts into the new buffer.   Alternatively, you can
 * perform a gathering write operation in NIO, but it restricts you to represent
 * the composite of buffers as an array of {@link java.nio.ByteBuffer}s rather
 * than a single buffer, breaking the abstraction and introducing complicated
 * state management.  Moreover, it's of no use if you are not going to read or
 * write from an NIO channel.
 * <pre>
 * // The composite type is incompatible with the component type.
 * ByteBuffer[] message = new ByteBuffer[] { header, body };
 * </pre>
 * By contrast, {@link io.netty.buffer.ByteBuf} does not have such
 * caveats because it is fully extensible and has a built-in composite buffer
 * type.
 * <pre>
 * // The composite type is compatible with the component type.
 * {@link io.netty.buffer.ByteBuf} message = {@link io.netty.buffer.Unpooled}.wrappedBuffer(header, body);
 *
 * // Therefore, you can even create a composite by mixing a composite and an
 * // ordinary buffer.
 * {@link io.netty.buffer.ByteBuf} messageWithFooter = {@link io.netty.buffer.Unpooled}.wrappedBuffer(message, footer);
 *
 * // Because the composite is still a {@link io.netty.buffer.ByteBuf}, you can access its content
 * // easily, and the accessor method will behave just like it's a single buffer
 * // even if the region you want to access spans over multiple components.  The
 * // unsigned integer being read here is located across body and footer.
 * messageWithFooter.getUnsignedInt(
 *     messageWithFooter.readableBytes() - footer.readableBytes() - 1);
 * </pre>
 *
 * 自动容量扩展
 * <h3>Automatic Capacity Extension</h3>
 *
 * 许多protocols定义了可变长度的消息, 这意味着直到构造消息前没有办法确定消息的长度, 或者它是
 * 计算精确长度困难且不便。
 * 这就像{@link java.lang.String} 一样, 你估计字符串长度, 并使用 {@link java.lang.StringBuffer}
 * 在需要的时候进行扩展。
 * Many protocols define variable length messages, which means there's no way to
 * determine the length of a message until you construct the message or it is
 * difficult and inconvenient to calculate the length precisely.  It is just
 * like when you build a {@link java.lang.String}. You often estimate the length
 * of the resulting string and let {@link java.lang.StringBuffer} expand itself
 * on demand.
 * <pre>
 * // A new dynamic buffer is created.  Internally, the actual buffer is created
 * // lazily to avoid potentially wasted memory space.
 * {@link io.netty.buffer.ByteBuf} b = {@link io.netty.buffer.Unpooled}.buffer(4);
 *
 * // When the first write attempt is made, the internal buffer is created with
 * // the specified initial capacity (4).
 * b.writeByte('1');
 *
 * b.writeByte('2');
 * b.writeByte('3');
 * b.writeByte('4');
 *
 * // When the number of written bytes exceeds the initial capacity (4), the
 * // internal buffer is reallocated automatically with a larger capacity.
 * b.writeByte('5');
 * </pre>
 *
 * <h3>Better Performance</h3>
 *
 * Most frequently used buffer implementation of
 * {@link io.netty.buffer.ByteBuf} is a very thin wrapper of a
 * byte array (i.e. {@code byte[]}).  Unlike {@link java.nio.ByteBuffer}, it has
 * no complicated boundary check and index compensation, and therefore it is
 * easier for a JVM to optimize the buffer access.  More complicated buffer
 * implementation is used only for sliced or composite buffers, and it performs
 * as well as {@link java.nio.ByteBuffer}.
 */
package io.netty.buffer;
