package com.max.blepro.util;

import android.content.Context;
import android.content.res.AssetManager;

import com.max.blepro.service.BluetoothLeService;

import java.io.IOException;
import java.io.InputStream;

/**
 * @ClassName UpdateCommand
 * @Description TODO
 * @Author max
 * @Date 2023/5/27 11:16
 * @Version 1.0.0
 */
public class UpdateCommand {

    private static final String TAG = "UpdateCommand";
    private static UpdateCommand instance = null;
    private int wTimer = 0;
    private int filePackages = 0;
    private final static int EVERY_PACKAGE_DATA_LENGTH = 16;
    public static UpdateCommand getInstance() {

        if (null == instance)
            instance = new UpdateCommand();
        return instance;
    }

    private int getCRC(byte[] bytes) {
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;

        int i, j;
        for (i = 0; i < bytes.length-2; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        return (CRC);
    }

    public void openLockCmd(BluetoothLeService bluetoothLeService){
        byte [] lockOn = {0x06,0x02,0x00,0x00,0x00,0x00};
        int crc = UpdateCommand.getInstance().getCRC(lockOn);
        lockOn[lockOn.length-1] = (byte)(crc >> 8);
        lockOn[lockOn.length-2] = (byte)(crc & (0xff));
        bluetoothLeService.writeSpecialCharacteristic(lockOn);
    }

    public void closeLockCmd(BluetoothLeService bluetoothLeService){
        byte [] lockOff = {0x07,0x02,0x00,0x00,0x00,0x00};
        int crc = UpdateCommand.getInstance().getCRC(lockOff);
        lockOff[lockOff.length-1] = (byte)(crc >> 8);
        lockOff[lockOff.length-2] = (byte)(crc & (0xff));
        bluetoothLeService.writeSpecialCharacteristic(lockOff);
    }

    public void uartUpgradeCompleted(BluetoothLeService bluetoothLeService) {
        byte[] complete_cmd = new byte[]{0x05,0x02,0x00,0x00,(byte)0xa0,0x5c};
        int crc = UpdateCommand.getInstance().getCRC(complete_cmd);
        complete_cmd[complete_cmd.length-1] = (byte)(crc >> 8);
        complete_cmd[complete_cmd.length-2] = (byte)(crc & (0xff));
        bluetoothLeService.writeSpecialCharacteristic(complete_cmd);
    }

    public void uartSendUpgradeCmd(BluetoothLeService bluetoothLeService){

        byte[] upgradeCmd = new byte[]{0x02,0x02,0x00,0x00,(byte)0xa0,0x5c};
        bluetoothLeService.writeSpecialCharacteristic(upgradeCmd);
    }

    public void uartGetMcuVersion(BluetoothLeService bluetoothLeService)
    {
        byte[] versionCmd = new byte[]{0x01,0x02,0x00,0x00,(byte)0xa0,0x18};

        LogUtil.e(TAG,"uartGetMcuVersion");
        bluetoothLeService.writeSpecialCharacteristic(versionCmd);
    }

    public void uartSendFileSize(Context context,BluetoothLeService bluetoothLeService){

        int fileSize = UpdateCommand.getInstance().getUpgradeFileLength(context);
        byte [] fileSizeCmd = new byte[]{0x03,0x04,0x00,0x00,0x00,0x00,(byte)0xc2,0x42};

        fileSizeCmd[2] = (byte)((fileSize >> 24) & 0xff);
        fileSizeCmd[3] = (byte)((fileSize >> 16) & 0xff);
        fileSizeCmd[4] = (byte)((fileSize >> 8) & 0xff);
        fileSizeCmd[5] = (byte)((fileSize) & 0xff);

        int crcSize = UpdateCommand.getInstance().getCRC(fileSizeCmd);
        fileSizeCmd[fileSizeCmd.length-1] = (byte)(crcSize >> 8);
        fileSizeCmd[fileSizeCmd.length-2] = (byte)(crcSize & (0xff));

        bluetoothLeService.writeSpecialCharacteristic(fileSizeCmd);
    }

    public void uartSendFile(Context context,BluetoothLeService bluetoothLeService){

        int fileSize = UpdateCommand.getInstance().getUpgradeFileLength(context);
        wTimer = 0;
        AssetManager assetManager = context.getAssets();
        if (fileSize % EVERY_PACKAGE_DATA_LENGTH == 0){

            filePackages = fileSize / EVERY_PACKAGE_DATA_LENGTH;
        }else{
            filePackages = fileSize / EVERY_PACKAGE_DATA_LENGTH + 1;
        }
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open("Application.bin");
            byte buffer[] = new byte[EVERY_PACKAGE_DATA_LENGTH];
            byte buffer2[] = new byte[EVERY_PACKAGE_DATA_LENGTH + 4];

            int len = 0;
            while ((len = inputStream.read(buffer,0,buffer.length))>0) {
                //buffer为读出来的二进制数据，长度1024，最后一段数据小于1024
                if (len != EVERY_PACKAGE_DATA_LENGTH){
                    for (int i=len;i<EVERY_PACKAGE_DATA_LENGTH;i++){
                        buffer[i] = (byte) 0xff;
                    }
                }

                System.arraycopy(buffer,0,buffer2,2,buffer.length);
                buffer2[0] = 0x04;
                buffer2[1] = (byte)0x10;
                int crc = UpdateCommand.getInstance().getCRC(buffer2);
                buffer2[buffer2.length-1] = (byte)(crc >> 8);
                buffer2[buffer2.length-2] = (byte)(crc & (0xff));
                LogUtil.d(TAG,"need send data " + ChangeTool.getInstance().byte2HexStr(buffer2));
                bluetoothLeService.writeSpecialCharacteristic(buffer2);
                Thread.sleep(30);
                wTimer++;
                if(this.mOnReceiveUartUpgradeListener != null)
                    mOnReceiveUartUpgradeListener.OnReceiveUartUpgradeProcess((int)((wTimer * 100)/filePackages));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getUpgradeFileLength(Context context)
    {
        InputStream inputStream;
        int fileSize;
        AssetManager assetManager = context.getAssets();
        try {
            inputStream = assetManager.open("Application.bin");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            fileSize = inputStream.available();
            inputStream.close();
            return fileSize;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private  OnReceiveUartUpgradeListener mOnReceiveUartUpgradeListener = null;
    public interface OnReceiveUartUpgradeListener{
        void OnReceiveUartUpgradeProcess(int process);
    }

    public void setOnReceiveUartUpgradeListener(OnReceiveUartUpgradeListener listener){

        this.mOnReceiveUartUpgradeListener = listener;
    }
}
