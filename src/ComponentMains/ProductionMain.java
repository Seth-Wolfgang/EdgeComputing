package ComponentMains;

import Client.Client;
import Client.ClientCompute;
import Cloud.Cloud;
import Network.EdgeServer;
import Network.Server;

import java.io.IOException;

public class ProductionMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        String type = args[0];
        String IP;

        //prints out instructions for client if typed wrong
        if (args.length != 2 && type.equals("-c")) {
            System.out.println("""
                    Please use format for arguments:
                    -c [Edge IPV4] [FTP Port]
                    """);
        }
        //instructions for edge server
        else if (args.length != 3 && type.equals("-e")) {
            System.out.println("""
                    Please use format for arguments:
                    -e [Server IPV4] [Device IPV4]
                    """);
        }
        //Prints out help when run
        else if (type.equals("help") || type.equals("-h")) {
            System.out.println("""
                    Type:
                    \t-cc\t Client computer
                    \t-c\t Client device
                    \t-e\t Edge server device
                    \t-s\t server device
                    Please use format for client arguments:
                    \t-c [IPV4]
                    Edge server devices needs different arguments:
                    \t-e [Server IPV4] [Device IPV4]
                    Server needs no extra arguments:
                    \t-s""");
        }

        switch (args[0]) {
            case "-cl":
                String deviceIP = args[1];
                Cloud cloud = new Cloud(deviceIP);
            case "-c":
                IP = args[1];
                Client client = new Client(IP);
                break;
            case "-e":
                deviceIP = args[2];
                try {
                    IP = args[1];
                    EdgeServer edgeServer = new EdgeServer(deviceIP, IP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case "-cc":
                IP = args[1];
                ClientCompute cc = new ClientCompute(IP);
                break;
            case "-s":
                Server server = new Server(5000);
                break;
            default:
                System.out.println("""
                    Type:
                    \t-cc\t Client computer
                    \t-c\t Client device
                    \t-e\t Edge server device
                    \t-s\t server device
                    Please use format for client arguments:
                    \t-c [IPV4]
                    Edge server devices needs different arguments:
                    \t-e [Server IPV4] [Device IPV4]
                    Server needs no extra arguments:
                    \t-s""");
                break;
            case "install":

        }
    }
}
