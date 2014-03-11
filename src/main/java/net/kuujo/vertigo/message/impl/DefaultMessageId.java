/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.message.impl;

import java.util.Random;
import java.util.UUID;

import net.kuujo.vertigo.message.MessageId;

/**
 * A default message identifier implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultMessageId implements MessageId {
  private static final Random random = new Random();
  private String owner;
  private long code;
  private String id;
  private String parent;
  private String root;
  private String auditor;

  private DefaultMessageId() {
  }

  @Override
  public String owner() {
    return owner;
  }

  @Override
  public long ackCode() {
    return code;
  }

  @Override
  public String correlationId() {
    return id;
  }

  @Override
  public boolean hasParent() {
    return parent != null;
  }

  @Override
  public String parent() {
    return parent;
  }

  @Override
  public boolean hasRoot() {
    return root != null;
  }

  @Override
  public boolean isRoot() {
    return !hasRoot();
  }

  @Override
  public String root() {
    return root;
  }

  @Override
  public String auditor() {
    return auditor;
  }

  @Override
  public MessageId copy() {
    return Builder.newBuilder()
        .setAuditor(auditor)
        .setCode(random.nextInt())
        .setCorrelationId(UUID.randomUUID().toString())
        .setOwner(owner)
        .setParent(parent)
        .setRoot(root)
        .build();
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof MessageId && ((MessageId) object).correlationId().equals(id);
  }

  /**
   * Message ID builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder {
    private DefaultMessageId messageId = new DefaultMessageId();

    private Builder() {
    }

    /**
     * Creates a new message ID builder.
     *
     * @return A new message ID builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Sets the message owner.
     *
     * @param owner The message owner.
     * @return The message builder.
     */
    public Builder setOwner(String owner) {
      messageId.owner = owner;
      return this;
    }

    /**
     * Sets the message ack code.
     *
     * @param code The message ack code.
     * @return The message builder.
     */
    public Builder setCode(int code) {
      messageId.code = code;
      return this;
    }

    /**
     * Sets the correlation ID.
     *
     * @param correlationId The message correlation ID.
     * @return The message builder.
     */
    public Builder setCorrelationId(String correlationId) {
      messageId.id = correlationId;
      return this;
    }

    /**
     * Sets the message parent.
     *
     * @param parent The message parent.
     * @return The message builder.
     */
    public Builder setParent(String parent) {
      messageId.parent = parent;
      return this;
    }

    /**
     * Sets the message root.
     *
     * @param root The message root.
     * @return The message builder.
     */
    public Builder setRoot(String root) {
      messageId.root = root;
      return this;
    }

    /**
     * Sets the message auditor.
     *
     * @param auditor The message auditor.
     * @return The message builder.
     */
    public Builder setAuditor(String auditor) {
      messageId.auditor = auditor;
      return this;
    }

    /**
     * Builds the message ID.
     *
     * @return A new message ID.
     */
    public DefaultMessageId build() {
      return messageId;
    }
  }

}
