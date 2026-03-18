package org.example.ignitron;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;


public class IconExtractor {

    public static Image extract32Icon(String exePath) {
        WinDef.HICON[] large = new WinDef.HICON[1];
        WinDef.HICON[] small = new WinDef.HICON[1];

        int count = Shell32.INSTANCE.ExtractIconEx(
                exePath,
                0,
                large,
                small,
                1
        );

        if (count <= 0 || large[0] == null) {
            return null;
        }

        WinGDI.ICONINFO info = new WinGDI.ICONINFO();
        if (!User32.INSTANCE.GetIconInfo(large[0], info)) {
            return null;
        }

        BufferedImage img = hbitmapToBuffered(info.hbmColor);
        if (img == null) return null;

        return SwingFXUtils.toFXImage(img, null);
    }

    public static String saveIconToFile(Image image, String gameName) {
        try {
            File dir = new File(System.getenv("APPDATA"), "Ignitron/icons");
            dir.mkdirs();

            File file = new File(dir, gameName + ".png");

            BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
            ImageIO.write(bImage, "png", file);

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static BufferedImage hbitmapToBuffered(WinDef.HBITMAP hBitmap) {
        WinGDI.BITMAP bmp = new WinGDI.BITMAP();
        GDI32.INSTANCE.GetObject(hBitmap, bmp.size(), bmp.getPointer());
        bmp.read();

        int width = bmp.bmWidth.intValue();
        int height = bmp.bmHeight.intValue();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[] pixels = new int[width * height];
        Memory buffer = new Memory(pixels.length * 4);

        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height;
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        WinDef.HDC hdc = User32.INSTANCE.GetDC(null);

        int res = GDI32.INSTANCE.GetDIBits(
                hdc,
                hBitmap,
                0,
                height,
                buffer,
                bmi,
                WinGDI.DIB_RGB_COLORS
        );

        User32.INSTANCE.ReleaseDC(null, hdc);

        if (res == 0) return null;

        buffer.read(0, pixels, 0, pixels.length);
        image.setRGB(0, 0, width, height, pixels, 0, width);

        return image;
    }

}