import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.net.*;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Patrick
 */
public class Project2 {

    boolean hasOutstandingGrantMessage;
    long outstandingGrantMessageTimestamp;
    int outstandingGrantMessageId;
    int id;
    int clock = 0;
    InetAddress add = null;
    int port1 = 0;
    LinkedList<String> hostname;
    LinkedList<String> port;
    LinkedList<Integer> quorum;
    LinkedList<Boolean> quorumGranted;
    Socket clientSocket;

    public Project2(int id) {
        this.id = id;
        hostname = new LinkedList<>();
        port = new LinkedList<>();
        quorum = new LinkedList<>();
        quorumGranted = new LinkedList<>();
    }

    void startThread() {
        new ServerThread(this).start();
        //new HelperThread(this).start();
    }

    boolean quorumAllGranted() {
        Iterator<Boolean> iter = quorumGranted.iterator();
        while (iter.hasNext()) 
            if (!iter.next()) return false;
        return true;
    }


    class ServerThread extends Thread {

        Project2 server;
        int port;
        LinkedList<Request> request;
//        LinkedList<Request> failedRequest;

        public ServerThread(Project2 main) {
            this.server = main;
            request = new LinkedList<>();
        }

        int increaseClock(int clk1, int clk2)
        {
            if(clk1>clk2)
            {
                clock = clk1+1;
            }
            else
            {
                clock = clk2+1;
            }
            return clock;
        }

        void insertRequestByTimestamp(Request newRequest) {
            int indexToInsertInto = 0;
            for (int i = 0; i < request.size(); i++) {
                Request r = request.get(i);
                if (r.clk > newRequest.clk) {
                    indexToInsertInto = i;
                    break;
                }
            }
            request.add(indexToInsertInto, newRequest);
        }

        void grant(int clk, int messageId) {
            server.hasOutstandingGrantMessage = true;
            server.outstandingGrantMessageTimestamp = clk;
            server.outstandingGrantMessageId = messageId;
            String hostName = server.hostname.get(messageId);
            String hostPort = server.port.get(messageId);
            try {
//                Socket socket = new Socket(hostName, Integer.parseInt(hostPort));
//                OutputStream outToServer = socket.getOutputStream();
//                DataOutputStream out = new DataOutputStream(outToServer);
                String msg1 = Message.GRANT.id + ","+clk+"," + server.id;
                System.out.println(msg1);
//                out.writeUTF(msg1);
                BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
             DatagramSocket clientSocket = new DatagramSocket();
             InetAddress IPAddress = InetAddress.getByName(hostName);
             byte[] sendData = new byte[1024];
             byte[] receiveData = new byte[1024];
             String sentence = msg1;
             sendData = sentence.getBytes();
             System.out.println("Sent Grant response:"+sentence);
             DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(hostPort));
             clientSocket.send(sendPacket);
             clock++;
                //writer.write(Message.GRANT.id + "," + server.id);
            } catch (IOException ex) {
                Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        Boolean checkInquire(int messageId)
        {
            for (int i = 0; i < this.server.quorum.size(); i++) {
                if (server.quorum.get(i)-1 == messageId){

                }
                else
                {
                    if(!this.server.quorumGranted.get(i))
                        return false;
                }
            }
            return true;
            
        }

        void fail(int clk, int messageId) {
            String hostName = server.hostname.get(messageId);
            String hostPort = server.port.get(messageId);
            try {
//                Socket socket = new Socket(hostName, Integer.parseInt(hostPort));
//                OutputStream outToServer = socket.getOutputStream();
//                DataOutputStream out = new DataOutputStream(outToServer);
                String msg = Message.FAILED.id + "," + server.id;
//                out.writeUTF(msg);
                BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
             DatagramSocket clientSocket = new DatagramSocket();
             InetAddress IPAddress = InetAddress.getByName(hostName);
             byte[] sendData = new byte[1024];
             byte[] receiveData = new byte[1024];
             String sentence = msg;
             sendData = sentence.getBytes();
             System.out.println("Sent Failed response:"+sentence);
             DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(hostPort));
             //System.out.println("Here 123");
             clientSocket.send(sendPacket);
             clock++;
             //clk = clock;
                //writer.write(Message.FAILED.id + "," + server.id);
                insertRequestByTimestamp(new Request(clk, messageId));
            } catch (IOException ex) {
                Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        void inquire(int clk, int messageId) {
            String hostName = server.hostname.get(server.outstandingGrantMessageId);
            String hostPort = server.port.get(server.outstandingGrantMessageId);
            try {
//                Socket socket = new Socket(hostName, Integer.parseInt(hostPort));
//                OutputStream outToServer = socket.getOutputStream();
//                DataOutputStream out = new DataOutputStream(outToServer);
                String msg = Message.INQUIRE.id + "," + server.id;
//                out.writeUTF(msg);
                //writer.write(Message.INQUIRE.id + "," + server.id);
                BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
             DatagramSocket clientSocket = new DatagramSocket();
             InetAddress IPAddress = InetAddress.getByName(hostName);
             byte[] sendData = new byte[1024];
             byte[] receiveData = new byte[1024];
             String sentence = msg;
             sendData = sentence.getBytes();
             DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(hostPort));
             clientSocket.send(sendPacket);
             System.out.println("Sent inquire "+sentence);
             clock++;
//                insertRequestByTimestamp(new Request(messageTimestamp, messageId));
            } catch (IOException ex) {
                Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
            }
            insertRequestByTimestamp(new Request(clk, messageId));
        }

        void yield(int clk, int messageId) {
            Request r = request.removeFirst();
            String hostName = server.hostname.get(r.messageId);
            String hostPort = server.port.get(r.messageId);
            try {
//                Socket socket = new Socket(hostName, Integer.parseInt(hostPort));
//                OutputStream outToServer = socket.getOutputStream();
//                DataOutputStream out = new DataOutputStream(outToServer);
                String msg = Message.GRANT.id + "," +clock+","+ server.id;
//                out.writeUTF(msg);
                BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
             DatagramSocket clientSocket = new DatagramSocket();
             InetAddress IPAddress = InetAddress.getByName(hostName);
             byte[] sendData = new byte[1024];
             byte[] receiveData = new byte[1024];
             String sentence = msg;
             sendData = sentence.getBytes();
             System.out.println("Sent Yeild response:"+sentence);
             DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(hostPort));
             clientSocket.send(sendPacket);

             clock++;
            } catch (IOException ex) {
                Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
            }
            insertRequestByTimestamp(new Request(clk, messageId));
        }

