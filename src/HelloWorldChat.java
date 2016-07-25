import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by renan on 7/24/16.
 */
public class HelloWorldChat {

    JChannel channel;

    public void runCliente() throws Exception {
        /* Instanciando o canal */
        channel = new JChannel("udp.xml");

        /* Configurando o receiver */
        channel.setReceiver(new ReceiverAdapter() {
            @Override
            public void receive(Message msg) {
                System.out.println("Mensagem Source:" + msg.getSrc() + " Destino: " + msg.getDest() + " Obj: " + msg.getObject());
            }

            @Override
            public void viewAccepted(View view) {
                System.out.println("New View: " + view.toString());
            }
        });

        // Conectando ao canal

        channel.connect("chat");

        eventLoop();

        channel.close();
    }


    public void eventLoop(){
        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        Boolean flag = true;

        while(true){
            System.out.print(">");
            try{
                line = teclado.readLine();
                Message msg = new Message(null, null, line);
                channel.send(msg);

            }catch (Exception e){

            }
        }


    }

    public static void main(String[] args) throws Exception{
        new HelloWorldChat().runCliente();
    }
}
