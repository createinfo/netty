/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.example.objectecho;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.channel.ChannelEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelState;
import io.netty.channel.ChannelStateEvent;
import io.netty.channel.ExceptionEvent;
import io.netty.channel.MessageEvent;
import io.netty.channel.SimpleChannelUpstreamHandler;

/**
 * Handler implementation for the object echo client.  It initiates the
 * ping-pong traffic between the object echo client and server by sending the
 * first message to the server.
 */
public class ObjectEchoClientHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = Logger.getLogger(
            ObjectEchoClientHandler.class.getName());

    private final List<Integer> firstMessage;
    private final AtomicLong transferredMessages = new AtomicLong();

    /**
     * Creates a client-side handler.
     */
    public ObjectEchoClientHandler(int firstMessageSize) {
        if (firstMessageSize <= 0) {
            throw new IllegalArgumentException(
                    "firstMessageSize: " + firstMessageSize);
        }
        firstMessage = new ArrayList<Integer>(firstMessageSize);
        for (int i = 0; i < firstMessageSize; i ++) {
            firstMessage.add(Integer.valueOf(i));
        }
    }

    public long getTransferredMessages() {
        return transferredMessages.get();
    }

    @Override
    public void handleUpstream(
            ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent &&
            ((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
            logger.info(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelConnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) {
        // Send the first message if this handler is a client-side handler.
        e.channel().write(firstMessage);
    }

    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) {
        // Echo back the received object to the client.
        transferredMessages.incrementAndGet();
        e.channel().write(e.getMessage());
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.",
                e.cause());
        e.channel().close();
    }
}