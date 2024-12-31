package com.revfad.strip_exif;

import java.io.BufferedOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;

import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.io.FileUtils;

public class ExifStripper {

    private File jpegFile;
    private Set<String> removed = new HashSet<>();
    private TiffOutputSet outputSet = null;
    private TiffOutputDirectory exifDirectory = null;
    private TiffOutputDirectory gpsDirectory = null;

    private static Map<String, TagInfo> nameToTag = ExifTagConstants.ALL_EXIF_TAGS.stream()
            .collect(Collectors.toMap(x -> x.name, Function.identity(), (e1, e2) -> e1));

    public ExifStripper(File jpegFile) throws IOException, ImagingException {
        this.jpegFile = jpegFile;
        ImageMetadata metadata = Imaging.getMetadata(jpegFile);
        JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        if (jpegMetadata == null) {
            return;
        }
        final TiffImageMetadata exif = jpegMetadata.getExif();
        if (exif == null) {
            return;
        }
        outputSet = exif.getOutputSet();
        if (outputSet != null) {
            exifDirectory = outputSet.getExifDirectory();
            gpsDirectory = outputSet.getGpsDirectory();
        }
    }

    public ExifStripper stripGps() {
        if (gpsDirectory != null) {
            for (TiffOutputField field : gpsDirectory.getFields()) {
                stripTag(gpsDirectory, field.tagInfo);
            }
        }
        return this;
    }

    public ExifStripper stripTag(String tagName) throws NoSuchElementException {
        TagInfo tag = nameToTag.get(tagName);
        if (tag == null) {
            throw new NoSuchElementException("Unknown EXIF tag: " + tagName);
        }
        stripTag(exifDirectory, tag);
        return this;
    }

    private void stripTag(TiffOutputDirectory dir, TagInfo tag) {
        if (dir != null) {
            if (dir.findField(tag) != null) {
                dir.removeField(tag);
                removed.add(tag.name);
            }
        }
    }

    public Set<String> write() throws IOException {
        File outputFile = File.createTempFile("stripped", "jpeg");
        if (removed.isEmpty()) {
            return removed;
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        OutputStream os = new BufferedOutputStream(fos);
        new ExifRewriter().updateExifMetadataLossless(jpegFile, os, outputSet);
        Files.move(outputFile.toPath(), jpegFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return removed;
    }
}