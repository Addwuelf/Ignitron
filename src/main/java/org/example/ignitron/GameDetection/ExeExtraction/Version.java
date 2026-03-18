package org.example.ignitron.GameDetection.ExeExtraction;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface Version  extends Library {
    Version INSTANCE = Native.load( "version", Version.class);

    int GetFileVersionInfoSizeW(WString filename, IntByReference Handle);
    boolean GetFileVersionInfoW(WString filename, int handle, int size, Pointer buffer);

    boolean VerQueryValueW(Pointer block, WString subBlock, PointerByReference buffer, IntByReference len);

}
