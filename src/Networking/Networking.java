package Networking;

import Networking.Commands.NetworkingCommand_TestConnection;
import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;

/**
 * главный класс для сетевого взаимодействия
 */
public class Networking {
    //do check connection
    public static void SendMessage(@NotNull Object obj, DatagramChannel channel, SocketAddress ADDRESS, boolean confirmReceived) {

        try {
            //create a byte array where we will store an object
            ByteBuffer msg = ByteBuffer.allocate(10*1024);
            msg.clear();
            msg.put(ByteConverter.convertToBytes(obj));
            msg.flip();

            //send constructed message
            channel.send(msg, ADDRESS);

            if(confirmReceived){
                for (int i=0;i<100;i++){
                    try {
                        //get a response that receiver received the message and only then proceed
                        //Message receiveConfirmationMessage =
                        Object receivedConfirmationObject = ReceiveObject(channel, false);

                        if(receivedConfirmationObject instanceof Message){
                            Message receiveConfirmationMessage = (Message)receivedConfirmationObject;
                            if(receiveConfirmationMessage.content instanceof NetworkingStatus){
                                if(receiveConfirmationMessage.content == NetworkingStatus.RECEIVED){
                                    return;
                                }
                            }
                        }else{
                            if(receivedConfirmationObject instanceof NotConnectedException){
                                return;
                            }
                        }
                        Thread.sleep(100);
                    }catch (InterruptedException ex){
                        System.out.println("Interrupted exception: "+ex.getMessage());
                    }
                }
                System.out.println("Ответ не отправлен.");
            }

        }catch (NullPointerException ex){
            System.out.println("Я всё обработал!");
        }
        catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param channel channel
     * @param sendConfirmation boolean
     * @return Message
     */
    public static Message ReceiveMessage(DatagramChannel channel, boolean sendConfirmation) {
        Object obj = ReceiveObject(channel, sendConfirmation);
        if(obj instanceof Message){
            return (Message) obj;
        }else {
            return null;
        }
    }

    public static Object ReceiveObject(DatagramChannel channel, boolean sendConfirmation){
        //create a byte array where we will store received object
        ByteBuffer buffer = ByteBuffer.allocate(10*1024);
        buffer.clear();

        try {
            //receive an object with its address and write it to the buffer and SocketAddress

            SocketAddress address = channel.receive(buffer);

            //if we received nothing then skip next step and return null
            if (address != null) {
                if (sendConfirmation) {
                    SendMessage(NetworkingStatus.RECEIVED, channel, address, false);
                }
                return new Message(ByteConverter.convertFromBytes(buffer.array()), address);
            }
        }catch(PortUnreachableException ex){
            System.out.println("Не могу подключиться.");
            return new NotConnectedException();
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
}