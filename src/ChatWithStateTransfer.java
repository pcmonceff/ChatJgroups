import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by renan on 7/24/16.
 */
public class ChatWithStateTransfer extends ReceiverAdapter {

    private JChannel channel;

    /* Objetio que sera transferido */
    private final List<String> history = new ArrayList<>();
    private final Lock historyLock = new ReentrantLock();


    @Override
    public void viewAccepted(View view) {
        System.out.println("New view :: " + view.toString());
    }

    /* usado pelo coordenador para serializar o estado atual */
    @Override
    public void getState(OutputStream output) throws Exception {
        DataOutput out = new DataOutputStream(output);
        historyLock.lock();

        try {
            Util.objectToStream(history, out);
        }finally {
            historyLock.unlock();
        }

        System.out.println("Estado Atual Serializado!");
    }


    /* Usado pelo novo no para obter o estado atual */
    @Override
    public void setState(InputStream input) throws Exception {
        historyLock.lock();

        try {
            DataInput in = new DataInputStream(input);
            List<String> newState = (List<String>) Util.objectFromStream(in);
            history.clear();
            history.addAll(newState);
        }finally {
            historyLock.unlock();
        }

        System.out.println("Estado Deserializdo: " + history.toString());

    }

    public void runClient() throws Exception {
        channel = new JChannel("udp.xml");
        RpcDispatcher dispatcher = new RpcDispatcher(channel, this, this, this);
        channel.connect("chat");
        dispatcher.start();
        /* Obtendo estado */
        channel.getState(null, 10_000);

        /* para ler do teclado */
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        while(true){

            line = bufferedReader.readLine();
            switch (line){
                case "history":
                    System.out.println("Historico: " + history.toString());
                    break;
                default:
                    MethodCall sendMessage = new MethodCall(getClass().getDeclaredMethod("printOut", String.class), line);
                    dispatcher.callRemoteMethods(null, sendMessage, RequestOptions.SYNC());
            }

        }

    }

    public static void main(String[] args) throws Exception {
        new ChatWithStateTransfer().runClient();
    }


    public void printOut(String text){
        System.out.println("Nova Mensage: " + text);
        history.add(text);
    }

}