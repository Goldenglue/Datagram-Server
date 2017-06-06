package serverpack;

/**
 * Created by IvanOP on 04.05.2017.
 */


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

class Connection {
    private static DatagramSocket socketInput;
    private static DatagramSocket socketOutput;
    private static DatagramPacket packetOfData;
    private static InetAddress address;
    ByteArrayOutputStream byteArrayOutputStream;
    private static int inputPortNumber = 4020;
    private static int outputPortNumber = 4021;
    private boolean isConnected = false;
    private static Executor executor;
    private Thread runConnectionThread;
    private static byte[] bufferForData = new byte[256];

    private Runnable runConnection = () -> {
        while (true) {
            while (isConnected) {
                String receivedData[] = receivePacketOfData();
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


    static void sendPacketOfData(String command, String data) {

        bufferForData =  command.getBytes();
        System.out.println("sending command " + new String(bufferForData));
        packetOfData = new DatagramPacket(bufferForData,bufferForData.length,address,outputPortNumber);
        try {
            socketOutput.send(packetOfData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bufferForData = data.getBytes();
        if (bufferForData.length > 256) {
            int offset = 0;
            while (offset < bufferForData.length) {
                byte[] tempArray;
                if (offset + 256 < bufferForData.length) {
                    tempArray = Arrays.copyOfRange(bufferForData, offset, offset + 256);
                    offset += 256;
                } else {
                    tempArray = Arrays.copyOfRange(bufferForData, offset, bufferForData.length);
                    offset += 256;
                }
                System.out.println("sending " + new String(tempArray) );
                packetOfData = new DatagramPacket(tempArray, tempArray.length, address, outputPortNumber);
                try {
                    socketOutput.send(packetOfData);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else {
            packetOfData = new DatagramPacket(bufferForData, bufferForData.length, address, outputPortNumber);
            System.out.println("sending " + new String(bufferForData));
            try {
                socketOutput.send(packetOfData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bufferForData = "end".getBytes();
        packetOfData = new DatagramPacket(bufferForData,bufferForData.length,address,outputPortNumber);
        try {
            socketOutput.send(packetOfData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendPacketOfData(String command) {
        bufferForData =  command.getBytes();
        System.out.println("sending command " + new String(bufferForData));
        packetOfData = new DatagramPacket(bufferForData,bufferForData.length,address,outputPortNumber);
        try {
            socketOutput.send(packetOfData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bufferForData = "end".getBytes();
        packetOfData = new DatagramPacket(bufferForData,bufferForData.length,address,outputPortNumber);
        try {
            socketOutput.send(packetOfData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String[] receivePacketOfData() {
        bufferForData = new byte[256];
        packetOfData = new DatagramPacket(bufferForData, bufferForData.length);
        String[] everythingThatIsNeeded = new String[2];
        String whateverCame = null;
        String command = null;
        StringBuilder builder = new StringBuilder();
        try {
            socketInput.receive(packetOfData);
            command = new String(packetOfData.getData());
            command = command.substring(0,6);
            System.out.println("command is " + command);
            while (true) {
                bufferForData = new byte[256];
                packetOfData = new DatagramPacket(bufferForData, bufferForData.length);
                socketInput.receive(packetOfData);
                String receivedData = new String(packetOfData.getData());
                System.out.println(receivedData);
                if (Objects.equals(receivedData.substring(0,3), "end")) {
                    System.out.println("ending");
                    break;
                } else {
                    builder.append(receivedData);
                }
            }
            whateverCame = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        everythingThatIsNeeded[0] = command;
        everythingThatIsNeeded[1] = whateverCame;
        return everythingThatIsNeeded;
    }

    static void processCommand(String serverCommand) {
        executor.executeMessageFromClient(serverCommand);
    }
}