        void release(int clk, int messageId) {
            for (int i = 0; i < request.size(); i++) {
                Request r = request.get(i);
                if (r.messageId == messageId) {
                    request.remove(i);
                    break;
                }
            }
            server.hasOutstandingGrantMessage = false;
            if (request.size() > 0) {
                Request r = request.removeFirst();
                this.grant(r.clk, r.messageId);
                /*
                 String hostName = server.hostname.get(r.messageId);
                 String hostPort = server.port.get(r.messageId);
                 try {
                 Socket socket = new Socket(hostName, Integer.parseInt(hostPort));
                 PrintWriter writer = new PrintWriter(socket.getOutputStream());
                 writer.write(Message.GRANT.id + "," + server.id);
                 } catch (IOException ex) {
                 Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
                 }*/
            }
            System.out.println("Resource Released");
        }
        
        void receiveGrant(int clk, int messageId, InetAddress add, int port1, DatagramSocket serverSocket) {
            
            int realIndex = -1;
            for (int i = 0; i < this.server.quorum.size(); i++) {
                System.out.println(server.quorum.get(i)+","+messageId);
                if (server.quorum.get(i)-1 == messageId) realIndex = i;
            }
            this.server.quorumGranted.set(realIndex, true);
            boolean b = server.quorumAllGranted();
            if (b) {
//                server.clientSocket.getOutputStream();
               byte[] sendData = "capitalizedSentence".getBytes();
               //System.out.println(add+" "+port1);
               DatagramPacket sendPacket =
               new DatagramPacket(sendData, sendData.length, add, port1);
                try {
                    serverSocket.send(sendPacket);
                    clock++;
                } catch (IOException ex) {
                    Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        void processInquire(int messageId)
        {
            //Request r = request.removeFirst();
            String hostName = server.hostname.get(messageId);
            String hostPort = server.port.get(messageId);
            System.out.println(hostPort);
            int realIndex = -1;
            for (int i = 0; i < this.server.quorum.size(); i++) {
                System.out.println(server.quorum.get(i)+","+messageId);
                if (server.quorum.get(i)-1 == messageId) realIndex = i;
            }
            this.server.quorumGranted.set(realIndex, false);
            try {
//                Socket socket = new Socket(hostName, Integer.parseInt(hostPort));
//                OutputStream outToServer = socket.getOutputStream();
//                DataOutputStream out = new DataOutputStream(outToServer);
                String msg = Message.YIELD.id + "," +clock+","+ server.id;
//                out.writeUTF(msg);
                BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
             DatagramSocket clientSocket = new DatagramSocket();
             InetAddress IPAddress = InetAddress.getByName(hostName);
             byte[] sendData = new byte[1024];
             byte[] receiveData = new byte[1024];
             String sentence = msg;
             sendData = sentence.getBytes();
             System.out.println("Sent Yeild response:"+sentence);
             DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(hostPort));
             clientSocket.send(sendPacket);

             clock++;
            } catch (IOException ex) {
                Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        void readFromSocket(DatagramPacket receivePacket, String inpmsg, DatagramSocket serverSocket) {
            String message = inpmsg;
            String[] field = message.split(",");
            int clk = 0;
            /*if (field.length == 3) {
            clk = Integer.parseInt(field[1]);
            if(clk>100 && clk < 130)
            {  

                message = "start";
                clk = 30;
                clock = 20;
                
            }}*/
            if (message.equals("start")) {
                add = receivePacket.getAddress();
               port1 = receivePacket.getPort();
               //System.out.print("quorum for server "+server.id+": ");
               for (int i = 0; i < quorum.size(); i++) {
                   System.out.print(quorum.get(i)+" ");
                    try {
                        int index = quorum.get(i);
                        //System.out.println("index"+index);
                        String host2 = this.server.hostname.get(index-1);
                        System.out.println(host2);
                        int port2 = Integer.parseInt(this.server.port.get(index-1));
                        //BufferedReader inFromUser =
                                //new BufferedReader(new InputStreamReader(System.in));
                        DatagramSocket clientSocket = new DatagramSocket();
                        //System.out.println("Test1"+host2+" "+port2);
                        InetAddress IPAddress = InetAddress.getByName(host2);
                        //System.out.println("Test1"+host2);
                        byte[] sendData = new byte[1024];
                        //System.out.println("Test1");
                        byte[] receiveData = new byte[1024];
                        String sentence = "1,"+clock+","+this.server.id;
                        //clock++;
                        sendData = sentence.getBytes();
                        System.out.println("Sent Requests"+sentence);
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port2);
                        clientSocket.send(sendPacket);
                        //clock++;
                        //System.out.println("Finish");
                    }
//                new HelperThread(server, clientSocket).start();
                    catch (SocketException ex) {
                        Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
                    }
               } System.out.println("");
               clock++;
            }
            if(message.equals("stop"))
            {
                add = receivePacket.getAddress();
               port1 = receivePacket.getPort();
                //System.out.print("quorum for server "+server.id+": ");
               for (int i = 0; i < quorum.size(); i++) {
                   System.out.print(quorum.get(i)+" ");
                    try {
                        int index = quorum.get(i);
                        //System.out.println("index"+index);
                        String host2 = this.server.hostname.get(index-1);
                        System.out.println(host2);
                        int port2 = Integer.parseInt(this.server.port.get(index-1));
                        //BufferedReader inFromUser =
                                //new BufferedReader(new InputStreamReader(System.in));
                        DatagramSocket clientSocket = new DatagramSocket();
                        //System.out.println("Test1"+host2+" "+port2);
                        InetAddress IPAddress = InetAddress.getByName(host2);
                        //System.out.println("Test1"+host2);
                        byte[] sendData = new byte[1024];
                        //System.out.println("Test1");
                        byte[] receiveData = new byte[1024];
                        String sentence = "3,"+clock+","+this.server.id;
                        clock++;
                        sendData = sentence.getBytes();
                        //System.out.println("Test1"+sentence);
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port2);
                        clientSocket.send(sendPacket);
                        //System.out.println("Finish11111");
                        sendData = "CapitalizedSentence".getBytes();
                        for(int i1=0;i1<server.quorumGranted.size();i1++)
                        {
                            server.quorumGranted.set(i1,false);
                        }
                        //System.out.println(add+" "+port1);
                        sendPacket =
                        new DatagramPacket(sendData, sendData.length, add, port1);
                        try {
                            serverSocket.send(sendPacket);
                            clock++;
                        } catch (IOException ex) {
                            Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
                        }
            }
            catch (SocketException ex) {
                        Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
                    }
        }}
            //String[] field = message.split(",");

        if (field.length == 2) {
                int messageType = Integer.parseInt(field[0]);
                int messageId = Integer.parseInt(field[1]);
                if(messageType == Message.FAILED.id)
                {
                    System.out.println("Recieved Failed Message From: "+messageId);
                    int realIndex = -1;
                    for (int i = 0; i < this.server.quorum.size(); i++) {
                        System.out.println(server.quorum.get(i)+","+messageId);
                        if (server.quorum.get(i)-1 == messageId) realIndex = i;
                    }
                    this.server.quorumGranted.set(realIndex, false);

                }
                else if(messageType == Message.INQUIRE.id)
                {

                    System.out.println("INQUIRE::"+messageId);
                    if(!checkInquire(messageId))
                    {
                        int in = -1;
                        for (int i = 0; i < this.server.quorum.size(); i++) {
                            System.out.println(server.quorum.get(i));
                            if (server.quorum.get(i)-1 == messageId){
                                in = i;
                                }
                            
                        }
                        this.server.quorumGranted.set(in, false);
                        System.out.println("------------------Hello-------------");
                        processInquire(messageId);
                        System.out.println("------------------Hello-------------");
                    }
                }
                /*else if(messageType == Message.GRANT.id)
                {
                    receiveGrant(clk, messageId, add, port1, serverSocket);   
                }*/
            }



            if (field.length == 3) {
                int messageType = Integer.parseInt(field[0]);
                clk = Integer.parseInt(field[1]);
                int messageId = Integer.parseInt(field[2]);
                if (messageType == Message.REQUEST.id) {
                    if (!server.hasOutstandingGrantMessage) {
                        //System.out.println("in grant");
                        grant(clk, messageId);
                    } else {
                        // if request message has lower priority
                        if (clk <= server.outstandingGrantMessageTimestamp) {
                            inquire(clk, messageId);
                        } else {
                            fail(clk, messageId);
                        }
                    }
                } else if (messageType == Message.YIELD.id) {

                    yield(clk, messageId);
                } else if (messageType == Message.RELEASE.id) {
                    release(clk, messageId);
                } else if (messageType == Message.GRANT.id) {
                    receiveGrant(clk, messageId, add, port1, serverSocket);
                }
                clk = increaseClock(clk,clock);
                //System.out.println("3");
            }

            //} //catch (IOException ex) {
            //  Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
            //}
        }

        @Override
        public void run() {
            // first start client thread

            // then do server
            try {
//                String port2 = server.port.get(id);
//                //port2 = "3384";
//                System.out.println(port2);
//                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port2));
//                System.out.print("Server " + id + " started");
//                String hostName = server.hostname.get(id);
//
//                System.out.println(" - " + hostName + ":" + port2);
                
                String port2 = server.port.get(id);
                DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(port2));
                byte[] receiveData = new byte[1024];
                byte[] sendData = new byte[1024];
                while (true) {
                    //System.out.println("0");
//                    Socket socket = serverSocket.accept();
//                    DataInputStream in = new DataInputStream(socket.getInputStream());
//                    //System.out.print(in.available());
//                    String inpmsg = in.readUTF();
//                    System.out.println("Input message: " + inpmsg);
                    System.out.println("Listening....");
                    if (!server.hasOutstandingGrantMessage) { 
                        System.out.println("Processing Here...."); 
                    }
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    String sentence = "";
                    sentence = "";
                    sentence = new String( receivePacket.getData(),0, receivePacket.getLength());
                    System.out.println("RECEIVED: " + sentence);
                    readFromSocket(receivePacket, sentence, serverSocket);
                    //System.out.println("4");
                }
            } catch (IOException ex) {
                Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    class Request {

        final int clk;
        final int messageId;

        public Request(int l, int i) {
            clk = l;
            messageId = i;
        }
    }

    enum Message {

        REQUEST(1), REPLY(2), RELEASE(3), GRANT(4), INQUIRE(5), FAILED(6), YIELD(7),
        MONITOR(8), END(9);

        public final int id;

        Message(int i) {
            this.id = i;
        }

        int getMessageId() {
            return id;
        }
    }

    public static void main(String[] args) {
//        File f = new File("config.txt");
        File f = new File("testconfig.txt");
        System.out.println(f.exists());
        //int id = 0;
        int id = Integer.parseInt(args[0]);
        System.out.println(id);
        //int id = 0; <-- should be 1 for first server
        Project2 main = new Project2(id);
        try {
//            Project2 main = new Project2(id);
            Scanner reader = new Scanner(f);
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                System.out.println(line + "!");
                if (!line.startsWith("#")) {
                    int numberOfNodes = Integer.parseInt(line);
                    break;
                }
            }
            int cntr = 0;
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                System.out.println(line + "?");
                if (!line.startsWith(" ") && !line.startsWith("#")
                        && !line.equals("")) {
                    String[] split = line.split("\t+| +");
                    String host = split[0];
                    String port = split[1];
                    if (id == cntr) {
                        for (int i = 2; i < split.length; i++) {
                            main.quorum.add(Integer.parseInt(split[i]));
                            main.quorumGranted.add(false);
                        }
                        
                    } 
                    main.hostname.add(host);
                    main.port.add(port);
                    cntr++;
                }
            }
            main.startThread();

//            main.getNewClientThread(time, id, mylist).start();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
        }
//        try {
//            Thread.sleep(500);
//            String host1 = main.hostname.get(id - 1);
//            String port1 = main.port.get(id - 1);
//            Socket socket = new Socket(host1, Integer.parseInt(port1));
//            
//        } catch (InterruptedException | IOException ex) {
//            Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
//        }

    }
}

