#include <iostream>
#include <winsock2.h>
#include <ws2tcpip.h>

#pragma comment(lib, "ws2_32.lib")

#define PORT 8080
#define BUFFER_SIZE 4096

int main() {
    WSADATA wsaData;
    SOCKET sockfd;
    sockaddr_in serverAddr, clientAddr;
    int addrLen = sizeof(clientAddr);
    char buffer[BUFFER_SIZE];

    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        std::cerr << "WSAStartup that bai\n";
        return 1;
    }

    sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sockfd == INVALID_SOCKET) {
        std::cerr << "Tao socket that bai\n";
        WSACleanup();
        return 1;
    }

    int recvBuf = 1 << 20; // 1MB
    setsockopt(sockfd, SOL_SOCKET, SO_RCVBUF, (char*)&recvBuf, sizeof(recvBuf));

    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port = htons(PORT);

    if (bind(sockfd, (SOCKADDR*)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR) {
        std::cerr << "Bind that bai\n";
        closesocket(sockfd);
        WSACleanup();
        return 1;
    }

    std::cout << "[SERVER] Dang cho du lieu tren cong " << PORT << "...\n";
    int packetCount = 0;

    while (true) {
        int len = recvfrom(sockfd, buffer, BUFFER_SIZE - 1, 0,
            (SOCKADDR*)&clientAddr, &addrLen);
        if (len == SOCKET_ERROR) continue;
        buffer[len] = '\0';
        packetCount++;
        std::cout << "[SERVER] Nhan goi #" << packetCount << ": " << buffer << std::endl;

        if (packetCount % 5 == 0) {
            const char* ack = "ACK";
            sendto(sockfd, ack, strlen(ack), 0,
                (SOCKADDR*)&clientAddr, addrLen);
            std::cout << "[SERVER] Gui ACK\n";
        }
    }

    closesocket(sockfd);
    WSACleanup();
    return 0;
}
