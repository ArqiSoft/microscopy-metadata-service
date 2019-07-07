package com.arqisoft.microscopymetadata.domain;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import ome.units.quantity.Length;
import ome.units.UNITS;
import ome.units.unit.Unit;

public class MicroscopyMetadataExtractor {

    public static Map<String, Object> readPhysicalSize(final String inputFile) {
        Map<String, Object> result = new HashMap<>();
        try {
            try (ImageReader reader = new ImageReader()) {
                final IMetadata omeMeta = MetadataTools.createOMEXMLMetadata();
                reader.setMetadataStore(omeMeta);
                reader.setId(inputFile);
                final Unit<Length> targetUnit = UNITS.MICROMETER;
                
                for (int image = 0; image < omeMeta.getImageCount(); image++) {
                    final Length physSizeX = omeMeta.getPixelsPhysicalSizeX(image);
                    final Length physSizeY = omeMeta.getPixelsPhysicalSizeY(image);
                    final Length physSizeZ = omeMeta.getPixelsPhysicalSizeZ(image);
                    
                    System.out.println("Physical calibration - Image: " + image);
                    
                    if (physSizeX != null) {
                        final Length convertedSizeX = new Length(physSizeX.value(targetUnit), targetUnit);
                        result.put("Physical calibration - Image X", convertedSizeX.value() + " " + convertedSizeX.unit().getSymbol());
                        System.out.println("\tX = " + physSizeX.value() + " " + physSizeX.unit().getSymbol()
                                + " = " + convertedSizeX.value() + " " + convertedSizeX.unit().getSymbol());
                    }
                    if (physSizeY != null) {
                        final Length convertedSizeY = new Length(physSizeY.value(targetUnit), targetUnit);
                        result.put("Physical calibration - Image Y", convertedSizeY.value() + " " + convertedSizeY.unit().getSymbol());
                        System.out.println("\tY = " + physSizeY.value() + " " + physSizeY.unit().getSymbol()
                                + " = " + convertedSizeY.value() + " " + convertedSizeY.unit().getSymbol());
                    }
                    if (physSizeZ != null) {
                        final Length convertedSizeZ = new Length(physSizeZ.value(targetUnit), targetUnit);
                        result.put("Physical calibration - Image Z", convertedSizeZ.value() + " " + convertedSizeZ.unit().getSymbol());
                        System.out.println("\tZ = " + physSizeZ.value() + " " + physSizeZ.unit().getSymbol()
                                + " = " + convertedSizeZ.value() + " " + convertedSizeZ.unit().getSymbol());
                    }
                }
            }
            
        } catch (FormatException | IOException ex) {
            Logger.getLogger(MicroscopyMetadataExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static Map<String, Object> printPixelDimensions(IFormatReader reader) {
        // output dimensional information
        int sizeX = reader.getSizeX();
        int sizeY = reader.getSizeY();
        int sizeZ = reader.getSizeZ();
        int sizeC = reader.getSizeC();
        int sizeT = reader.getSizeT();
        int imageCount = reader.getImageCount();
        Map<String, Object> result = new HashMap<>();
        //result.put("Pixel dimensions:");
        result.put("Width", sizeX);
        result.put("Height", sizeY);
        result.put("Focal planes", sizeZ);
        result.put("Channels", sizeC);
        result.put("Timepoints", sizeT);
        result.put("Total planes", imageCount);
        return result;
    }

    /**
     * Outputs global timing details.
     */
    public static Map<String, Object> printPhysicalDimensions(IMetadata meta, int series) {
        Length physicalSizeX = meta.getPixelsPhysicalSizeX(series);
        Length physicalSizeY = meta.getPixelsPhysicalSizeY(series);
        Length physicalSizeZ = meta.getPixelsPhysicalSizeZ(series);
        Map<String, Object> result = new HashMap<>();
        if (physicalSizeX != null )
        {
            result.put("X spacing", physicalSizeX.value() + " " + physicalSizeX.unit().getSymbol());
        }
        
        if (physicalSizeY != null)
        {
            result.put("Y spacing", physicalSizeY.value() + " " + physicalSizeY.unit().getSymbol());
        }
        
        if(physicalSizeZ != null)
        {
            result.put("Z spacing", physicalSizeZ.value() + " " + physicalSizeZ.unit().getSymbol());
        }
        return result;
    }

    public static Map<String, Object> printLensNA(String id) {

        Map<String, Object> result = new HashMap<>();
        try {
            // configure reader
            IFormatReader reader = new ImageReader();
            ServiceFactory factory = new ServiceFactory();
            OMEXMLService service = factory.getInstance(OMEXMLService.class);
            IMetadata meta = service.createOMEXMLMetadata();
            reader.setMetadataStore(meta);
            System.out.println("Initializing file: " + id);
            reader.setId(id); // parse metadata
            // output metadata values
            int instrumentCount = meta.getInstrumentCount();
            result.put("Instrument(s) associated with this file", instrumentCount);
            for (int i = 0; i < instrumentCount; i++) {
                int objectiveCount = meta.getObjectiveCount(i);
                result.put("Instrument #" + i + " [" + meta.getInstrumentID(i) + "]: ", objectiveCount + " objective(s) found");
                for (int o = 0; o < objectiveCount; o++) {
                    Double lensNA = meta.getObjectiveLensNA(i, o);
                    result.put("\tObjective #" + o + " [" + meta.getObjectiveID(i, o) + "]: LensNA", lensNA);
                }
            }
            
        } catch (DependencyException | FormatException | IOException | ServiceException ex) {
            Logger.getLogger(MicroscopyMetadataExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static Map<String, Object> calculateSubresolution(String id) {
        Map<String, Object> result = new HashMap<>();
        try {
            try (IFormatReader reader = new ImageReader()) {
                reader.setFlattenedResolutions(false);
                System.out.println("Initializing file: " + id);
                reader.setId(id); // parse metadata
                int seriesCount = reader.getSeriesCount();
                
                result.put("Series count", seriesCount);
                
                for (int series = 0; series < seriesCount; series++) {
                    reader.setSeries(series);
                    int resolutionCount = reader.getResolutionCount();
                    
                    result.put("Resolution count for series #" + series, resolutionCount);
                    
                    for (int r = 0; r < resolutionCount; r++) {
                        reader.setResolution(r);
                        result.put("Resolution #" + r + " dimensions", reader.getSizeX() + " x " + reader.getSizeY());
                    }
                }
            }
            
        } catch (FormatException | IOException ex) {
            Logger.getLogger(MicroscopyMetadataExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
