package com.arqisoft.microscopymetadata.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import sds.messaging.contracts.AbstractContract;

import java.util.UUID;

public class MicroscopyMetadataExtracted extends AbstractContract {

    protected UUID id;
    protected UUID userId;
    protected String timeStamp;
    protected Map<String, Object> metadata;

    public MicroscopyMetadataExtracted() {
        namespace = "Leanda.Microscopy.Metadata.Domain.Events";
        contractName = MicroscopyMetadataExtracted.class.getSimpleName();

    }

    public MicroscopyMetadataExtracted(UUID id, UUID userId, String timeStamp, Map<String, Object> metadata) {
        this.id = id;
        this.userId = userId;
        this.timeStamp = timeStamp;
        this.metadata = metadata;

        namespace = "Leanda.Microscopy.Metadata.Domain.Events";
        contractName = MicroscopyMetadataExtracted.class.getSimpleName();
    }

    @JsonProperty("Id")
    public UUID getId() {
        return id;
    }

    @JsonProperty("UserId")
    public UUID getUserId() {
        return userId;
    }

    @JsonProperty("TimeStamp")
    public String getTimeStamp() {
        return timeStamp;
    }

    @JsonProperty("Metadata")
    public Map<String, Object> getResult() {
        return metadata;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "MicroscopyMetadataExtracted{"
                + "id=" + id
                + ", userId=" + userId
                + ", timeStamp='" + timeStamp + '\''
                + ", metadata=" + metadata
                + '}';
    }
}
