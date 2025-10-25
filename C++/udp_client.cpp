#include <iostream>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <vector>
#include <string>

#pragma comment(lib, "ws2_32.lib")

#define SERVER_IP "127.0.0.1"
#define SERVER_PORT 8080

int main() {
    WSADATA wsaData;
    SOCKET sockfd;
    sockaddr_in serverAddr;
    int serverLen = sizeof(serverAddr);
    char buffer[1024];

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

    int sendBuf = 1 << 20;
    setsockopt(sockfd, SOL_SOCKET, SO_SNDBUF, (char*)&sendBuf, sizeof(sendBuf));

    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(SERVER_PORT);
    inet_pton(AF_INET, SERVER_IP, &serverAddr.sin_addr);

    std::vector<std::string> messages = {
        "Hello Server 1", "Hello Server 2", "Hello Server 3",
        "Hello Server 4", "Hello Server 5", "Hello Server 6"
    };

    for (size_t i = 0; i < messages.size(); i += 2) {
        std::string batched = messages[i];
        if (i + 1 < messages.size()) batched += " | " + messages[i + 1];

        sendto(sockfd, batched.c_str(), (int)batched.size(), 0,
            (SOCKADDR*)&serverAddr, serverLen);

        std::cout << "[CLIENT] Gui goi: " << batched << std::endl;
        Sleep(200); // 200ms
    }

    int len = recvfrom(sockfd, buffer, sizeof(buffer) - 1, 0,
        (SOCKADDR*)&serverAddr, &serverLen);
    if (len > 0) {
        buffer[len] = '\0';
        std::cout << "[CLIENT] Nhan: " << buffer << std::endl;
    }

    closesocket(sockfd);
    WSACleanup();
    return 0;
}
