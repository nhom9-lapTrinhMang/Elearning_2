import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class UDPClient {
    public static void main(String[] args) {
        final String SERVER = "127.0.0.1"; // loopback
        final int PORT = 12345;
        final int NUM_MESSAGES = 5;
        final int TIMEOUT_MS = 1000; // wait for ACK 1s
        final int MAX_RETRIES = 3;

        Random rnd = new Random();

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress serverAddr = InetAddress.getByName(SERVER);

            for (int seq = 0; seq < NUM_MESSAGES; seq++) {
                String payload = "Hello from client " + seq;
                String packetStr = seq + ":" + payload;
                byte[] data = packetStr.getBytes("UTF-8");

                boolean acked = false;
                int tries = 0;

                while (!acked && tries <= MAX_RETRIES) {
                    tries++;
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverAddr, PORT);
                    socket.send(sendPacket);
                    System.out.println("Sent (try " + tries + "): Seq=" + seq + ", Msg=\"" + payload + "\"");

                    try {
                        // wait for ACK
                        byte[] buf = new byte[1024];
                        DatagramPacket recvPacket = new DatagramPacket(buf, buf.length);
                        socket.receive(recvPacket);
                        String resp = new String(recvPacket.getData(), 0, recvPacket.getLength(), "UTF-8").trim();

                        if (resp.startsWith("ACK:")) {
                            String num = resp.substring(4);
                            try {
                                int ackSeq = Integer.parseInt(num);
                                if (ackSeq == seq) {
                                    System.out.println("Received ACK for seq " + seq);
                                    acked = true;
                                } else {
                                    System.out.println("Received ACK for other seq: " + ackSeq + " (expected " + seq + ")");
                                }
                            } catch (NumberFormatException nfe) {
                                System.out.println("Malformed ACK: " + resp);
                            }
                        } else {
                            System.out.println("Received non-ACK response: " + resp);
                        }
                    } catch (java.net.SocketTimeoutException ste) {
                        System.out.println("Timeout waiting for ACK for seq " + seq + " (try " + tries + ")");
                        if (tries > MAX_RETRIES) {
                            System.out.println("Max retries reached for seq " + seq + ". Giving up.");
                        } else {
                            // optional small backoff
                            try { Thread.sleep(200 + rnd.nextInt(300)); } catch (InterruptedException ie) {}
                        }
                    }
                } // end while

                // optional pause between messages
                try { Thread.sleep(300); } catch (InterruptedException e) {}
            } // end for

            System.out.println("Client finished sending messages.");
        } catch (Exception ex) {
            System.err.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
