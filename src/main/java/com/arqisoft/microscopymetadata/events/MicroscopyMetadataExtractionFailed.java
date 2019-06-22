package com.arqisoft.microscopymetadata.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import sds.messaging.contracts.AbstractContract;

import java.util.UUID;

public class MicroscopyMetadataExtractionFailed extends AbstractContract {
    protected UUID id;
    protected UUID userId;
    protected String timeStamp;
    protected String message;

    public MicroscopyMetadataExtractionFailed() {
        namespace = "Leanda.Microscopy.Metadata.Domain.Events";
        contractName = MicroscopyMetadataExtractionFailed.class.getSimpleName();
    }

    public MicroscopyMetadataExtractionFailed(UUID id, UUID userId, String timeStamp, String message) {
        this.id = id;
        this.userId = userId;
        this.timeStamp = timeStamp;
        this.message = message;

        namespace = "Leanda.Microscopy.Metadata.Domain.Events";
        contractName = MicroscopyMetadataExtractionFailed.class.getSimpleName();
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

    @JsonProperty("Message")
    public String getMessage() {
        return message;
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

    public void setCalculationException(String calculationException) {
        this.message = calculationException;
    }

    @Override
    public String toString() {
        return "MicroscopyMetadataExtractionFailed{" +
                "id=" + id +
                ", userId=" + userId +
                ", timeStamp='" + timeStamp + '\'' +
                ", message=" + message +
                '}';
    }
}
