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
    private static Executor executor;
    private Thread runConnectionThread;
    private static byte[] bufferForDataSize = new byte[4];
    private static byte[] bufferForData;

    private Runnable runConnection = () -> {
        while (true) {
            while (isConnected) {
                String receivedData = receivePacketOfData();
                System.out.println("received message: " + receivedData);
                executor.executeMessageFromClient(receivedData);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Connection() {
        executor = new Executor();

        try {
            address = InetAddress.getByName("localhost");
            socketInput = new DatagramSocket(inputPortNumber, address);
            socketOutput = new DatagramSocket();
            isConnected = true;
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }


        runConnectionThread = new Thread(runConnection);
        runConnectionThread.start();
    }

    private static void sendPacketOfDataSize(String data) {
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
        sendPacketOfDataSize(data);
        System.out.println("sending: " + data);
        bufferForData = data.getBytes();
        packetOfData = new DatagramPacket(bufferForData, bufferForData.length, address, outputPortNumber);
        try {
            socketOutput.send(packetOfData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int receivePacketOfDataSize() {
        packetOfDataSize = new DatagramPacket(bufferForDataSize, bufferForDataSize.length);
        try {
            socketInput.receive(packetOfDataSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ByteBuffer.wrap(packetOfDataSize.getData()).getInt();
    }

    static String receivePacketOfData() {
        int size = receivePacketOfDataSize();
        bufferForData = new byte[size];
        packetOfData = new DatagramPacket(bufferForData, bufferForData.length);
        try {
            socketInput.receive(packetOfData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(packetOfData.getData());
    }

    static void processCommand(String serverCommand) {
        executor.executeMessageFromClient(serverCommand);
    }
}
