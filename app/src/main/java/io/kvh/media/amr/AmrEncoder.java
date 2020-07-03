package io.kvh.media.amr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dds
 */
public class AmrEncoder {
    final private static byte[] header = new byte[]{0x23, 0x21, 0x41, 0x4D, 0x52, 0x0A};

    public static void convertAMR(String inpath, String outpath) {
        try {
            AmrEncoder.init(0);
            File inFile = new File(inpath);
            List<short[]> armsList = new ArrayList<>();
            FileInputStream inputStream = new FileInputStream(inFile);
            FileOutputStream outStream = new FileOutputStream(outpath);
            //写入Amr头文件
            outStream.write(header);
            int byteSize = 320;
            byte[] buff = new byte[byteSize];
            while ((inputStream.read(buff, 0, byteSize)) > 0) {
                short[] shortTemp = new short[160];
                ByteBuffer.wrap(buff).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortTemp);
                armsList.add(shortTemp);
            }

            for (int i = 0; i < armsList.size(); i++) {
                int size = armsList.get(i).length;
                byte[] encodedData = new byte[size * 2];
                int len = AmrEncoder.encode(AmrEncoder.Mode.MR122.ordinal(), armsList.get(i), encodedData);
                if (len > 0) {
                    byte[] tempBuf = new byte[len];
                    System.arraycopy(encodedData, 0, tempBuf, 0, len);
                    outStream.write(tempBuf, 0, len);
                }
            }
          //  AmrEncoder.reset();
            AmrEncoder.exit();
            outStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public enum Mode {
        MR475,/* 4.75 kbps */
        MR515,    /* 5.15 kbps */
        MR59,     /* 5.90 kbps */
        MR67,     /* 6.70 kbps */
        MR74,     /* 7.40 kbps */
        MR795,    /* 7.95 kbps */
        MR102,    /* 10.2 kbps */
        MR122,    /* 12.2 kbps */
        MRDTX,    /* DTX       */
        N_MODES   /* Not Used  */
    }

    public static native void init(int dtx);

    public static native int encode(int mode, short[] in, byte[] out);

    public static native void reset();

    public static native void exit();

    static {
        System.loadLibrary("amr-codec");
    }
}
