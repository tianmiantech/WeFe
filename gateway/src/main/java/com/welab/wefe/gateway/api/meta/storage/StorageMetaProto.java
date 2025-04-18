// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: storage-basic.proto

package com.welab.wefe.gateway.api.meta.storage;

public final class StorageMetaProto {
  private StorageMetaProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  /**
   * Protobuf enum {@code com.welab.wefe.gateway.api.meta.storage.StorageType}
   */
  public enum StorageType
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>LMDB = 0;</code>
     */
    LMDB(0),
    /**
     * <code>CLICK_HOUSE = 1;</code>
     */
    CLICK_HOUSE(1),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>LMDB = 0;</code>
     */
    public static final int LMDB_VALUE = 0;
    /**
     * <code>CLICK_HOUSE = 1;</code>
     */
    public static final int CLICK_HOUSE_VALUE = 1;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @Deprecated
    public static StorageType valueOf(int value) {
      return forNumber(value);
    }

    /**
     * @param value The numeric wire value of the corresponding enum entry.
     * @return The enum associated with the given numeric wire value.
     */
    public static StorageType forNumber(int value) {
      switch (value) {
        case 0: return LMDB;
        case 1: return CLICK_HOUSE;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<StorageType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        StorageType> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<StorageType>() {
            public StorageType findValueByNumber(int number) {
              return StorageType.forNumber(number);
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
      return StorageMetaProto.getDescriptor().getEnumTypes().get(0);
    }

    private static final StorageType[] VALUES = values();

    public static StorageType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private StorageType(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:com.welab.wefe.gateway.api.meta.storage.StorageType)
  }

  public interface StorageLocatorOrBuilder extends
      // @@protoc_insertion_point(interface_extends:com.welab.wefe.gateway.api.meta.storage.StorageLocator)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.com.welab.wefe.gateway.api.meta.storage.StorageType type = 1;</code>
     * @return The enum numeric value on the wire for type.
     */
    int getTypeValue();
    /**
     * <code>.com.welab.wefe.gateway.api.meta.storage.StorageType type = 1;</code>
     * @return The type.
     */
    StorageType getType();

    /**
     * <code>string namespace = 2;</code>
     * @return The namespace.
     */
    String getNamespace();
    /**
     * <code>string namespace = 2;</code>
     * @return The bytes for namespace.
     */
    com.google.protobuf.ByteString
        getNamespaceBytes();

    /**
     * <code>string name = 3;</code>
     * @return The name.
     */
    String getName();
    /**
     * <code>string name = 3;</code>
     * @return The bytes for name.
     */
    com.google.protobuf.ByteString
        getNameBytes();

    /**
     * <code>int32 fragment = 4;</code>
     * @return The fragment.
     */
    int getFragment();
  }
  /**
   * Protobuf type {@code com.welab.wefe.gateway.api.meta.storage.StorageLocator}
   */
  public  static final class StorageLocator extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:com.welab.wefe.gateway.api.meta.storage.StorageLocator)
      StorageLocatorOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use StorageLocator.newBuilder() to construct.
    private StorageLocator(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private StorageLocator() {
      type_ = 0;
      namespace_ = "";
      name_ = "";
    }

    @Override
    @SuppressWarnings({"unused"})
    protected Object newInstance(
        UnusedPrivateParameter unused) {
      return new StorageLocator();
    }

    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private StorageLocator(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new NullPointerException();
      }
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
              String s = input.readStringRequireUtf8();

              namespace_ = s;
              break;
            }
            case 26: {
              String s = input.readStringRequireUtf8();

              name_ = s;
              break;
            }
            case 32: {

              fragment_ = input.readInt32();
              break;
            }
            default: {
              if (!parseUnknownField(
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
      return StorageMetaProto.internal_static_com_welab_wefe_gateway_api_meta_storage_StorageLocator_descriptor;
    }

    @Override
    protected FieldAccessorTable
        internalGetFieldAccessorTable() {
      return StorageMetaProto.internal_static_com_welab_wefe_gateway_api_meta_storage_StorageLocator_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              StorageLocator.class, Builder.class);
    }

    public static final int TYPE_FIELD_NUMBER = 1;
    private int type_;
    /**
     * <code>.com.welab.wefe.gateway.api.meta.storage.StorageType type = 1;</code>
     * @return The enum numeric value on the wire for type.
     */
    public int getTypeValue() {
      return type_;
    }
    /**
     * <code>.com.welab.wefe.gateway.api.meta.storage.StorageType type = 1;</code>
     * @return The type.
     */
    public StorageType getType() {
      @SuppressWarnings("deprecation")
      StorageType result = StorageType.valueOf(type_);
      return result == null ? StorageType.UNRECOGNIZED : result;
    }

    public static final int NAMESPACE_FIELD_NUMBER = 2;
    private volatile Object namespace_;
    /**
     * <code>string namespace = 2;</code>
     * @return The namespace.
     */
    public String getNamespace() {
      Object ref = namespace_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        namespace_ = s;
        return s;
      }
    }
    /**
     * <code>string namespace = 2;</code>
     * @return The bytes for namespace.
     */
    public com.google.protobuf.ByteString
        getNamespaceBytes() {
      Object ref = namespace_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        namespace_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int NAME_FIELD_NUMBER = 3;
    private volatile Object name_;
    /**
     * <code>string name = 3;</code>
     * @return The name.
     */
    public String getName() {
      Object ref = name_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        name_ = s;
        return s;
      }
    }
    /**
     * <code>string name = 3;</code>
     * @return The bytes for name.
     */
    public com.google.protobuf.ByteString
        getNameBytes() {
      Object ref = name_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        name_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int FRAGMENT_FIELD_NUMBER = 4;
    private int fragment_;
    /**
     * <code>int32 fragment = 4;</code>
     * @return The fragment.
     */
    public int getFragment() {
      return fragment_;
    }

    private byte memoizedIsInitialized = -1;
    @Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (type_ != StorageType.LMDB.getNumber()) {
        output.writeEnum(1, type_);
      }
      if (!getNamespaceBytes().isEmpty()) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 2, namespace_);
      }
      if (!getNameBytes().isEmpty()) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 3, name_);
      }
      if (fragment_ != 0) {
        output.writeInt32(4, fragment_);
      }
      unknownFields.writeTo(output);
    }

    @Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (type_ != StorageType.LMDB.getNumber()) {
        size += com.google.protobuf.CodedOutputStream
          .computeEnumSize(1, type_);
      }
      if (!getNamespaceBytes().isEmpty()) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, namespace_);
      }
      if (!getNameBytes().isEmpty()) {
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, name_);
      }
      if (fragment_ != 0) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(4, fragment_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof StorageLocator)) {
        return super.equals(obj);
      }
      StorageLocator other = (StorageLocator) obj;

      if (type_ != other.type_) return false;
      if (!getNamespace()
          .equals(other.getNamespace())) return false;
      if (!getName()
          .equals(other.getName())) return false;
      if (getFragment()
          != other.getFragment()) return false;
      if (!unknownFields.equals(other.unknownFields)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + TYPE_FIELD_NUMBER;
      hash = (53 * hash) + type_;
      hash = (37 * hash) + NAMESPACE_FIELD_NUMBER;
      hash = (53 * hash) + getNamespace().hashCode();
      hash = (37 * hash) + NAME_FIELD_NUMBER;
      hash = (53 * hash) + getName().hashCode();
      hash = (37 * hash) + FRAGMENT_FIELD_NUMBER;
      hash = (53 * hash) + getFragment();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static StorageLocator parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static StorageLocator parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static StorageLocator parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static StorageLocator parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static StorageLocator parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static StorageLocator parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static StorageLocator parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static StorageLocator parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static StorageLocator parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static StorageLocator parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static StorageLocator parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static StorageLocator parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(StorageLocator prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @Override
    protected Builder newBuilderForType(
        BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code com.welab.wefe.gateway.api.meta.storage.StorageLocator}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:com.welab.wefe.gateway.api.meta.storage.StorageLocator)
        StorageLocatorOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return StorageMetaProto.internal_static_com_welab_wefe_gateway_api_meta_storage_StorageLocator_descriptor;
      }

      @Override
      protected FieldAccessorTable
          internalGetFieldAccessorTable() {
        return StorageMetaProto.internal_static_com_welab_wefe_gateway_api_meta_storage_StorageLocator_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                StorageLocator.class, Builder.class);
      }

      // Construct using com.welab.wefe.gateway.api.meta.storage.StorageMetaProto.StorageLocator.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @Override
      public Builder clear() {
        super.clear();
        type_ = 0;

        namespace_ = "";

        name_ = "";

        fragment_ = 0;

        return this;
      }

      @Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return StorageMetaProto.internal_static_com_welab_wefe_gateway_api_meta_storage_StorageLocator_descriptor;
      }

      @Override
      public StorageLocator getDefaultInstanceForType() {
        return StorageLocator.getDefaultInstance();
      }

      @Override
      public StorageLocator build() {
        StorageLocator result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @Override
      public StorageLocator buildPartial() {
        StorageLocator result = new StorageLocator(this);
        result.type_ = type_;
        result.namespace_ = namespace_;
        result.name_ = name_;
        result.fragment_ = fragment_;
        onBuilt();
        return result;
      }

      @Override
      public Builder clone() {
        return super.clone();
      }
      @Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return super.setField(field, value);
      }
      @Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return super.addRepeatedField(field, value);
      }
      @Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof StorageLocator) {
          return mergeFrom((StorageLocator)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(StorageLocator other) {
        if (other == StorageLocator.getDefaultInstance()) return this;
        if (other.type_ != 0) {
          setTypeValue(other.getTypeValue());
        }
        if (!other.getNamespace().isEmpty()) {
          namespace_ = other.namespace_;
          onChanged();
        }
        if (!other.getName().isEmpty()) {
          name_ = other.name_;
          onChanged();
        }
        if (other.getFragment() != 0) {
          setFragment(other.getFragment());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @Override
      public final boolean isInitialized() {
        return true;
      }

      @Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        StorageLocator parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (StorageLocator) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private int type_ = 0;
      /**
       * <code>.com.welab.wefe.gateway.api.meta.storage.StorageType type = 1;</code>
       * @return The enum numeric value on the wire for type.
       */
      public int getTypeValue() {
        return type_;
      }
      /**
       * <code>.com.welab.wefe.gateway.api.meta.storage.StorageType type = 1;</code>
       * @param value The enum numeric value on the wire for type to set.
       * @return This builder for chaining.
       */
      public Builder setTypeValue(int value) {
        type_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>.com.welab.wefe.gateway.api.meta.storage.StorageType type = 1;</code>
       * @return The type.
       */
      public StorageType getType() {
        @SuppressWarnings("deprecation")
        StorageType result = StorageType.valueOf(type_);
        return result == null ? StorageType.UNRECOGNIZED : result;
      }
      /**
       * <code>.com.welab.wefe.gateway.api.meta.storage.StorageType type = 1;</code>
       * @param value The type to set.
       * @return This builder for chaining.
       */
      public Builder setType(StorageType value) {
        if (value == null) {
          throw new NullPointerException();
        }
        
        type_ = value.getNumber();
        onChanged();
        return this;
      }
      /**
       * <code>.com.welab.wefe.gateway.api.meta.storage.StorageType type = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearType() {
        
        type_ = 0;
        onChanged();
        return this;
      }

      private Object namespace_ = "";
      /**
       * <code>string namespace = 2;</code>
       * @return The namespace.
       */
      public String getNamespace() {
        Object ref = namespace_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          namespace_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>string namespace = 2;</code>
       * @return The bytes for namespace.
       */
      public com.google.protobuf.ByteString
          getNamespaceBytes() {
        Object ref = namespace_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          namespace_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string namespace = 2;</code>
       * @param value The namespace to set.
       * @return This builder for chaining.
       */
      public Builder setNamespace(
          String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        namespace_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string namespace = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearNamespace() {
        
        namespace_ = getDefaultInstance().getNamespace();
        onChanged();
        return this;
      }
      /**
       * <code>string namespace = 2;</code>
       * @param value The bytes for namespace to set.
       * @return This builder for chaining.
       */
      public Builder setNamespaceBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        
        namespace_ = value;
        onChanged();
        return this;
      }

      private Object name_ = "";
      /**
       * <code>string name = 3;</code>
       * @return The name.
       */
      public String getName() {
        Object ref = name_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          name_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>string name = 3;</code>
       * @return The bytes for name.
       */
      public com.google.protobuf.ByteString
          getNameBytes() {
        Object ref = name_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          name_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>string name = 3;</code>
       * @param value The name to set.
       * @return This builder for chaining.
       */
      public Builder setName(
          String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        name_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>string name = 3;</code>
       * @return This builder for chaining.
       */
      public Builder clearName() {
        
        name_ = getDefaultInstance().getName();
        onChanged();
        return this;
      }
      /**
       * <code>string name = 3;</code>
       * @param value The bytes for name to set.
       * @return This builder for chaining.
       */
      public Builder setNameBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
        
        name_ = value;
        onChanged();
        return this;
      }

      private int fragment_ ;
      /**
       * <code>int32 fragment = 4;</code>
       * @return The fragment.
       */
      public int getFragment() {
        return fragment_;
      }
      /**
       * <code>int32 fragment = 4;</code>
       * @param value The fragment to set.
       * @return This builder for chaining.
       */
      public Builder setFragment(int value) {
        
        fragment_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>int32 fragment = 4;</code>
       * @return This builder for chaining.
       */
      public Builder clearFragment() {
        
        fragment_ = 0;
        onChanged();
        return this;
      }
      @Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:com.welab.wefe.gateway.api.meta.storage.StorageLocator)
    }

    // @@protoc_insertion_point(class_scope:com.welab.wefe.gateway.api.meta.storage.StorageLocator)
    private static final StorageLocator DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new StorageLocator();
    }

    public static StorageLocator getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<StorageLocator>
        PARSER = new com.google.protobuf.AbstractParser<StorageLocator>() {
      @Override
      public StorageLocator parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new StorageLocator(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<StorageLocator> parser() {
      return PARSER;
    }

    @Override
    public com.google.protobuf.Parser<StorageLocator> getParserForType() {
      return PARSER;
    }

    @Override
    public StorageLocator getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_com_welab_wefe_gateway_api_meta_storage_StorageLocator_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_com_welab_wefe_gateway_api_meta_storage_StorageLocator_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    String[] descriptorData = {
      "\n\023storage-basic.proto\022\'com.welab.wefe.ga" +
      "teway.api.meta.storage\"\207\001\n\016StorageLocato" +
      "r\022B\n\004type\030\001 \001(\01624.com.welab.wefe.gateway" +
      ".api.meta.storage.StorageType\022\021\n\tnamespa" +
      "ce\030\002 \001(\t\022\014\n\004name\030\003 \001(\t\022\020\n\010fragment\030\004 \001(\005" +
      "*(\n\013StorageType\022\010\n\004LMDB\020\000\022\017\n\013CLICK_HOUSE" +
      "\020\001B\022B\020StorageMetaProtob\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_com_welab_wefe_gateway_api_meta_storage_StorageLocator_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_com_welab_wefe_gateway_api_meta_storage_StorageLocator_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_com_welab_wefe_gateway_api_meta_storage_StorageLocator_descriptor,
        new String[] { "Type", "Namespace", "Name", "Fragment", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
