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
                System.out.println("YES SOMETHING HAPPEND");
                System.out.println("received message: " + receivedData[1]);
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


    static void sendPacketOfData(String data) {
        System.out.println("sending: " + data);
        bufferForData = data.getBytes();
        packetOfData = new DatagramPacket(bufferForData, bufferForData.length, address, outputPortNumber);
        try {
            socketOutput.send(packetOfData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] receivePacketOfData() {
        packetOfData = new DatagramPacket(bufferForData, bufferForData.length);
        String[] everythingThatIsNeeded = new String[2];
        String whateverCame = null;
        String command = null;
        StringBuilder builder = new StringBuilder();
        try {
            socketInput.receive(packetOfData);
            command = new String(packetOfData.getData());
            System.out.println("command is " + command);
            while (true) {
                bufferForData =  new byte[256];
                packetOfData = new DatagramPacket(bufferForData, bufferForData.length);
                socketInput.receive(packetOfData);
                String receivedData = new String(packetOfData.getData());
                System.out.println("received " + receivedData);
                if (receivedData.equalsIgnoreCase("end")) {
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
