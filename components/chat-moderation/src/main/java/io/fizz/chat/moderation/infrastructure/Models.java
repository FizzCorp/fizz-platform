// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: models.proto

package io.fizz.chat.moderation.infrastructure;

public final class Models {
  private Models() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface ProviderConfigModelOrBuilder extends
      // @@protoc_insertion_point(interface_extends:hbaseClient.ProviderConfigModel)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.hbaseClient.ProviderConfigModel.ProviderType type = 1;</code>
     */
    int getTypeValue();
    /**
     * <code>.hbaseClient.ProviderConfigModel.ProviderType type = 1;</code>
     */
    io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.ProviderType getType();

    /**
     * <code>map&lt;string, string&gt; params = 2;</code>
     */
    int getParamsCount();
    /**
     * <code>map&lt;string, string&gt; params = 2;</code>
     */
    boolean containsParams(
        java.lang.String key);
    /**
     * Use {@link #getParamsMap()} instead.
     */
    @java.lang.Deprecated
    java.util.Map<java.lang.String, java.lang.String>
    getParams();
    /**
     * <code>map&lt;string, string&gt; params = 2;</code>
     */
    java.util.Map<java.lang.String, java.lang.String>
    getParamsMap();
    /**
     * <code>map&lt;string, string&gt; params = 2;</code>
     */

    java.lang.String getParamsOrDefault(
        java.lang.String key,
        java.lang.String defaultValue);
    /**
     * <code>map&lt;string, string&gt; params = 2;</code>
     */

