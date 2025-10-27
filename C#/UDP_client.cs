using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

class UdpClientExample
{
    static async Task Main()
    {
        using var client = new UdpClient();
        var serverEndpoint = new IPEndPoint(IPAddress.Loopback, 12345);

        for (int i = 0; i < 5; i++)
        {
            string msg = $"{i}|Hello UDP {i}";
            byte[] data = Encoding.UTF8.GetBytes(msg);
            await client.SendAsync(data, data.Length, serverEndpoint);

            // Chờ ACK với timeout 1 giây
            var task = client.ReceiveAsync();
            if (await Task.WhenAny(task, Task.Delay(1000)) == task)
            {
                var result = task.Result;
                Console.WriteLine("Received: " + Encoding.UTF8.GetString(result.Buffer));
            }
            else
            {
                Console.WriteLine($"Timeout for Seq={i}, consider resending...");
            }

            await Task.Delay(500);
        }
    }
}
