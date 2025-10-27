<?php
// UDP Server - Mô phỏng nhận gói tin, gửi ACK, có độ trễ xử lý nhỏ

$serverIP = "127.0.0.1";
$serverPort = 12000;
$socket = socket_create(AF_INET, SOCK_DGRAM, SOL_UDP);

if (!$socket) {
    die("❌ Không thể tạo socket\n");
}

if (!socket_bind($socket, $serverIP, $serverPort)) {
    die("❌ Không thể bind tới {$serverIP}:{$serverPort}\n");
}

echo "✅ UDP Server đang chạy tại {$serverIP}:{$serverPort}\n";

$buffer = [];

while (true) {
    $from = '';
    $port = 0;
    $bytes = socket_recvfrom($socket, $data, 1024, 0, $from, $port);

    if ($bytes === false) {
        continue;
    }

    // Giả lập xử lý chậm (jitter/delay)
    usleep(50000); // 50ms

    $buffer[] = $data;
    echo "📩 Nhận: {$data} từ {$from}:{$port}\n";

    // Gửi ACK phản hồi
    $ack = "ACK:{$data}";
    socket_sendto($socket, $ack, strlen($ack), 0, $from, $port);
}
