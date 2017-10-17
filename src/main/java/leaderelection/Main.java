package leaderelection;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Set;

public class Main
{
    public static void main(String []args) throws IOException {
        Address[] addresses = new Address[4];
        for (int i=3; i>=0; i--)
        {
            addresses[i] = new Address("127.0.0.1:123" + i);
        }
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        final int id = Integer.parseInt(bf.readLine());
        Election el = new Election(addresses[id]);
        Transport t = new NettyTransport();
        ThreadContext tc = new SingleThreadContext("proto-%d", new Serializer());

        tc.execute(() -> {
            Clique c = new Clique(t, id, addresses);
            Class<Election> e = Election.class;
            c.handler(e, (j,m)-> {
                // message handler
                System.out.println("Int " + j);
                el.join(m);
                System.out.println("Joining :" + m.publicize());
            }).onException(ex->{
                // exception handler
                ex.printStackTrace();
            });

            c.open().thenRun(() -> {
                // initialization code
                for(int i=0; i<4;i++)
                {
                    c.send(i,el);
                }
                ThreadContext.currentContext().schedule(Duration.ofSeconds(12),() -> {
                    el.elect();
                    if (el.isLeader())
                    {
                        System.out.println("Sou o lider");
                    }
                });
            });
        }).join();
    }
}
