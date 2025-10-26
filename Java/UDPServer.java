import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;

public class UDPServer {
    public static void main(String[] args) {
        final int PORT = 12345;
        byte[] buffer = new byte[1024];
        HashSet<Integer> receivedSeq = new HashSet<>();

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            System.out.println("Server started. Listening on port " + PORT);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength(), "UTF-8").trim();
                InetAddress clientAddr = packet.getAddress();
                int clientPort = packet.getPort();

                // Expecting format: "SEQ:msg" or "SEQ|msg"
                int seq = -1;
                String msg = received;
                try {
                    // try "SEQ:msg"
                    if (received.contains(":")) {
                        String[] parts = received.split(":", 2);
                        seq = Integer.parseInt(parts[0]);
                        msg = parts[1];
                    } else if (received.contains("|")) {
                        String[] parts = received.split("\\|", 2);
                        seq = Integer.parseInt(parts[0]);
                        msg = parts[1];
                    } else {
                        // if not formatted, treat as seq=-1
                        msg = received;
                    }
                } catch (Exception e) {
                    // malformed, keep seq = -1
                    msg = received;
                }

                if (seq >= 0) {
                    if (receivedSeq.contains(seq)) {
                        System.out.println("Duplicate packet ignored. Seq=" + seq + ", Msg=\"" + msg + "\" from " + clientAddr + ":" + clientPort);
                    } else {
                        receivedSeq.add(seq);
                        System.out.println("Received: Seq=" + seq + ", Msg=\"" + msg + "\" from " + clientAddr + ":" + clientPort);
                    }

                    // send ACK back
                    String ack = "ACK:" + seq;
                    byte[] ackData = ack.getBytes("UTF-8");
                    DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, clientAddr, clientPort);
                    socket.send(ackPacket);
                    System.out.println("Sent: " + ack + " to " + clientAddr + ":" + clientPort);
                } else {
                    // no seq: echo back generic ACK
                    System.out.println("Received non-seq message: \"" + msg + "\" from " + clientAddr + ":" + clientPort);
                    String resp = "ACK:-1";
                    byte[] rd = resp.getBytes("UTF-8");
                    socket.send(new DatagramPacket(rd, rd.length, clientAddr, clientPort));
                }
            }
        } catch (Exception ex) {
            System.err.println("Server error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
