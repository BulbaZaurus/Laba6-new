package Networking;

import java.net.InetAddress;
import java.net.SocketAddress;

/**
 * Отвечает за пересылку комманд
 */
public class Message {
    public Object content;
    public SocketAddress address;

    public Message(Object content, SocketAddress address) {
        this.content = content;
        this.address = address;
    }
}
