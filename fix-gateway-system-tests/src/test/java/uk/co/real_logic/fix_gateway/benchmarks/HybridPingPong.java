/*
 * Copyright 2015 Real Logic Ltd.
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
package uk.co.real_logic.fix_gateway.benchmarks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import static uk.co.real_logic.fix_gateway.benchmarks.NetworkBenchmarkUtil.*;

public final class HybridPingPong extends AbstractPingPong
{
    private final FileChannel PING_BUFFER = NetworkBenchmarkUtil.newFileChannel("ping");

    private final ByteBuffer PONG_BUFFER = ByteBuffer.allocate(NetworkBenchmarkUtil.MESSAGE_SIZE);

    public static void main(String[] args) throws IOException
    {
        new HybridPingPong().benchmark();
    }

    protected void ping(SocketChannel channel) throws IOException
    {
        writeChannel(channel, PING_BUFFER);

        readChannel(channel, PING_BUFFER);
    }

    protected void pong(SocketChannel channel) throws IOException
    {
        readByteBuffer(channel, PONG_BUFFER);

        writeByteBuffer(channel, PONG_BUFFER);
    }

}
