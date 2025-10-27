using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

class UdpServerExample
{
    static async Task Main()
    {
        using var server = new UdpClient(12345);
        Console.WriteLine("Server listening...");

        var receivedIds = new HashSet<string>();

        while (true)
        {
            var result = await server.ReceiveAsync();
            string msg = Encoding.UTF8.GetString(result.Buffer);
            string[] parts = msg.Split('|', 2);
            string seq = parts[0];
            string content = parts.Length > 1 ? parts[1] : "";

            Console.WriteLine($"Received: Seq={seq}, Msg={content}");

            if (!receivedIds.Contains(seq))
            {
                receivedIds.Add(seq);
                byte[] ack = Encoding.UTF8.GetBytes($"ACK:{seq}");
                await server.SendAsync(ack, ack.Length, result.RemoteEndPoint);
            }
        }
    }
}
