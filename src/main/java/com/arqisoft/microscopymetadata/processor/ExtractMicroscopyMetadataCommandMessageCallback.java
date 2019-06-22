package com.arqisoft.microscopymetadata.processor;

import java.util.concurrent.BlockingQueue;

import com.arqisoft.microscopymetadata.commands.ExtractMicroscopyMetadata;
import sds.messaging.callback.AbstractMessageCallback;

public class ExtractMicroscopyMetadataCommandMessageCallback extends AbstractMessageCallback<ExtractMicroscopyMetadata> {

    public ExtractMicroscopyMetadataCommandMessageCallback(Class<ExtractMicroscopyMetadata> tClass, BlockingQueue<ExtractMicroscopyMetadata> queue) {
        super(tClass, queue);
    }

}
