package leaderelection;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import pt.haslab.ekit.Clique;

import java.io.*;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class Main
{
    private Election el;
    private Clique c;
    private Class<Election> e;
    private Transport t;
    private Serializer s;
    private ThreadContext tc;
    private Address[] a;
    private int id;
    private int size;
    private int Async;
    private Runnable r[];

    private Main(Address a[],int id,int size,boolean Async)
    {
        this.a=a;
        el = new Election(a[id]);
        this.c = c;
        e = Election.class;
        t = new NettyTransport();
        s = new Serializer();
        s.register(e);
        tc = new SingleThreadContext("proto-%d", s);
        c = new Clique(t, id, a);
        this.id = id;
        this.size = size;
        if(Async)
            this.Async = 1;
        else
            this.Async = 0;
        r = new Runnable[2];
        r[0]=this::electSync;
        r[1]=this::electAsync;
    }

    public static void main(String []args) throws IOException {
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("How many processes");
        final int size = Integer.parseInt(bf.readLine());
        Address[] addresses = new Address[size];
        for (int i=size-1; i>=0; i--)
        {
            addresses[i] = new Address("127.0.0.1:" + (12345 + i));
        }
        System.out.println("Id for this one?");
        final int id = Integer.parseInt(bf.readLine());
        System.out.println("Async?");
        final boolean Async = Boolean.parseBoolean(bf.readLine());
        System.out.println("Id is : " + id);
        final Main m = new Main(addresses,id,size,Async);
        m.execute();
    }

    private void execute() {
        tc.execute(() -> {
            c.handler(e, (j,msg)-> {
                // message handler
                System.out.println("Int " + j);
                el.join(msg);
                System.out.println("Joining :" + msg.publicize());
            }).onException(ex->{
                // exception handler
                ex.printStackTrace();
            });


            c.open().thenRun(r[Async]);
        }).join();
    }

    private void electSync()
    {
        // initialization code
        System.out.println("Starting");
        for(int i=0;i<size;i++)
        {
            c.send(i,el);
        }
        ThreadContext.currentContext().schedule(Duration.ofSeconds(8),() -> {
            el.elect();
            if (el.isLeader()) {
                System.out.println("Sou o lider");
            }
        });

    }


    private void electAsync()
    {
        // initialization code
        System.out.println("Starting");
        for(int j=0;j<4;j++)
        {
            ThreadContext.currentContext().schedule(Duration.ofSeconds(4*j),() -> {
            System.out.println("Send");
            for(int i=0;i<size;i++)
                c.send(i,el);
            });
        }
        ThreadContext.currentContext().schedule(Duration.ofSeconds(20),() -> {
            el.elect();
            if (el.isLeader()) {
                System.out.println("Sou o lider");
            }
        });
    }
}
