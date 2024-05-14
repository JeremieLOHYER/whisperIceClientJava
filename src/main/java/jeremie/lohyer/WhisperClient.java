package jeremie.lohyer;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import jeremie.lohyer.whisperIced.SpeechReceiverPrx;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class WhisperClient {

    private Communicator communicator;

    private String serverAddress;
    SpeechReceiverPrx speechReceiver = null;

    private int nbBlocs = 1;

    public WhisperClient() {
        communicator = com.zeroc.Ice.Util.initialize();
    }

    public void initClient(String serverAddress) {
        initClient(serverAddress, null);
    }

    public void initClient(String serverAddress, ImplTextSender textSender) {
        if (textSender == null) {
            textSender = new ImplTextSender();
        }
        this.serverAddress = serverAddress;
        communicator.getProperties().setProperty("Ice.Default.Package", "com.zeroc.demos.Ice.minimal");

        final String[] ip = {"localhost"};
        new Thread(new Runnable() {
            @Override
            public void run() {
                ip[0] = getIP();
            }
        }).start();

        while (ip[0].contains("localhost")) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("local IP : " + ip[0]);

        try {
            ObjectPrx proxy = communicator.stringToProxy("speechReceiver:default -h " + serverAddress + " -p 6000");
            speechReceiver = SpeechReceiverPrx.checkedCast(proxy);
            System.out.println("connected");
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("TextSender", "default -h " + ip[0] + " -p 12000");
            adapter.add(textSender, Util.stringToIdentity("textSender"));
            adapter.activate();
            System.out.println("server UP");

            speechReceiver.addClient(ip[0], "12000");

            System.out.println("client added");

        } catch (Throwable e) {
            System.out.println("feur : " + e.getMessage());
        }
    }

    public int getNbBlocs() {
        return nbBlocs;
    }

    public void disconnect() {
        communicator.destroy();
    }

    public static String getIP() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (UnknownHostException | SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        WhisperClient monWhisperClient = new WhisperClient();

        ImplTextSender test = new ImplTextSender();

        test.setGetCompletionCallBack((s) -> System.out.println("my callBack said : \n" + s));
        test.setGetCompletionCallBack((val) -> {
            double percentage = (val / (double) monWhisperClient.getNbBlocs()) * 100;
            System.out.println("upload : " + String.format("%.1f", percentage) + "%");
        });

        monWhisperClient.initClient("192.168.1.46", test);

        monWhisperClient.upload("D:/musique/jeremieJAIPETE_meufamanu.wav");

        monWhisperClient.disconnect();
    }

    public void upload(String filePath) {

        if (this.speechReceiver == null) {
            return;
        }

        // Créez un objet File pour le fichier spécifié
        File file = new File(filePath);
        // Obtenez la taille du fichier pour calculer le nombre de blocs nécessaires
        long fileSize = file.length();
        int blockSize = 8192 * 96; // Taille du bloc, vous pouvez ajuster cette valeur selon vos besoins
        nbBlocs = (int) Math.ceil( (double) fileSize / blockSize);

        System.out.println(nbBlocs + " blocs");
        speechReceiver.prepareUpload(nbBlocs);
        // Préparez l'envoi de la chanson au serveur

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[blockSize];
            int bytesRead;
            int blocId = 0;
            // Lisez le fichier par blocs et envoyez-les au serveur
            while ((bytesRead = bis.read(buffer)) != -1) {
                // Envoyez le bloc de données au serveur
                speechReceiver.upload(blocId, buffer);
                blocId++;
            }
        } catch (IOException e) {
            // Gérez les erreurs d'E/S ici
            e.printStackTrace();
        }
    }

}