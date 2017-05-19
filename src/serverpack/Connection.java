package serverpack;

/**
 * Created by IvanOP on 04.05.2017.
 */


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

class Connection {
    private static DatagramSocket socketInput;
    private static DatagramSocket socketOutput;
    private static DatagramPacket packetOfDataSize;
    private static DatagramPacket packetOfData;
    private static InetAddress address;
    ByteArrayOutputStream byteArrayOutputStream;
    private static int inputPortNumber = 4020;
    private static int outputPortNumber = 4021;
    private boolean isConnected = false;
    private Executor executor;
    private Thread runConnectionThread;
    private static byte[] bufferForDataSize = new byte[4];
    private static byte[] bufferForData;

    private Runnable runConnection = () -> {
        while (true) {
            while (isConnected) {
                System.out.println("trying to receive");
                int size = receivePacketOfDataSize();
                System.out.println(size);
                String receivedData = receivePacketOfData(size);
                System.out.println(receivedData);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*String serverCommandMessage = setMessage();
                executor.executeMessageFromClient(serverCommandMessage);*/
            }
        }
    };

    Connection() {
        executor = new Executor(this);

        try {
            address = InetAddress.getByName("localhost");
            socketInput = new DatagramSocket(inputPortNumber, address);
            socketOutput = new DatagramSocket();
            isConnected = true;
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
        sendPacketOfDataSize("privet s servera!");
        sendPacketOfData("privet s servera!");

        runConnectionThread = new Thread(runConnection);
        runConnectionThread.start();
    }

    static void sendPacketOfDataSize(String data) {
        int size = data.length();
        bufferForDataSize = ByteBuffer.allocate(4).putInt(size).array();
        packetOfDataSize = new DatagramPacket(bufferForDataSize, bufferForDataSize.length, address, outputPortNumber);
        try {
            socketOutput.send(packetOfDataSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendPacketOfData(String data) {
        bufferForData = data.getBytes();
        packetOfData = new DatagramPacket(bufferForData, bufferForData.length, address, outputPortNumber);
        try {
            socketOutput.send(packetOfData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static int receivePacketOfDataSize() {
        packetOfDataSize = new DatagramPacket(bufferForDataSize, bufferForDataSize.length);
        try {
            socketInput.receive(packetOfDataSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ByteBuffer.wrap(packetOfDataSize.getData()).getInt();
    }

    static String receivePacketOfData(int size) {
        bufferForData = new byte[size];
        packetOfData = new DatagramPacket(bufferForData, bufferForData.length);
        try {
            socketInput.receive(packetOfData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(packetOfData.getData());
    }

    private String setMessage() {
        return ServerUI.getMessageToClient();
    }
}
