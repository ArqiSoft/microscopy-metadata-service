package com.arqisoft.microscopymetadata.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import sds.messaging.contracts.AbstractContract;

import java.util.UUID;

public class ExtractMicroscopyMetadata extends AbstractContract {
    protected UUID id;
    protected String bucket;
    protected UUID blobId;
    protected UUID userId;

    public ExtractMicroscopyMetadata() {
        namespace = "Leanda.Microscopy.Metadata.Domain.Commands";
        contractName = ExtractMicroscopyMetadata.class.getSimpleName();
    }

    public ExtractMicroscopyMetadata(UUID id, String bucket, UUID blobId, UUID userId) {
        this.id = id;
        this.bucket = bucket;
        this.blobId = blobId;
        this.userId = userId;

        namespace = "Leanda.Microscopy.Metadata.Domain.Commands";
        contractName = ExtractMicroscopyMetadata.class.getSimpleName();
    }

    @JsonProperty("Id")
    public UUID getId() {
        return id;
    }

    @JsonProperty("Bucket")
    public String getBucket() {
        return bucket;
    }

    @JsonProperty("BlobId")
    public UUID getBlobId() {
        return blobId;
    }

    @JsonProperty("UserId")
    public UUID getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "ExtractMicroscopyMetadata{" +
                "id=" + id +
                ", bucket='" + bucket + '\'' +
                ", blobId=" + blobId +
                ", userId=" + userId +
                ", namespace='" + namespace + '\'' +
                ", contractName='" + contractName + '\'' +
                ", correlationId=" + getCorrelationId() +
                '}';
    }
}
