import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;

/**
 * Created by renan on 7/24/16.
 */
public class ChatWithRPC extends ReceiverAdapter {

    JChannel channel;

    @Override
    public void viewAccepted(View view) {
        System.out.println("New View : " + view.toString());
    }

    public void runClient() throws Exception{
        channel = new JChannel("udp.xml");
        RpcDispatcher dispatcher = new RpcDispatcher(channel, this, this, this);
        channel.connect("chat");


        /* Setando Loop */
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        while(true){
            System.out.print(">");
            line = bufferedReader.readLine();
            MethodCall sendMessage = new MethodCall(this.getClass().getDeclaredMethod("printOut", String.class), line);
            RspList rsps = dispatcher.callRemoteMethods(null, sendMessage, RequestOptions.SYNC());
            System.out.println("Responses : " + rsps.toString());


        }


    }

    public void printOut(String text){
        System.out.println("New Message: " + text);
    }

    public static void main(String[] args) throws Exception {


        new ChatWithRPC().runClient();
    }

}
