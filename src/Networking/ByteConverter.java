package Networking;

import java.io.*;
import java.util.Arrays;

/**
 * Класс отвечающий за конвертацию в байты и из байтов
 */
public class ByteConverter {
    public static byte[] convertToBytes(Object object) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos)) {
            objectOutputStream.writeObject(object);
            return baos.toByteArray();
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }

    public static Object convertFromBytes(byte[] buffer) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            ObjectInputStream objectInputStream = new ObjectInputStream(bais);
            Object object = objectInputStream.readObject();
            return object;
        } catch (EOFException ex) {
            System.out.println("EOFException. Размер буффера меньша чем размер переденного сообщения.");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFound error: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("Ошибка object`a. Object пропущен.");
        return null;
    }
}
