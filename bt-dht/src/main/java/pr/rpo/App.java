package pr.rpo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pr.rpo.kbucket.NodeInfo;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 *
 */
public class App {
    private final static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main( String[] args ) throws InterruptedException {
        DhtImpl dhtImpl = DhtImpl.getSingleton();
        dhtImpl.listen();

        dhtImpl.bootstrap();

        CompletableFuture cf = CompletableFuture.supplyAsync(() -> {
            NodeInfo[] nodeInfos2 = null;

            while (true) {
                if(dhtImpl.cfList.size() > 5) {
                    synchronized (dhtImpl.cfList) {
                        dhtImpl.cfList = dhtImpl.cfList.stream().filter(e -> !e.isDone()).collect(Collectors.toList());
                    }
                }
                else {
                    if (dhtImpl.getRoutingTable().size() < 20) {
                        NodeInfo[] nodeInfos = dhtImpl.getRoutingTable().findNode(dhtImpl.localNodeId, dhtImpl.getRoutingTable().size());

                        if (nodeInfos.length != 0) {
                            for (NodeInfo n : nodeInfos) {
                                dhtImpl.findNodeSend(n.getContactInfo(), dhtImpl.localNodeId.getRaw());
                            }
                        }
                    } else {
                        NodeInfo[] nodeInfos = dhtImpl.getRoutingTable().findNode(dhtImpl.localNodeId);

                        if (nodeInfos.length != 0) {
                            //TODO
                            if (!Arrays.equals(nodeInfos, nodeInfos2)) {
                                nodeInfos2 = nodeInfos;
                                for (NodeInfo n : nodeInfos) {
                                    dhtImpl.findNodeSend(n.getContactInfo(), dhtImpl.localNodeId.getRaw());
                                }
                            }

                        }
                    }
                }

                try {
                    Thread.sleep(20000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        while(true) {
            String[] commands = {"print","log"};


            System.out.print("$:");
            Scanner sc = new Scanner(System.in);
            String s = sc.next();
            if(s.equals(commands[0])) {
                System.out.println(dhtImpl.getRoutingTable().prettyPrintString());
                dhtImpl.cfList.forEach(System.out::println);
            }else if(s.equals(commands[1])) {
                try(
                        FileReader fr = new FileReader("./dhtFile.log");
                ) {
                    char[] buffer = new char[1024];
                    while(fr.read(buffer) != -1) {
                        System.out.println(buffer);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                System.out.println("not support operate");
            }
        }
    }
}
