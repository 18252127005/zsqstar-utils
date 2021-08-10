package request.server.window.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.telnet.TelnetClient;
import org.springframework.stereotype.Service;
import request.server.window.ISystemCmdServer;
import request.server.window.pojo.User;

import java.io.*;

/**
 * @author: Mark.ZSQ
 * @Date: 2021/2/4 3:17 下午
 * @Description:
 */
@Slf4j
@Service
public class WinISystemCmdServerImpl implements ISystemCmdServer {

    private String prompt = ">";    //结束标识字符串,Windows中是>,Linux中是#
    private TelnetClient telnet;
    private InputStream in;        // 输入流,接收返回信息
    private PrintStream out;    // 向服务器写入 命令
    private FTPClient ftp;
    private User userFtp;

    /**
     * 方法说明: 返回结果
     *
     * @param pattern 执行的结果
     * @return: java.lang.String
     * @date: 2019/8/19 14:59
     */
    private String readUntil(String pattern) {
        StringBuffer sb = new StringBuffer();
        try {
            char lastChar = (char) -1;
            boolean flag = pattern != null && pattern.length() > 0;
            if (flag)
                lastChar = pattern.charAt(pattern.length() - 1);
            char ch;
            int code = -1;
            while ((code = in.read()) != -1) {
                ch = (char) code;
                sb.append(ch);

                //匹配到结束标识时返回结果
                if (flag) {
                    if (ch == lastChar && sb.toString().endsWith(pattern)) {//endsWith(pattern)检查字符串是否以pattern 结束
                        return sb.toString();
                    }
                } else {
                    //如果没指定结束标识,匹配到默认结束标识字符时返回结果
                    if (ch == '>')
                        return sb.toString();
                }
                //登录失败时返回结果
                if (sb.toString().contains("Login Failed")) {
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 方法说明: 执行cmd中的命令
     *
     * @param value 命令
     * @return: void
     * @exception:
     * @date: 2019/8/19 15:00
     */
    private void write(String value) {
        try {
            out.println(value);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法说明: 执行命令并返回结果
     *
     * @param command 命令
     * @return: java.lang.String 返回执行的结果集
     * @exception:
     * @date: 2019/8/19 15:01
     */
    private String sendCommand(String command) {
        if (telnet != null) {
            try {
                write(command);
                String str = new String(readUntil(prompt).getBytes("ISO-8859-1"), "GBK");
                return str;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 方法说明: FTP服务连接
     *
     * @return: boolean true 连接成功 false 连接失败
     * @date: 2019/8/19 17:15
     */
    private boolean connectFTP() {
        //创建ftp
        if (ftp == null) {
            ftp = new FTPClient();
        }
        //下面三行代码必须要，而且不能改变编码格式，否则不能正确下载中文文件
        ftp.setControlEncoding("GBK");
        FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_NT);
        config.setServerLanguageCode("zh");
        try {
            ftp.connect(userFtp.getIp(), 21);
            ftp.login(userFtp.getUserName(), userFtp.getUserPwd());
            //看返回的值是不是230，如果是，表示登陆成功
            int replyCode = ftp.getReplyCode();
            //以2开头的返回值就会为真
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftp.disconnect();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 方法说明: 登录windows
     *
     * @param user 用户信息
     * @return: boolean true 成功  false 失败
     * @date: 2019/8/19 15:02
     */
    @Override
    public boolean login(User user) {
        if (user == null)
            return false;
        telnet = new TelnetClient("VT220");
        userFtp = user;
        try {
            telnet.connect(user.getIp(), user.getPort());
            in = telnet.getInputStream();
            out = new PrintStream(telnet.getOutputStream(), false, "GBK");
            readUntil("login:");
            write(user.getUserName());
            readUntil("password:");
            write(user.getUserPwd());
            String rs = readUntil(null);
            if (rs != null && rs.contains("Login Failed")) {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("登录失败！连接异常", e);
        }
        return true;
    }

    /**
     * 方法说明: 关闭
     *
     * @date: 2019/8/19 15:03
     */
    @Override
    public void close() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (telnet != null && !telnet.isConnected())
                telnet.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法说明: 重启
     *
     * @date: 2019/8/19 15:04
     */
    @Override
    public void restart() {
        sendCommand("shutdown -r -t 0");
    }

    /**
     * 方法说明: 关机
     *
     * @date: 2019/8/19 15:05
     */
    @Override
    public void shutdown() {
        sendCommand("shutdown -s -t 0");
    }

    /**
     * 方法说明: 查看分区
     *
     * @date: 2019/8/19 15:05
     */
    @Override
    public String findPartitions() {
        sendCommand("diskpart");
        return sendCommand("list disk");
    }

    /**
     * 方法说明: 查看端口是否被占用
     *
     * @return: boolean true 没被占用 false 被占用
     * @date: 2019/8/19 15:05
     */
    @Override
    public boolean findPort(int port) {
        String s = sendCommand("netstat -aon|findstr \"" + port + "\"");
        if (s.contains(":" + port + " ")) {
            return false;
        }
        return true;
    }

    /**
     * 方法说明: 打开服务
     *
     * @param server 服务名
     * @date: 2019/8/19 15:06
     */
    @Override
    public void openServer(String server) {
        sendCommand("net start " + server);
    }

    /**
     * 方法说明: 关闭服务
     *
     * @param server 服务名
     * @date: 2019/8/19 15:07
     */
    @Override
    public void closeServer(String server) {
        sendCommand("net stop " + server);
    }

    /**
     * 方法说明: 重启服务
     *
     * @param server 服务名
     * @date: 2019/8/19 15:09
     */
    @Override
    public void restartServer(String server) {
        closeServer(server);
        openServer(server);
    }

    /**
     * 方法说明: 上传操作
     *
     * @param remoteFilePath 远程路径名
     * @param localFilePath  本地的路径
     * @return: boolean
     * @exception: 需要root用户才可以进行操作
     * @date: 2019/8/19 15:10
     */
    @Override
    public boolean sftpDownload(String remoteFilePath, String localFilePath) {
        boolean flag = false;
        if (remoteFilePath == null || remoteFilePath.length() == 0 || localFilePath == null || localFilePath.length() == 0) {
            return false;
        }
        if (!connectFTP())
            return false;
        File file = new File(localFilePath);
        //查看远程文件是否存在
        String s = sendCommand("if exist \"" + remoteFilePath + "\" echo");
        if (!s.contains("ECHO 处于打开状态")) {
            sendCommand("md " + remoteFilePath);
        }
        String fileName = file.getName();
        InputStream input = null;
        //上传时本地的文件地址存在执行上传操作
        if (file.exists()) {
            try {
                input = new FileInputStream(new File(localFilePath));
                //每次数据连接之前，ftp client告诉ftp server开通一个端口来传输数据
                ftp.enterLocalPassiveMode();
                //取消服务器获取自身Ip地址和提交的host进行匹配
                ftp.setRemoteVerificationEnabled(false);
                ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftp.storeFile(fileName, input);
                input.close();
                if (!sendCommand("xcopy C:\\ftpFile\\" + fileName + " " + remoteFilePath + " /y").contains("复制了 0 个文件")) {
                    flag = true;
                }
                ftp.dele(fileName);
                ftp.logout();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ftp != null)
                        ftp.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }

    /**
     * 方法说明: 下载操作
     *
     * @param remoteFilePath 远程路径名
     * @param localFilePath  本地的路径
     * @return: boolean
     * @exception: 需要root用户才可以进行操作
     * @date: 2019/8/19 15:10
     */
    @Override
    public boolean uploadFile(String remoteFilePath, String localFilePath) {
        boolean flag = false;
        OutputStream fos = null;
        if (remoteFilePath == null || remoteFilePath.length() == 0 || localFilePath == null || localFilePath.length() == 0) {
            return false;
        }
        if (!connectFTP())
            return false;
        File file = new File(localFilePath);
        if (!file.exists() && !file.isDirectory()) {
            //下载时如果本地文件地址不存在，就创建
            file.mkdir();
        }
        int index = remoteFilePath.lastIndexOf("\\");
        String fileName = "";
        if ((index != -1) && (index != (remoteFilePath.length() - 1))) {
            //获取文件的名称和属性
            fileName = remoteFilePath.substring(index + 1);
        }
        //查看远程文件是否存在
        String s = sendCommand("if exist \"" + remoteFilePath + "\" echo");
        if (s.contains("ECHO 处于打开状态")) {
            //将文件移动到ftp服务的根目录下
            if (!sendCommand("xcopy " + remoteFilePath + " C:\\ftpFile\\" + " /y").contains("复制了 0 个文件")) {
                try {
                    file = new File(localFilePath + "/" + fileName);
                    fos = new FileOutputStream(file);
                    //每次数据连接之前，ftp client告诉ftp server开通一个端口来传输数据
                    ftp.enterLocalPassiveMode();
                    //取消服务器获取自身Ip地址和提交的host进行匹配
                    ftp.setRemoteVerificationEnabled(false);
                    ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
                    flag = ftp.retrieveFile(fileName, fos);
                    fos.close();
                    ftp.dele(fileName);
                    ftp.logout();
                    flag = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (ftp != null)
                            ftp.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return flag;
    }

    /**
     * 方法说明: 配置jdk
     *
     * @param remoteFilePath 远程的jdk路径
     * @param localFilePath  本地的jdk路径
     * @return: boolean 返回安装是否成功
     * @exception: 需要等待软件安装完成才能进行环境变量配置和jdk版本测试
     * @date: 2019/8/20 16:44
     */
    @Override
    public boolean installationJDK(String remoteFilePath, String localFilePath) {
        boolean flag = sftpDownload(remoteFilePath, localFilePath);
        if (flag) {
            int index = localFilePath.lastIndexOf("\\");
            String fileName = "";
            if ((index != -1) && (index != (localFilePath.length() - 1))) {
                //获取文件的名称和属性
                fileName = localFilePath.substring(index + 1);
            }
            //将远程系统中默认安装路径修改为指定路径
            if (sendCommand("reg add HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion /v ProgramFilesDir /t REG_SZ /d " + remoteFilePath + " /f").contains("操作成功完成。")) {
                //使用cmd命令对软件进行静默安装，安装路径为默认路径
                sendCommand("start " + remoteFilePath + "\\" + fileName + " /s");
                try {
                    //毫秒为单位  目的等待软件完全安装结束
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                fileName = sendCommand("for /d %i in (\"" + remoteFilePath + "\\Java\\jdk*\") do echo %i");
                if (fileName != null && fileName.length() > 0) {
                    int top = fileName.indexOf("%i\r\n") + 4;
                    int last = fileName.indexOf("\r\n\r\n");
                    fileName = fileName.substring(top, last);
                }
                sendCommand("setx JAVA_HOME \"" + fileName + "\"");
                sendCommand("setx Path \"%PATH%;%JAVA_HOME%\\bin\";");
                //以下步骤是为让设置的环境变量立即生效
                close();
                login(userFtp);
                flag = sendCommand("java -version").contains("java version ");
            }
        }
        return flag;
    }
}
