package request.server.window;

import request.server.window.pojo.User;

public interface ISystemCmdServer {
    boolean login(User user);
    void close();
    void restart();
    void shutdown();
    String findPartitions();
    boolean findPort(int port);
    void openServer(String server);
    void closeServer(String server);
    void restartServer(String server);
    boolean sftpDownload(String remoteFilePath, String localFilePath);
    boolean uploadFile(String remoteFilePath, String localFilePath);
    boolean installationJDK(String remoteFilePath, String localFilePath);
}
