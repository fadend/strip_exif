package com.revfad.strip_exif;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.revfad.strip_exif.ExifStripper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command()
public class App implements Runnable {

    @Option(names = { "--jpeg-file" }, required = true, description = "File to be stripped.")
    private String jpegFile = "";

    @Option(names = { "--strip-gps" }, description = "Whether to strip GPS tags.")
    private boolean stripGps = true;

    @Option(names = { "--tags" }, split = ",", description = "Comma-separated list of tags to strip.")
    private Set<String> tagsToStrip = new HashSet<>();

    @Override
    public void run() {
        try {
            ExifStripper stripper = new ExifStripper(new File(jpegFile));
            if (stripGps) {
                System.out.println("Attempting to strip GPS tags");
                stripper.stripGps();
            }
            for (String tag : tagsToStrip) {
                stripper.stripTag(tag);
            }
            Set<String> removedTags = stripper.write();
            if (removedTags.isEmpty()) {
                System.out.println("Didn't change " + jpegFile);
            } else {
                System.out.println("Removed the following from " + jpegFile);
                for (String tag : removedTags) {
                    System.out.println("  " + tag);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
