package com.example.netrasagar.rfid;

import java.util.Arrays;

public class Transform {

    public static byte[] getArray(byte[] array, int length) {
        byte[] dst = new byte[length];
        Arrays.fill(dst, (byte) 0);
        if (array != null) {
            if (array.length > length) {
                System.arraycopy(array, 0, dst, 0, length);
            } else {
                System.arraycopy(array, 0, dst, 0, array.length);
            }
        }
        return dst;
    }

    public static byte[] IntToBytes(int num) {
        if (num == 0) {
            return new byte[]{0x00};
        }

        byte[] b = new byte[4];
        int length = 0;
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (num >> (24 - i * 8));
            if (b[i] != 0x00 || i == 3)
                length++;
        }
        byte[] bReturn = new byte[length];
        System.arraycopy(b, 4 - length, bReturn, 0, length);
        return bReturn;
    }

    public static String Bytes2HexString(byte... src) {
        if (src == null)
            return null;
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < src.length; i++) {
            ret.append(String.format("%02X", src[i]));
        }
        return ret.toString();
    }

    public static String Bytes2String(byte[] src) {
        if (src == null)
            return null;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < src.length; i++) {
            sb.append((char) (src[i]));
        }
        return sb.toString();
    }
}
