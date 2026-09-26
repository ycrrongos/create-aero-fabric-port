package dev.ryanhcode.sable.network.udp;

import io.netty.channel.Channel;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import net.minecraft.server.network.EventLoopGroupHolder;
import org.jetbrains.annotations.ApiStatus;

/**
 * Picks the datagram channel implementation that matches the IO handler of a vanilla event loop group.
 */
@ApiStatus.Internal
public final class SableDatagramChannels {

    private SableDatagramChannels() {
    }

    public static Class<? extends Channel> datagramChannel(final EventLoopGroupHolder holder) {
        final Class<? extends Channel> socketChannel = holder.channelCls();
        if (socketChannel == EpollSocketChannel.class) {
            return EpollDatagramChannel.class;
        }
        if (socketChannel == KQueueSocketChannel.class) {
            return KQueueDatagramChannel.class;
        }
        return NioDatagramChannel.class;
    }
}
