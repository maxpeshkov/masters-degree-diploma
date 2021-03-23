package com.github.peshkovm.common.netty;

import com.github.peshkovm.common.component.AbstractLifecycleComponent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * Abstract client bootstrapping class.
 */
public abstract class NettyClient extends AbstractLifecycleComponent {

  protected volatile Bootstrap bootstrap;
  protected final NettyProvider provider;
  protected final EventExecutorGroup executor;

  /**
   * Constructs a new instance using provider for bootstrapping.
   *
   * @param provider provides SocketChannel and EventLoopGroup
   */
  protected NettyClient(NettyProvider provider) {
    logger.info("Initializing...");
    this.provider = provider;
    this.executor = new DefaultEventExecutorGroup(1);
    logger.info("Initialized");
  }

  /** Bootstraps client. This method doesn't connect or bind (in case of UDP) client. */
  @Override
  protected void doStart() {
    bootstrap = new Bootstrap();
    bootstrap
        .group(provider.getChildEventLoopGroup())
        .channel(provider.getClientSocketChannel())
        .handler(
            new LoggingHandler(
                LoggingHandler.class.getName()
                    + "."
                    + this.getClass().getSimpleName()
                    + ".Channel"))
        .handler(channelInitializer());
  }

  /**
   * Returns {@link ChannelInitializer} instance.
   *
   * @return ChannelInitializer instance.
   */
  protected abstract ChannelInitializer<Channel> channelInitializer();

  /**
   * Does nothing.
   */
  @Override
  protected void doStop() {}

  /** Shutdowns Netty's components. */
  @Override
  protected void doClose() {
    bootstrap = null;
  }
}