package software.ulpgc.kata5;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Uso: java -jar fibonacci-service-1.0.jar <id> <host <port> <consulUrl>");
            System.exit(1);
        }
        Webservice webservice = new Webservice(args[0], args[1], Integer.parseInt(args[2]), args[3]);
        webservice.start();
    }
}
