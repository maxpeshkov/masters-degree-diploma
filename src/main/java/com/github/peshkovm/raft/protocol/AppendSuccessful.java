package com.github.peshkovm.raft.protocol;

import com.github.peshkovm.common.codec.Message;
import com.github.peshkovm.transport.DiscoveryNode;
import lombok.Data;

@Data
public class AppendSuccessful implements Message {

  private final DiscoveryNode discoveryNode;
  private final AppendMessage message;

  public AppendSuccessful(DiscoveryNode discoveryNode, AppendMessage message) {
    this.discoveryNode = discoveryNode;
    this.message = message;
  }
}
