package Network;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.util.ArrayList;
import java.util.List;

public class easyFTPServer {

    public easyFTPServer (String address, int port) {

        //instantiates the factories for the FTP server
        FtpServer ftpServer = null;
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory lFactory = new ListenerFactory();
        FileSystemFactory fsf = new NativeFileSystemFactory();
        ConnectionConfigFactory cFactory = new ConnectionConfigFactory();
        cFactory.setAnonymousLoginEnabled(true);

        //sets port and listener
        lFactory.setPort(port);
        lFactory.setServerAddress(address);
        serverFactory.addListener("default", lFactory.createListener());
        serverFactory.setConnectionConfig(cFactory.createConnectionConfig());

        try {

            //basic user authorities
            List<Authority> authorities = new ArrayList<Authority>();
            authorities.add(new WritePermission());
            BaseUser user = new BaseUser();
            user.setAuthorities(authorities);

            //basic user settings
            user.setName("user");
            user.setPassword("");
            user.setHomeDirectory("filesToProcess");

            //configures and creates FTP server
            serverFactory.setFileSystem(fsf);
            serverFactory.getUserManager().save(user);
            ftpServer = serverFactory.createServer();
            ftpServer.start();

        } catch (
                FtpException e) {
            e.printStackTrace();
        }
    }
}
