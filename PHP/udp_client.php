<?php
// UDP Client - Gửi gói tin có pacing và chờ ACK (ARQ)

$serverIP = "127.0.0.1";
$serverPort = 12000;

$socket = socket_create(AF_INET, SOCK_DGRAM, SOL_UDP);
socket_set_option($socket, SOL_SOCKET, SO_RCVTIMEO, ["sec" => 1, "usec" => 0]); // Timeout 1s

echo "🚀 UDP Client khởi động, gửi dữ liệu đến {$serverIP}:{$serverPort}\n";

for ($i = 1; $i <= 10; $i++) {
    $message = "Packet-{$i}";
    socket_sendto($socket, $message, strlen($message), 0, $serverIP, $serverPort);
    echo "📤 Gửi: {$message}\n";

    // Chờ ACK (ARQ)
    $from = '';
    $port = 0;
    $buf = '';
    $bytes = @socket_recvfrom($socket, $buf, 1024, 0, $from, $port);

    if ($bytes === false) {
        echo "⚠️ Mất ACK cho {$message} — gửi lại...\n";
        socket_sendto($socket, $message, strlen($message), 0, $serverIP, $serverPort);
    } else {
        echo "✅ Nhận phản hồi: {$buf}\n";
    }

    // pacing: giữ nhịp gửi (giúp giảm tràn mạng)
    usleep(200000); // 200ms
}

socket_close($socket);
