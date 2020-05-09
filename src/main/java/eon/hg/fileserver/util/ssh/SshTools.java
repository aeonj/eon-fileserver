package eon.hg.fileserver.util.ssh;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SshTools {

    private static final Logger logger = LoggerFactory.getLogger(SshTools.class);
    public static List<Machine> machines;
    public static List<String> exeRemoteConsole(String hostname, int port, String username, String password, String cmd) {
        List<String> result = new ArrayList<String>();
        //指明连接主机的IP地址
        Session ssh = null;
        try {
            //连接到主机
            Connection conn = SshPool.INSTANCE.getConnection(hostname,port,username,password);
            if (conn==null || false == conn.isAuthenticationComplete()) {
                logger.error("用户名称或者是密码不正确");
            } else {
                logger.info("已经连接OK");
                ssh = conn.openSession();
                //使用多个命令用分号隔开
//              ssh.execCommand("pwd;cd /tmp;mkdir shb;ls;ps -ef|grep weblogic");
                ssh.execCommand(cmd);
                //只允许使用一行命令，即ssh对象只能使用一次execCommand这个方法，多次使用则会出现异常
//              ssh.execCommand("mkdir hb");
                //将屏幕上的文字全部打印出来
                InputStream is = new StreamGobbler(ssh.getStdout());
                BufferedReader brs = new BufferedReader(new InputStreamReader(is));
                for (String line = brs.readLine(); line != null; line = brs.readLine()) {
                    result.add(line);
                }
            }
            //连接的Session和Connection对象都需要关闭
            if (ssh != null) {
                ssh.close();
            }
        } catch (IOException e) {
            logger.error("", e);
        }
        return result;
    }
    public static String getRootPath() {
        String classPath = SshTools.class.getResource("/").getPath();
        String rootPath  = "";
        //windows下
        if("\\".equals(File.separator)){
            rootPath  = classPath.substring(1,classPath.indexOf("/WEB-INF/classes"));
            rootPath = rootPath.replace("/", "\\");
        }
        //linux下
        if("/".equals(File.separator)){
            rootPath  = classPath.substring(0,classPath.indexOf("/WEB-INF/classes"));
            rootPath = rootPath.replace("\\", "/");
        }
        return rootPath;
    }
    public static String getClassPath(){
        String classPath = SshTools.class.getResource("/").getPath();

        //windows下
        if("\\".equals(File.separator)){

            classPath = classPath.replace("/", "\\");
        }
        //linux下
        if("/".equals(File.separator)){

            classPath = classPath.replace("\\", "/");
        }
        return classPath;
    }

    static {
        SAXReader saxReader = new SAXReader();
        try {
            System.out.println(SshTools.getClassPath());
            Document document = saxReader.read(SshTools.getClassPath() + "ssh-config.xml");
            Element root = document.getRootElement();
            machines = new ArrayList<Machine>();
            @SuppressWarnings("unchecked")
            List<Element> elements = root.elements("server");
            for (Element element : elements) {
                Machine machine = new Machine();
                String ip = element.element("ip").getText();
                String username = element.element("username").getText();
                if(element.element("port")!=null) {
                    int port = Integer.parseInt(element.element("port").getText());
                    machine.setPort(port);
                } else {
                    machine.setPort(22);
                }
                if(element.element("password")!=null){

                    String password = element.element("password").getText();
                    machine.setPassword(password);
                    machine.setConfigType(true);      //用户名密码登录
                }

                if(element.element("ssh")!=null){

                    String ssh = element.element("ssh").getText();
                    machine.setSsh(ssh);
                    machine.setConfigType(false);    //密钥登录

                }
                String logpath = element.element("logpath").getText();
                machine.setIp(ip);
                machine.setUsername(username);

                machine.setLogpath(logpath);

                machines.add(machine);
            }
        } catch (DocumentException e) {
            logger.error("read config.xml error!!!!", e);
        }
    }
}
