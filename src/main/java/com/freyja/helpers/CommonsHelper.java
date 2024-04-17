package com.freyja.helpers;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

public class CommonsHelper {

    private CommonsHelper () {
        throw new IllegalStateException("Utility class");
    }

    // Helper method to format file size in a human-readable format
    public static String formatSize(long size) {
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double formattedSize = size;

        while (formattedSize >= 1024 && unitIndex < units.length - 1) {
            formattedSize /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", formattedSize, units[unitIndex]);
    }

    public static int downloadFile (URL src, String dest) {

        try {
            FileUtils.copyURLToFile(
                    src,
                    new File(dest),
                    8000,
                    8000);
        } catch (IOException e) {
            Logger.getLogger("Something went wrong! Either the link is now invalid or a write error has occurred!");
            e.printStackTrace();
        }

        return 0;
    }

}
