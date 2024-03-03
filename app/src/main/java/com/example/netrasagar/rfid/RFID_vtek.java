package com.example.netrasagar.rfid;

import android.util.Log;

import com.cloudpos.DeviceException;
import com.cloudpos.OperationResult;
import com.cloudpos.card.Card;
import com.cloudpos.card.MifareCard;
import com.cloudpos.rfcardreader.RFCardReaderDevice;
import com.cloudpos.rfcardreader.RFCardReaderOperationResult;

public class RFID_vtek {
    private static final int checkcardTimeout = 10;
    public static String read_RFID_card(int sectorIndex, int blockIndex, Card rfCard) {
        try {
            byte[] resultname = ((MifareCard) rfCard).readBlock(sectorIndex, blockIndex);
            String output = HexString.hexToString(Transform.Bytes2HexString(resultname));
            return output;
        } catch (DeviceException e) {
            Log.d("RFID Read Error", e.toString());
            return "";
        }
    }

    public static Card get_RFID_card(RFCardReaderDevice RFID_Device) {
        try {
            OperationResult operationResult = RFID_Device.waitForCardPresent(checkcardTimeout*1000);
            if (operationResult.getResultCode() == OperationResult.SUCCESS) {
                Card rfCard = ((RFCardReaderOperationResult) operationResult).getCard();
                return rfCard;
            } else {
                return null;
            }
        } catch (DeviceException e) {
            Log.d("RFID Verify Error", e.toString());
            return null;
        }
    }

    public static boolean write_RFID_Data(int sectorIndex, int blockId, String data, Card rfCard) {
        try {
            if (data.length() > 16) {
                Log.d("RFID Write", "Data more than 16 characters");
                return false;
            } else {
                if (blockId > 2) {
                    Log.d("RFID Write", "Block " + blockId + " cannot be greater than '2'");
                    return false;
                } else {
                    data = data + new String(new char[16 - data.length()]).replace("\0", " ");
                    String hexstr = HexString.stringToHex(data);
                    byte[] writeData = HexString.hexToBuffer(hexstr);
                    ((MifareCard) rfCard).writeBlock(sectorIndex, blockId, writeData);
                    return true;
                }
            }
        } catch (DeviceException e) {
            Log.d("RFID Write Error", e.toString());
            return false;
        }
    }

    public static boolean verify_RFID_card(int sectorIndex, byte[] RFID_key, Card rfCard) {
        try {
            boolean verifyResult = ((MifareCard) rfCard).verifyKeyA(sectorIndex, RFID_key);
            if (verifyResult)
                return true;
            else
                return false;

        } catch (DeviceException e) {
            Log.d("RFID Verify Error", e.toString());
            return false;
        }
    }

}
