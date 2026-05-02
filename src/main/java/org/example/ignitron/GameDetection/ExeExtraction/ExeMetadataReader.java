package org.example.ignitron.GameDetection.ExeExtraction;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.example.ignitron.Log;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class ExeMetadataReader {

    public static ExeMetadata read(Path exePath) {

        // Get the Metadata Block Size
        WString file = new WString(exePath.toString());
        IntByReference handle = new IntByReference();

        Log.info("Reading metadata for: " + exePath);
        int size = Version.INSTANCE.GetFileVersionInfoSizeW(file, handle);

        if (size == 0) {
            Log.warn("No version info found for: " + exePath);
            return null;
        }
        Log.fine("Metadata block size: " + size + " bytes");

        // Allocate Buffer & Read Metadata
        Memory buffer = new Memory(size);

        boolean ok = Version.INSTANCE.GetFileVersionInfoW(file, handle.getValue(), size, buffer);

        if(!ok) return null;

        // Read the Translation Table
        PointerByReference translationPtr = new PointerByReference();
        IntByReference translationLen = new IntByReference();

        boolean transOk = Version.INSTANCE.VerQueryValueW(
                buffer,
                new WString("\\VarFileInfo\\Translation"),
                translationPtr,
                translationLen
        );

        if (!transOk) {
            Log.warn("Failed to read translation table for: " + exePath);
            return null;
        }

        ByteBuffer bb = translationPtr.getValue().getByteBuffer(0, translationLen.getValue());
        int lang = bb.getShort() & 0xFFFF;
        int codepage = bb.getShort() & 0xFFFF;

        Log.fine("Translation table length: " + translationLen.getValue());
        Log.fine(String.format("Using language=%04x, codepage=%04x", lang, codepage));

        String block = String.format("%04x%04x", lang, codepage);

        // Query Individual Fields
        ExeMetadata data = new ExeMetadata();

        data.setProductName(queryString(buffer, block, "ProductName"));
        data.fileDescription = queryString(buffer, block, "FileDescription");
        data.companyName = queryString(buffer, block, "CompanyName");
        data.productVersion = queryString(buffer, block, "ProductVersion");
        data.fileVersion = queryString(buffer, block, "FileVersion");

        Log.info("Metadata extracted: " +
                "ProductName=" + data.getProductName() + ", " +
                "FileDescription=" + data.fileDescription + ", " +
                "CompanyName=" + data.companyName + ", " +
                "ProductVersion=" + data.productVersion + ", " +
                "FileVersion=" + data.fileVersion
        );


        return data;

    }

    private static String queryString(Pointer buffer, String block, String key) {
        String path = "\\StringFileInfo\\" + block + "\\" + key;

        PointerByReference ptr = new PointerByReference();
        IntByReference len = new IntByReference();

        boolean ok = Version.INSTANCE.VerQueryValueW(buffer, new WString(path), ptr, len);
        if (!ok) {
            Log.fine("Failed to read " + key + " from " + path);
            return null;
        }

        String value = ptr.getValue().getWideString(0);
        Log.fine("Extracted " + key + ": " + value);

        return value;
    }

}
