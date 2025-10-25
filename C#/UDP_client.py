import socket
import time

client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
server_address = ('localhost', 12345)

for i in range(5):
    msg = f"{i}|Hello UDP {i}"
    client.sendto(msg.encode(), server_address)
    
    # Chờ ACK
    client.settimeout(1)
    try:
        data, _ = client.recvfrom(1024)
        print("Received:", data.decode())
    except socket.timeout:
        print(f"Timeout for Seq={i}, consider resending...")
    
    time.sleep(0.5)
