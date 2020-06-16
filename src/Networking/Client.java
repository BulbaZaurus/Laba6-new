package Networking;






import сom.company.Controller;
import сom.company.command.*;
import сom.company.command.CommandBuilder;

import java.io.*;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.util.*;

/**
 * @author }{отт@бь)ч
 * @version 1.8
 */
public class Client extends Networking{
    public static SocketAddress ADDRESS;
    private static DatagramChannel channel;
    private static Scanner scanner;
    private InputStream inputStream;
    private static List<String> executed_scripts= new ArrayList<String>();
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Syntax: Networking.Client <hostname> <port>");
            return;
        }

        InitReader();
        InitConnection(args[0], args[1]);

        Process();
    }

    /**
     * Фактически метод обеспечивает работу класса Client
     * Проверяет комманды на соответсвие с различными командами
     */
    public static void Process() {
        while (true) {
            //create a command that will be sent to the server
            System.out.println("Для срочного отключения клиента нажмите сочетание клавиш ctrl+c ,а после нажмите enter");
            System.out.println("Однако это не дает возможности сохранения ");
            Command command = CommandBuilder.Build(Read());
            if(command instanceof Command_Save){
                Write("Невозможно использовать save на клиенте ");
                continue;
            }

            try {
                if (command instanceof Command_Execute_script) {
                    if(executed_scripts.contains(((Command_Execute_script) command).file_path)){
                        //recursion
                        Write("Red alert:рекурсия обнаружена ");
                        InitReader();
                        continue;
                    }
                    executed_scripts.add(((Command_Execute_script) command).file_path);
                    InputStream is = new FileInputStream(((Command_Execute_script) command).file_path);
                    InitReader(is);
                    continue;
                }
            }catch(FileNotFoundException ex){
                Write("Файл не найден");
            }

            if(command instanceof Command_Update_id){
                SendMessage(new ShowById(((Command_Update_id) command).id),channel,ADDRESS,true);

                //receive the response
                Message receivedMessage = ReceiveMessage(channel, true);

                //output the response
                if(receivedMessage!=null) {
                    String[] output = (String[])(receivedMessage.content);
                    if(output.length==0){
                        Write("Билет с данным id не найден.");
                        continue;
                    }
                    Write(Integer.toString(output.length));
                    for (String str: output) {
                        Write(str);
                    }
                }
                ((Command_Update_id) command).Prepare();
            }


            SendMessage(command, channel, ADDRESS, true);
            if(command instanceof Command_Exit){
                command.Execute(null,null,"",new boolean[]{false},null);
            }


            //receive the response
            Message receivedMessage = ReceiveMessage(channel, true);

            //output the response
            if(receivedMessage!=null) {
                String[] output = (String[])(receivedMessage.content);
                for (String str: output) {
                    Write(str);
                }
            }
            //wait a second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Иннициализация соеденения
     * @param hostname Хост
     * @param port Порт
     */
    public static void InitConnection(String hostname, String port) {
        boolean hasError = true;
        while(hasError) {
            try {
                int Port = Integer.parseInt(port);
                ADDRESS = new InetSocketAddress(hostname, Port);

                channel = DatagramChannel.open();
                channel.configureBlocking(false);
                channel.socket().connect(ADDRESS);

                Thread.sleep(300);

                hasError = false;
            } catch (UnknownHostException ex) {
                Write("Host error: " + ex.getMessage());
                hasError = true;
            } catch (SocketException ex) {
                Write("Socket error: " + ex.getMessage());
                hasError = true;
            } catch (IOException ex) {
                Write("I/O error при инициализации .");
                hasError = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                hasError = true;
            }
        }
    }

    /**
     * Отвечает за чтение
     * @return String
     */
    public static String Read(){
        if(scanner.hasNext()){
            String s = scanner.nextLine();
            return s;
        }else{
            InitReader();
        }
        return scanner.nextLine();
    }

    public static void InitReader(){
        executed_scripts=new ArrayList<String>();
        InitReader(System.in);
    }

    public static void InitReader(InputStream input){
        scanner = new Scanner(input);
    }

    public static void Write(String str){
           System.out.println(str);
    }
}