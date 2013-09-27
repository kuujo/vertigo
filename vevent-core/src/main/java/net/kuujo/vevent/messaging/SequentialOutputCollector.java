package net.kuujo.vevent.messaging;

import java.util.ArrayList;
import java.util.List;

public class SequentialOutputCollector implements OutputCollector {

  private List<Channel<?>> channels = new ArrayList<Channel<?>>();

  @Override
  public OutputCollector addChannel(Channel<?> channel) {
    if (!channels.contains(channel)) {
      channels.add(channel);
    }
    return this;
  }

  @Override
  public OutputCollector removeChannel(Channel<?> channel) {
    if (channels.contains(channel)) {
      channels.remove(channel);
    }
    return this;
  }

  @Override
  public int size() {
    return channels.size();
  }

  @Override
  public OutputCollector emit(JsonMessage message) {
    for (Channel<?> channel : channels) {
      channel.write(message);
    }
    return this;
  }

  @Override
  public OutputCollector emit(JsonMessage... messages) {
    for (JsonMessage message : messages) {
      emit(message);
    }
    return this;
  }

}
