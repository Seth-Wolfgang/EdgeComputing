/**
 * Author: Seth Wolfgang
 * Date: 4/19/2022
 * This program serves as the client/worker of the network.
 * It receives an image from the `middle` layer, reads the text on the image,
 * and sends the text back to the middle layer.
 */

package Client;

import OCR.OCRTest;
import OCR.Timer;
import SmithWaterman.SWinitialiser;
import net.sourceforge.tess4j.TesseractException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class Client {

    //Initial vars
    ArrayList<Long> runTimes = new ArrayList<>();
    int test = 1; //test refers to the benchmark performed todo create a better way of handling this
    int counter = 0;
    final int iterations = 100; //controls how many times this class performs a bench


    // constructor to put ip address and port
    public Client(String address, int port, int ftpPort) throws IOException, TesseractException {

        //Setup before connection occurs
        Socket socket = new Socket();
        InetSocketAddress sa = new InetSocketAddress(address, port);
        easyFTPClient ftpClient = new easyFTPClient(address, ftpPort);

        //initial connection to server
        socket.connect(sa, 5000);
        System.out.println("Connected");

        //connects to EdgeServer
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        switch (test) {
            case 1: //OCR Test
                try {

                    //grabbing image file
                    File image = new File("woahman.png");
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(image));

                    //todo temporary -> require args in future?
                    OCRBench(socket, ftpClient, "woahman.png", image);

                    //if test done
                    outputStream.close();
                    closeConnection(socket, out);

                    //cleanup
                    File file = new File(image.getAbsolutePath());
                    file.delete();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break; //end of OCR test

            case 2: //Smith-Waterman Test
                iteratedSWBench(socket, ftpClient);
                closeConnection(socket, out);
                break; //End of Smith-Waterman test


        }
    }// end of client

    /**
     * Method for easily and cleanly closing connection
     *
     * @param out DataOutputStream
     * @param socket Socket
     */

    public void closeConnection(Socket socket, DataOutputStream out) throws IOException {
        try {
            out.writeUTF("over"); //tells the server when to close connection for safe stop
            out.close();
            socket.close();

        } catch (IOException e) {
            throw new IOException();
        }
    }

    /**
     * Method for performing a benchmark using Optical Character Recognition. This will record
     * the time of each OCR performed and give the total time for each iteration to be performed.
     *
     *
     * @param socket Socket used to connect to Server.java
     * @param ftpClient easyFTP class client
     * @param imageName String
     * @param image File
     */

    public void OCRBench(Socket socket, easyFTPClient ftpClient, String imageName, File image) throws IOException {
        ArrayList<String> manyOutput = new ArrayList<>(); //output of the image. Arraylist for many iterations of this test
        OCRTest ocrTest = new OCRTest("tessdata");
        String imageText = null;

        try {
            //grabs image from EdgeServer and sets it as the image for OCR to run with
            image = ftpClient.getFile(imageName);
            ocrTest.setImage(image);
        } catch (IOException e) {
            System.out.println("Grabbing image Failed!");
            e.printStackTrace();
        }

        //runs the test
        runTimes = ocrTest.performCompactBenchmark(iterations);
        manyOutput = ocrTest.getManyOutput(); // returns the output

        //individualTransmission(socket, manyOutput);
        compactTransmission(socket, manyOutput);
    }

    /**
     * Runs method SWBench as many times as specified by `iterations`
     *
     * @param socket
     * @param ftpClient
     * @throws IOException
     */

    public void iteratedSWBench(Socket socket, easyFTPClient ftpClient) throws IOException {
        for (int i = 0; i < iterations; i++){
            SWBench(socket, ftpClient);
            counter++;
        }
    }


    /**
     * Performs the Smith-Waterman algorithm after retrieving files from ftp server.
     *
     * @param socket socket used for data transmission to server
     * @param ftpClient easyFTP client
     * @throws IOException
     */


    public void SWBench(Socket socket, easyFTPClient ftpClient) throws IOException {
        //NOTE: run time is affected most by query
        String[] inputFiles = {"smallQuery.txt","database.txt","alphabet.txt","scoringmatrix.txt"};
        Timer timer = new Timer();
        File file;
        ArrayList<String> SWOutput = new ArrayList<>();
        int m = 1; //todo replace with args?
        int k = 1;

        //File grabbing
        timer.start();
        for(String path : inputFiles){
            ftpClient.getFile(path);
            file = new File(path);
            file.deleteOnExit();
        }
        timer.stopAndPrint("SW File Requests");

        try {
            //Running Smith-Waterman
            timer.start();
            SWOutput.add(new SWinitialiser().run(inputFiles[0], inputFiles[1], inputFiles[2], inputFiles[3], m, k));
            timer.stopAndPrint("SW run");

            //Sending results of Smith-Waterman
            timer.start();
            compactTransmission(socket, SWOutput);
            timer.stopAndPrint("SW Transmission");

        } catch (Exception e) {
            e.printStackTrace();
        }

        //cleanup
        for(String path : inputFiles){
            file = new File(path);
            file.delete();
        }
    }

    /**
     * Individual transmission sends each value of the ArrayList input separately
     * from one another.
     *
     * @param socket
     * @param manyOutput
     * @throws IOException
     */

    public void individualTransmission(Socket socket, ArrayList<String> manyOutput) throws IOException {
        DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream());
        Timer timer = new Timer();

        timer.start();
        for (String out : manyOutput){
            dataOutput.writeUTF(out);
            timer.newLap();
        }
        timer.stopTimer();
        timer.printResultsToFile("Individual Transmission Start");
    }

    /**
     * Compact transmission sends all data at once by inputting an ArrayList and
     * turning the ArrayList into a semicolon seperated string.
     *
     * @param socket socket to connect to Server.java
     * @param manyOutput ArrayList<String>
     * @throws IOException
     */

    public void compactTransmission(Socket socket, ArrayList<String> manyOutput) throws IOException {
        DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream());
        String outputString = manyOutput.get(0); //Semicolon seperated values of manyOutput
        Timer timer = new Timer();

        //allows for proper formatting
        manyOutput.remove(0);
        //Converts ArrayList to semicolon seperated values
        for(String output : manyOutput) {
            outputString = outputString + ";" + output;
        }

        //removes unnecessary new lines
        outputString = outputString.replace("\n", "").replace("\r", "");

        //times the transmission until it is done
        timer.start();
        dataOutput.writeUTF(outputString);
        timer.stopAndPrint("Compact Transmission Start");
    }
}