    java.lang.String getParamsOrThrow(
        java.lang.String key);
  }
  /**
   * Protobuf type {@code hbaseClient.ProviderConfigModel}
   */
  public  static final class ProviderConfigModel extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:hbaseClient.ProviderConfigModel)
      ProviderConfigModelOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use ProviderConfigModel.newBuilder() to construct.
    private ProviderConfigModel(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private ProviderConfigModel() {
      type_ = 0;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private ProviderConfigModel(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {
              int rawValue = input.readEnum();

              type_ = rawValue;
              break;
            }
            case 18: {
              if (!((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
                params_ = com.google.protobuf.MapField.newMapField(
                    ParamsDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000002;
              }
              com.google.protobuf.MapEntry<java.lang.String, java.lang.String>
              params__ = input.readMessage(
                  ParamsDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              params_.getMutableMap().put(
                  params__.getKey(), params__.getValue());
              break;
            }
            default: {
              if (!parseUnknownFieldProto3(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return io.fizz.chat.moderation.infrastructure.Models.internal_static_hbaseClient_ProviderConfigModel_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    @java.lang.Override
    protected com.google.protobuf.MapField internalGetMapField(
        int number) {
      switch (number) {
        case 2:
          return internalGetParams();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return io.fizz.chat.moderation.infrastructure.Models.internal_static_hbaseClient_ProviderConfigModel_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.class, io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.Builder.class);
    }

    /**
     * Protobuf enum {@code hbaseClient.ProviderConfigModel.ProviderType}
     */
    public enum ProviderType
        implements com.google.protobuf.ProtocolMessageEnum {
      /**
       * <code>Azure = 0;</code>
       */
      Azure(0),
      /**
       * <code>CleanSpeak = 1;</code>
       */
      CleanSpeak(1),
      UNRECOGNIZED(-1),
      ;

      /**
       * <code>Azure = 0;</code>
       */
      public static final int Azure_VALUE = 0;
      /**
       * <code>CleanSpeak = 1;</code>
       */
      public static final int CleanSpeak_VALUE = 1;


      public final int getNumber() {
        if (this == UNRECOGNIZED) {
          throw new java.lang.IllegalArgumentException(
              "Can't get the number of an unknown enum value.");
        }
        return value;
      }

      /**
       * @deprecated Use {@link #forNumber(int)} instead.
       */
      @java.lang.Deprecated
      public static ProviderType valueOf(int value) {
        return forNumber(value);
      }

      public static ProviderType forNumber(int value) {
        switch (value) {
          case 0: return Azure;
          case 1: return CleanSpeak;
          default: return null;
        }
      }

      public static com.google.protobuf.Internal.EnumLiteMap<ProviderType>
          internalGetValueMap() {
        return internalValueMap;
      }
      private static final com.google.protobuf.Internal.EnumLiteMap<
          ProviderType> internalValueMap =
            new com.google.protobuf.Internal.EnumLiteMap<ProviderType>() {
              public ProviderType findValueByNumber(int number) {
                return ProviderType.forNumber(number);
              }
            };

      public final com.google.protobuf.Descriptors.EnumValueDescriptor
          getValueDescriptor() {
        return getDescriptor().getValues().get(ordinal());
      }
      public final com.google.protobuf.Descriptors.EnumDescriptor
          getDescriptorForType() {
        return getDescriptor();
      }
      public static final com.google.protobuf.Descriptors.EnumDescriptor
          getDescriptor() {
        return io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.getDescriptor().getEnumTypes().get(0);
      }

      private static final ProviderType[] VALUES = values();

      public static ProviderType valueOf(
          com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
        if (desc.getType() != getDescriptor()) {
          throw new java.lang.IllegalArgumentException(
            "EnumValueDescriptor is not for this type.");
        }
        if (desc.getIndex() == -1) {
          return UNRECOGNIZED;
        }
        return VALUES[desc.getIndex()];
      }

      private final int value;

      private ProviderType(int value) {
        this.value = value;
      }

      // @@protoc_insertion_point(enum_scope:hbaseClient.ProviderConfigModel.ProviderType)
    }

    private int bitField0_;
    public static final int TYPE_FIELD_NUMBER = 1;
    private int type_;
    /**
     * <code>.hbaseClient.ProviderConfigModel.ProviderType type = 1;</code>
     */
    public int getTypeValue() {
      return type_;
    }
    /**
     * <code>.hbaseClient.ProviderConfigModel.ProviderType type = 1;</code>
     */
    public io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.ProviderType getType() {
      @SuppressWarnings("deprecation")
      io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.ProviderType result = io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.ProviderType.valueOf(type_);
      return result == null ? io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.ProviderType.UNRECOGNIZED : result;
    }

    public static final int PARAMS_FIELD_NUMBER = 2;
    private static final class ParamsDefaultEntryHolder {
      static final com.google.protobuf.MapEntry<
          java.lang.String, java.lang.String> defaultEntry =
              com.google.protobuf.MapEntry
              .<java.lang.String, java.lang.String>newDefaultInstance(
                  io.fizz.chat.moderation.infrastructure.Models.internal_static_hbaseClient_ProviderConfigModel_ParamsEntry_descriptor, 
                  com.google.protobuf.WireFormat.FieldType.STRING,
                  "",
                  com.google.protobuf.WireFormat.FieldType.STRING,
                  "");
    }
    private com.google.protobuf.MapField<
        java.lang.String, java.lang.String> params_;
    private com.google.protobuf.MapField<java.lang.String, java.lang.String>
    internalGetParams() {
      if (params_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            ParamsDefaultEntryHolder.defaultEntry);
      }
      return params_;
    }

    public int getParamsCount() {
      return internalGetParams().getMap().size();
    }
    /**
     * <code>map&lt;string, string&gt; params = 2;</code>
     */

    public boolean containsParams(
        java.lang.String key) {
      if (key == null) { throw new java.lang.NullPointerException(); }
      return internalGetParams().getMap().containsKey(key);
    }
    /**
     * Use {@link #getParamsMap()} instead.
     */
    @java.lang.Deprecated
    public java.util.Map<java.lang.String, java.lang.String> getParams() {
      return getParamsMap();
    }
    /**
     * <code>map&lt;string, string&gt; params = 2;</code>
     */

    public java.util.Map<java.lang.String, java.lang.String> getParamsMap() {
      return internalGetParams().getMap();
    }
    /**
     * <code>map&lt;string, string&gt; params = 2;</code>
     */

    public java.lang.String getParamsOrDefault(
        java.lang.String key,
        java.lang.String defaultValue) {
      if (key == null) { throw new java.lang.NullPointerException(); }
      java.util.Map<java.lang.String, java.lang.String> map =
          internalGetParams().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;string, string&gt; params = 2;</code>
     */

    public java.lang.String getParamsOrThrow(
        java.lang.String key) {
      if (key == null) { throw new java.lang.NullPointerException(); }
      java.util.Map<java.lang.String, java.lang.String> map =
          internalGetParams().getMap();
      if (!map.containsKey(key)) {
        throw new java.lang.IllegalArgumentException();
      }
      return map.get(key);
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (type_ != io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.ProviderType.Azure.getNumber()) {
        output.writeEnum(1, type_);
      }
      com.google.protobuf.GeneratedMessageV3
        .serializeStringMapTo(
          output,
          internalGetParams(),
          ParamsDefaultEntryHolder.defaultEntry,
          2);
      unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (type_ != io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.ProviderType.Azure.getNumber()) {
        size += com.google.protobuf.CodedOutputStream
          .computeEnumSize(1, type_);
      }
      for (java.util.Map.Entry<java.lang.String, java.lang.String> entry
           : internalGetParams().getMap().entrySet()) {
        com.google.protobuf.MapEntry<java.lang.String, java.lang.String>
        params__ = ParamsDefaultEntryHolder.defaultEntry.newBuilderForType()
            .setKey(entry.getKey())
            .setValue(entry.getValue())
            .build();
        size += com.google.protobuf.CodedOutputStream
            .computeMessageSize(2, params__);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel)) {
        return super.equals(obj);
      }
      io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel other = (io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel) obj;

      boolean result = true;
      result = result && type_ == other.type_;
      result = result && internalGetParams().equals(
          other.internalGetParams());
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + TYPE_FIELD_NUMBER;
      hash = (53 * hash) + type_;
      if (!internalGetParams().getMap().isEmpty()) {
        hash = (37 * hash) + PARAMS_FIELD_NUMBER;
        hash = (53 * hash) + internalGetParams().hashCode();
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code hbaseClient.ProviderConfigModel}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:hbaseClient.ProviderConfigModel)
        io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModelOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return io.fizz.chat.moderation.infrastructure.Models.internal_static_hbaseClient_ProviderConfigModel_descriptor;
      }

      @SuppressWarnings({"rawtypes"})
      protected com.google.protobuf.MapField internalGetMapField(
          int number) {
        switch (number) {
          case 2:
            return internalGetParams();
          default:
            throw new RuntimeException(
                "Invalid map field number: " + number);
        }
      }
      @SuppressWarnings({"rawtypes"})
      protected com.google.protobuf.MapField internalGetMutableMapField(
          int number) {
        switch (number) {
          case 2:
            return internalGetMutableParams();
          default:
            throw new RuntimeException(
                "Invalid map field number: " + number);
        }
      }
      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return io.fizz.chat.moderation.infrastructure.Models.internal_static_hbaseClient_ProviderConfigModel_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.class, io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.Builder.class);
      }

      // Construct using io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        type_ = 0;

        internalGetMutableParams().clear();
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return io.fizz.chat.moderation.infrastructure.Models.internal_static_hbaseClient_ProviderConfigModel_descriptor;
      }

      @java.lang.Override
      public io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel getDefaultInstanceForType() {
        return io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.getDefaultInstance();
      }

      @java.lang.Override
      public io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel build() {
        io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel buildPartial() {
        io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel result = new io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        result.type_ = type_;
        result.params_ = internalGetParams();
        result.params_.makeImmutable();
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      @java.lang.Override
      public Builder clone() {
        return (Builder) super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel) {
          return mergeFrom((io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel other) {
        if (other == io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.getDefaultInstance()) return this;
        if (other.type_ != 0) {
          setTypeValue(other.getTypeValue());
        }
        internalGetMutableParams().mergeFrom(
            other.internalGetParams());
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private int type_ = 0;
      /**
       * <code>.hbaseClient.ProviderConfigModel.ProviderType type = 1;</code>
       */
      public int getTypeValue() {
        return type_;
      }
      /**
       * <code>.hbaseClient.ProviderConfigModel.ProviderType type = 1;</code>
       */
      public Builder setTypeValue(int value) {
        type_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>.hbaseClient.ProviderConfigModel.ProviderType type = 1;</code>
       */
      public io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.ProviderType getType() {
        @SuppressWarnings("deprecation")
        io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.ProviderType result = io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.ProviderType.valueOf(type_);
        return result == null ? io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.ProviderType.UNRECOGNIZED : result;
      }
      /**
       * <code>.hbaseClient.ProviderConfigModel.ProviderType type = 1;</code>
       */
      public Builder setType(io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel.ProviderType value) {
        if (value == null) {
          throw new NullPointerException();
        }
        
        type_ = value.getNumber();
        onChanged();
        return this;
      }
      /**
       * <code>.hbaseClient.ProviderConfigModel.ProviderType type = 1;</code>
       */
      public Builder clearType() {
        
        type_ = 0;
        onChanged();
        return this;
      }

      private com.google.protobuf.MapField<
          java.lang.String, java.lang.String> params_;
      private com.google.protobuf.MapField<java.lang.String, java.lang.String>
      internalGetParams() {
        if (params_ == null) {
          return com.google.protobuf.MapField.emptyMapField(
              ParamsDefaultEntryHolder.defaultEntry);
        }
        return params_;
      }
      private com.google.protobuf.MapField<java.lang.String, java.lang.String>
      internalGetMutableParams() {
        onChanged();;
        if (params_ == null) {
          params_ = com.google.protobuf.MapField.newMapField(
              ParamsDefaultEntryHolder.defaultEntry);
        }
        if (!params_.isMutable()) {
          params_ = params_.copy();
        }
        return params_;
      }

      public int getParamsCount() {
        return internalGetParams().getMap().size();
      }
      /**
       * <code>map&lt;string, string&gt; params = 2;</code>
       */

      public boolean containsParams(
          java.lang.String key) {
        if (key == null) { throw new java.lang.NullPointerException(); }
        return internalGetParams().getMap().containsKey(key);
      }
      /**
       * Use {@link #getParamsMap()} instead.
       */
      @java.lang.Deprecated
      public java.util.Map<java.lang.String, java.lang.String> getParams() {
        return getParamsMap();
      }
      /**
       * <code>map&lt;string, string&gt; params = 2;</code>
       */

      public java.util.Map<java.lang.String, java.lang.String> getParamsMap() {
        return internalGetParams().getMap();
      }
      /**
       * <code>map&lt;string, string&gt; params = 2;</code>
       */

      public java.lang.String getParamsOrDefault(
          java.lang.String key,
          java.lang.String defaultValue) {
        if (key == null) { throw new java.lang.NullPointerException(); }
        java.util.Map<java.lang.String, java.lang.String> map =
            internalGetParams().getMap();
        return map.containsKey(key) ? map.get(key) : defaultValue;
      }
      /**
       * <code>map&lt;string, string&gt; params = 2;</code>
       */

      public java.lang.String getParamsOrThrow(
          java.lang.String key) {
        if (key == null) { throw new java.lang.NullPointerException(); }
        java.util.Map<java.lang.String, java.lang.String> map =
            internalGetParams().getMap();
        if (!map.containsKey(key)) {
          throw new java.lang.IllegalArgumentException();
        }
        return map.get(key);
      }

      public Builder clearParams() {
        internalGetMutableParams().getMutableMap()
            .clear();
        return this;
      }
      /**
       * <code>map&lt;string, string&gt; params = 2;</code>
       */

      public Builder removeParams(
          java.lang.String key) {
        if (key == null) { throw new java.lang.NullPointerException(); }
        internalGetMutableParams().getMutableMap()
            .remove(key);
        return this;
      }
      /**
       * Use alternate mutation accessors instead.
       */
      @java.lang.Deprecated
      public java.util.Map<java.lang.String, java.lang.String>
      getMutableParams() {
        return internalGetMutableParams().getMutableMap();
      }
      /**
       * <code>map&lt;string, string&gt; params = 2;</code>
       */
      public Builder putParams(
          java.lang.String key,
          java.lang.String value) {
        if (key == null) { throw new java.lang.NullPointerException(); }
        if (value == null) { throw new java.lang.NullPointerException(); }
        internalGetMutableParams().getMutableMap()
            .put(key, value);
        return this;
      }
      /**
       * <code>map&lt;string, string&gt; params = 2;</code>
       */

      public Builder putAllParams(
          java.util.Map<java.lang.String, java.lang.String> values) {
        internalGetMutableParams().getMutableMap()
            .putAll(values);
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFieldsProto3(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:hbaseClient.ProviderConfigModel)
    }

    // @@protoc_insertion_point(class_scope:hbaseClient.ProviderConfigModel)
    private static final io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel();
    }

    public static io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<ProviderConfigModel>
        PARSER = new com.google.protobuf.AbstractParser<ProviderConfigModel>() {
      @java.lang.Override
      public ProviderConfigModel parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new ProviderConfigModel(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<ProviderConfigModel> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<ProviderConfigModel> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public io.fizz.chat.moderation.infrastructure.Models.ProviderConfigModel getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_hbaseClient_ProviderConfigModel_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_hbaseClient_ProviderConfigModel_fieldAccessorTable;
  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_hbaseClient_ProviderConfigModel_ParamsEntry_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_hbaseClient_ProviderConfigModel_ParamsEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\014models.proto\022\013hbaseClient\"\352\001\n\023Provider" +
      "ConfigModel\022;\n\004type\030\001 \001(\0162-.hbaseClient." +
      "ProviderConfigModel.ProviderType\022<\n\006para" +
      "ms\030\002 \003(\0132,.hbaseClient.ProviderConfigMod" +
      "el.ParamsEntry\032-\n\013ParamsEntry\022\013\n\003key\030\001 \001" +
      "(\t\022\r\n\005value\030\002 \001(\t:\0028\001\")\n\014ProviderType\022\t\n" +
      "\005Azure\020\000\022\016\n\nCleanSpeak\020\001B0\n&io.fizz.chat" +
      ".moderation.infrastructureB\006Modelsb\006prot" +
      "o3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_hbaseClient_ProviderConfigModel_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_hbaseClient_ProviderConfigModel_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_hbaseClient_ProviderConfigModel_descriptor,
        new java.lang.String[] { "Type", "Params", });
    internal_static_hbaseClient_ProviderConfigModel_ParamsEntry_descriptor =
      internal_static_hbaseClient_ProviderConfigModel_descriptor.getNestedTypes().get(0);
    internal_static_hbaseClient_ProviderConfigModel_ParamsEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_hbaseClient_ProviderConfigModel_ParamsEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
