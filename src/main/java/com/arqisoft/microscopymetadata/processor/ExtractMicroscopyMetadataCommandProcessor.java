package com.arqisoft.microscopymetadata.processor;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

import com.arqisoft.microscopymetadata.commands.ExtractMicroscopyMetadata;
import com.arqisoft.microscopymetadata.domain.MicroscopyMetadataExtractor;
import com.arqisoft.microscopymetadata.events.MicroscopyMetadataExtracted;
import com.arqisoft.microscopymetadata.events.MicroscopyMetadataExtractionFailed;
import com.sds.storage.BlobInfo;
import com.sds.storage.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.npspot.jtransitlight.consumer.ReceiverBusControl;
import com.npspot.jtransitlight.publisher.IBusControl;
import com.sds.storage.BlobStorage;
import java.nio.file.Files;
import loci.common.services.ServiceFactory;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import sds.messaging.callback.MessageProcessor;

@Component
public class ExtractMicroscopyMetadataCommandProcessor implements MessageProcessor<ExtractMicroscopyMetadata> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractMicroscopyMetadataCommandProcessor.class);

    ReceiverBusControl receiver;
    IBusControl bus;
    BlobStorage storage;

    @Autowired
    public ExtractMicroscopyMetadataCommandProcessor(ReceiverBusControl receiver, IBusControl bus, BlobStorage storage) {
        this.bus = bus;
        this.receiver = receiver;
        this.storage = storage;
    }

    @Override
    public void process(ExtractMicroscopyMetadata message) {

        try {
            BlobInfo blob = storage.getFileInfo(new Guid(message.getBlobId()), message.getBucket());

            if (blob == null) {
                throw new FileNotFoundException(String.format("Blob with Id %s not found in bucket %s",
                        new Guid(message.getBlobId()), message.getBucket()));
            }

            File directory = new File(System.getenv("OSDR_TEMP_FILES_FOLDER"));
            File tempFile = File.createTempFile("temp", "." + FilenameUtils.getExtension(blob.getFileName()).toLowerCase(), directory);

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                IOUtils.copy(storage.getFileStream(new Guid(message.getBlobId()), message.getBucket()), out);
            }

            String id = tempFile.getCanonicalPath();

            Map<String, Object> metadata = new HashMap<>();
            ServiceFactory factory = new ServiceFactory();
            OMEXMLService service = factory.getInstance(OMEXMLService.class);
            IMetadata meta = service.createOMEXMLMetadata();
            IFormatReader reader = new ImageReader();
            reader.setMetadataStore(meta);
            reader.setId(id);
            int series = 0;
            int seriesCount = reader.getSeriesCount();
            if (series < seriesCount) {
                reader.setSeries(series);
            }

            series = reader.getSeries();
            Map<String, Object> result = new HashMap<>();
            
            result.put("Image series", series + " of " + seriesCount);
            
            metadata.putAll(MicroscopyMetadataExtractor.printPixelDimensions(reader));

            metadata.putAll(MicroscopyMetadataExtractor.printPhysicalDimensions(meta, series));

            metadata.putAll(MicroscopyMetadataExtractor.readPhysicalSize(id));

            metadata.putAll(MicroscopyMetadataExtractor.printLensNA(id));

            metadata.putAll(MicroscopyMetadataExtractor.calculateSubresolution(id));

            publishSuccessEvent(message, metadata);
            
            Files.delete(tempFile.toPath());
            
        } catch (Exception exception) {
            publishFailureEvent(message, exception.getMessage());
        }

    }

    private void publishSuccessEvent(ExtractMicroscopyMetadata message, Map<String, Object> metadata) {
        MicroscopyMetadataExtracted event = new MicroscopyMetadataExtracted();
        event.setId(message.getId());
        event.setUserId(message.getUserId());
        event.setTimeStamp(getTimestamp());
        event.setMetadata(metadata);

        event.setCorrelationId(message.getCorrelationId());

        LOGGER.debug("Publishing event {}", event);

        bus.publish(event);
    }

    private void publishFailureEvent(ExtractMicroscopyMetadata message, String exception) {
        MicroscopyMetadataExtractionFailed event = new MicroscopyMetadataExtractionFailed();
        event.setId(message.getId());
        event.setUserId(message.getUserId());
        event.setTimeStamp(getTimestamp());
        event.setCorrelationId(message.getCorrelationId());
        event.setCalculationException(exception);

        LOGGER.debug("Publishing event {}", event);

        bus.publish(event);
    }

    private Map<String, Object> calculateMetadata() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("Experimenter", "John Doe");
        result.put("ExperimenterGroup", "John Doe Ltd.");
        result.put("ProjectExperiment -> Name", "Moonshine");
        result.put("ProjectExperiment -> Date", getTimestamp());
        result.put("Folder (path)", "C:\\Windows");
        result.put("Instrument Info -> Device Name", "Alcohol mashine");
        result.put("Instrument Info -> Detector", "What is Detector?");
        result.put("Instrument Info -> Objective ->", "Hmm... ");
        result.put("Instrument Info -> Magnification", "Common, what is this?");
        result.put("Instrument Info -> Filters", "Are you kiddin me?");
        result.put("Image properties -> X-dimension", 15);
        result.put("Image properties -> Y-dimension", 85);
        result.put("Image properties -> Z-dimension", 168);
        result.put("Image properties -> number of time points", 1586);
        result.put("Image properties -> number of channels", 852);
        result.put("Image properties -> umber of scan areas/platesNotes", 3);

        return result;
    }

    private String getTimestamp() {
        //("yyyy-MM-dd'T'HH:mm:ss'Z'")
        return LocalDateTime.now().toString();
    }

}
