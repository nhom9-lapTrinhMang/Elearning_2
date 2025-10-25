import socket

server = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
server.bind(('localhost', 12345))
print("Server listening...")

received_ids = set()

while True:
    data, addr = server.recvfrom(1024)
    seq, msg = data.decode().split('|', 1)
    print(f"Received: Seq={seq}, Msg={msg}")
    
    if seq not in received_ids:
        received_ids.add(seq)
        server.sendto(f"ACK:{seq}".encode(), addr)
