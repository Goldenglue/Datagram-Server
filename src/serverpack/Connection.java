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
    private static DatagramPacket packetThatContainsSizeOfNextPacket;
    private static DatagramPacket packetThatContainsMessageItself;
    ByteArrayOutputStream byteArrayOutputStream;
    private int inputPortNumber = 4020;
    private int outputPortNumber = 4021;
    private boolean isConnected = false;
    private Executor executor;
    private Thread runConnectionThread;
    private byte[] bufferForIncomingMessageSize = new byte[4];
    //private byte[] bufferForIncomingMessage;

    private Runnable runConnection = () -> {
        while (true) {
            while (isConnected) {
                try {
                    socketInput.receive(packetThatContainsSizeOfNextPacket);
                    //packetThatContainsMessageItself = new DatagramPacket(bufferForIncomingMessage = new byte[getSizeOfNextPacket(),])
                    String clientCommandMessage = new String(packetThatContainsSizeOfNextPacket.getData());
                    System.out.println(clientCommandMessage);
                    executor.executeMessageFromClient(clientCommandMessage);

                    String serverCommandMessage = setMessage();
                    executor.executeMessageFromClient(serverCommandMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Connection() {
        executor = new Executor(this);

        try {
            InetAddress address = InetAddress.getByName("localhost");
            socketInput = new DatagramSocket(inputPortNumber, address);
            socketOutput = new DatagramSocket(outputPortNumber, address);
            packetThatContainsSizeOfNextPacket = new DatagramPacket(bufferForIncomingMessageSize, 4);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
        runConnectionThread = new Thread(runConnection);
        runConnectionThread.start();
    }

    private String setMessage() {
        return ServerUI.getMessageToClient();
    }

    static void sendPacket() {
        try {
            socketOutput.send(packetThatContainsSizeOfNextPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String receivePacket() {
        try {
            socketInput.receive(packetThatContainsSizeOfNextPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(packetThatContainsSizeOfNextPacket.getData());
    }

    private int getSizeOfNextPacket() {
        return ByteBuffer.wrap(packetThatContainsSizeOfNextPacket.getData()).getInt();
    }

}
