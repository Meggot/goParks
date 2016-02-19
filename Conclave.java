public void startServer()
    {
        open = true;
        try {
            ServerSocket servSock = new ServerSocket(port, 50, ip);
            int activeThreads = 0;
            while (servSock.isBound())
            {
                System.out.println("Server is online on port: " + servSock.getLocalPort() + ". IP: " + servSock.getInetAddress());
                Socket newConnectionSocket = servSock.accept();
                if (!newConnectionSocket.isClosed()) 
                {
                    servSock.setReuseAddress(true);
                    System.out.println("New Connection! Connection IP: " + newConnectionSocket.getInetAddress());
                    ConnectionHandler newHandler = new ConnectionHandler(newConnectionSocket, activeThreads);
                    newHandler.start();
                    connections.add(newHandler);
                    activeThreads++;
                }
                newConnectionSocket = null;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    //IN TURN OPENS A CONNECTION HANDLER AND STARTS A NEW THREAD
    
    public class ConnectionHandler extends Thread{
    
    private Socket sock;
    private Conclave server;
    private int serverid;
    
    public ConnectionHandler(Socket isock, int id)
    {
        this.sock = isock;
        this.server = Conclave.getInstance();
        this.serverid = id;
    }
    
    @Override
    public void start()
    {
        while (!sock.isClosed())
        {
        int responseCode = 502;
        String returnMsg = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String nextLine = "";
            ArrayList<String> requestLines = new ArrayList<>();
            while (br.ready())
            {
                nextLine = br.readLine();
                System.out.println(nextLine);
                requestLines.add(nextLine);
            }
            //FIRST LINE OF REQUEST
            if (!requestLines.isEmpty())
            {
                String[] commandWords = requestLines.get(0).split("\\s+");
            if (commandWords[0].equals("SETUP-CONNECTION"))
                {
                    System.out.println(sock.getInetAddress() + " is trying to SETUP a CONNECTION");
                    Account account;
                    if(commandWords[1]!= null) 
                    {
                        String username = commandWords[1];
                        if (commandWords[2] != null && server.isAUser(username))
                        {
                            String password = commandWords[2];
                            account = server.login(username, password);
                            responseCode = 401;
                            if (account != null ) {
                                System.out.println("User logged in: " + account.getUsername());
                                responseCode = 100;
                            }
                        } else if (commandWords[2] == null && !server.isAUser(username)) {
                            server.createGuestAccount(username);
                            account = server.login(username, "GUESTSESSION");
                            System.out.println("Guest logged as: " + account.getUsername());
                            responseCode = 100;
                        } else {
                            //INVALID REQUEST SYNTAX
                            responseCode = 400;
                        }
                    }
                } else {
                    responseCode = 501;
                }
            } else {
                responseCode = 400;
                System.out.println("Empty Request");
            }
            System.out.println("Connection Terminated");
            //"SETUP-CONNECTION {username] <password>}
            //#ACCEPTED/DENIED {ACCEPTED: [RoomHandler] <Account>}
            returnMsg = responseCode(responseCode) + returnMsg;
            OutputStream os = sock.getOutputStream();
            System.out.println(returnMsg);
            os.write(returnMsg.getBytes());
            os.close();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
        }
    }
    
    
    //CLIENT CODE
    
    public class ConclaveClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            Socket outSocket = new Socket("192.168.0.20", 8080);
            String msg = "SETUP-CONNECTION Pozas 786";
            OutputStream outStream = outSocket.getOutputStream();
            InputStream is = outSocket.getInputStream();
            outStream.write(msg.getBytes());
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String nextLine = "";
            while (br.ready()) 
            {
                nextLine = br.readLine();
                System.out.println(nextLine);
            }
            outStream.close();
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    }
