package org.vavilon.servers;

import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

public class FtpServer {

    private final int ftpPort;
    private final int passiveStartPort;
    private final int passiveEndPort;
    private org.apache.ftpserver.FtpServer server;

    public FtpServer(int ftpPort, int passiveStartPort, int passiveEndPort) {
        this.ftpPort = ftpPort;
        this.passiveStartPort = passiveStartPort;
        this.passiveEndPort = passiveEndPort;
    }

    public void start() {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory();
        DataConnectionConfigurationFactory dataConFactory = new DataConnectionConfigurationFactory();
        dataConFactory.setPassivePorts(passiveStartPort + "-" + passiveEndPort);
        factory.setDataConnectionConfiguration(dataConFactory.createDataConnectionConfiguration());
        factory.setPort(ftpPort); // Установите порт для FTP сервера
        serverFactory.addListener("default", factory.createListener());

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        File userPropertiesFile = new File("users.properties");

        // Создание директории и файла, если они не существуют
        if (!userPropertiesFile.exists()) {
            try {
                Files.createFile(userPropertiesFile.toPath());
            } catch (IOException e) {
                System.err.println("Ошибка при создании директории или файла: " + e.getMessage());
                return;
            }
        }
        userManagerFactory.setFile(userPropertiesFile);
        userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());

        UserManager um = userManagerFactory.createUserManager();

        BaseUser user = new BaseUser();
        user.setName("admin");
        user.setPassword("password");
        String homeDirPath = "c:\\wamp\\www\\node_modules"; // Убедитесь, что путь указан правильно и существует

        // Проверка возможности чтения и записи
        File homeDir = new File(homeDirPath);
        if (!homeDir.exists() || !homeDir.isDirectory()) {
            System.err.println("Домашняя директория не существует или не является директорией: " + homeDirPath);
            return;
        }

        if (!homeDir.canRead() || !homeDir.canWrite()) {
            System.err.println("Недостаточно прав для чтения или записи в домашнюю директорию: " + homeDirPath);
            return;
        }

        // Дополнительная проверка записи и чтения файла
        try {
            File testFile = new File(homeDir, "testfile.txt");
            Files.write(Paths.get(testFile.getPath()), "test".getBytes());
            Files.readAllBytes(Paths.get(testFile.getPath()));
            testFile.delete();
        } catch (IOException e) {
            System.err.println("Ошибка при проверке записи и чтения файла в домашней директории: " + e.getMessage());
            return;
        }

        user.setHomeDirectory(homeDirPath);
        user.setAuthorities(Collections.singletonList(new WritePermission())); // Grant write permission

        try {
            um.save(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        serverFactory.setUserManager(um);

        server = serverFactory.createServer();
        try {
            server.start();
            System.out.println("FTP started on port: " + factory.getPort());
        } catch (Exception e) {
            System.out.println("FTP failed to start: " + e.getMessage());
        }
    }

    public void stopServer() {
        if (server != null && !server.isStopped()) {
            server.stop();
        }
    }
}