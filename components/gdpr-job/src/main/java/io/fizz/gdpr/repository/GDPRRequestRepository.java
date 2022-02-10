package io.fizz.gdpr.repository;

import io.fizz.gdpr.model.GDPRRequest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GDPRRequestRepository {
    private static final String PATH = getResourcesFilePath();

    private static String getResourcesFilePath () {
        final String rootDir = System.getProperty("user.dir");
        return (rootDir + "/src/main/resources/requests.ser").replace("\\", "/");
    }

    public static void save(final List<GDPRRequest> requests) {
        try {
            FileOutputStream writeData = new FileOutputStream(PATH);
            ObjectOutputStream writeStream = new ObjectOutputStream(writeData);

            writeStream.writeObject(requests);
            writeStream.flush();
            writeStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<GDPRRequest> fetch() {
        List<GDPRRequest> requests = new ArrayList<>();
        try {
            File file = new File(PATH);
            if (file.exists()) {
                FileInputStream readData = new FileInputStream(file);
                ObjectInputStream readStream = new ObjectInputStream(readData);

                requests = (List<GDPRRequest>) readStream.readObject();
                readStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return requests;
    }
}
