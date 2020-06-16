package Networking;

import java.io.*;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;



import сom.company.Controller;
import сom.company.command.Command;
import сom.company.command.Command_Exit;
import сom.company.command.Command_Save;

/**
 * @author Длинный
 */
public class Server extends Networking {
    private static DatagramChannel channel;
    private static InputStream inputStreamFromClient;
    private static InputStream inputStreamFromSystem;
    private static  OutputStream outputStreamFromServer;
    private static OutputStream outputStreamFromSystem;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Syntax: Networking.Server <port>");
            return;
        }
        Controller.main(new String[]{"savePath.xml", "true"});

        InitConnection(Integer.parseInt(args[0]));

        Process();
    }
    /**
     * Иннициализация соеденения
     * @param port Порт
     */
    public static void InitConnection(int port) {
        boolean hasError = true;
        while(hasError) {
            try {
                channel = DatagramChannel.open();
                channel.configureBlocking(false);
                channel.socket().bind(new InetSocketAddress(port));
                Thread.sleep(300);

                hasError=false;
            } catch (UnknownHostException ex) {
                System.out.println("Host error: " + ex.getMessage());
                hasError=true;
            } catch (SocketException ex) {
                System.out.println("Socket error: " + ex.getMessage());
                hasError=true;
            } catch (IOException ex) {
                System.out.println("I/O error when initializing connection.");
                hasError=true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                hasError=true;
            }
        }
    }
    /**
     * Фактически метод обеспечивает работу класса Server
     * Проверяет комманды на соответсвие с различными командами
     */
    private static void Process() {
        while(true){
            //receive msg

            Message receivedMessage = ReceiveMessage(channel,true);

            if(receivedMessage!=null && receivedMessage.content!=null) {
                if(receivedMessage.content instanceof Command_Exit){
                    CommandToCollection(new Command_Save());
                }
                String[] output = CommandToCollection((Command) receivedMessage.content);

                //send constructed message to the client
                SendMessage(output, channel, receivedMessage.address,true);
            }
        }
    }

    private static String[] CommandToCollection(Command command){
        PrintStream oldStream = System.out;
        List<String> output = new ArrayList<String>();
        PrintStream myStream = new PrintStream(System.out) {
            @Override
            public void println(String x) {
                super.println(x);
                output.add(x);
            }
        };
        System.setOut(myStream);
        Controller.ExecuteCommand(command);
        System.setOut(oldStream);
        String[] strA = new String[output.size()];
        output.toArray(strA);
        return strA;
    }

}