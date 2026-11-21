package software.ulpgc.kata5;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: kata5 <input> <output>");
            System.exit(1);
        }
        Webservice webservice = new Webservice(args[0], args[1], Integer.parseInt(args[2]), args[3]);
        webservice.start();
    }
}
