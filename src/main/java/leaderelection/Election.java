package leaderelection;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class Election implements Serializable
{
    TreeSet<String> sa;
    String own;
    boolean isLeader;

    public Election()
    {
        sa =null;
        own = null;
        isLeader = false;
    }

    public Election(Address own)
    {
        sa = new TreeSet<>();
        this.own=own.host() + ":" + own.port();
        isLeader = false;
        sa.add(this.own);
    }

    public void join(Election m)
    {
        sa.addAll(m.publicize());
    }

    protected Set<String> publicize()
    {
        return sa;
    }

    public void elect()
    {
        if (sa.size()!=1) {
            if (own == sa.first()) {
                isLeader = true;
            }
        }
        else
        {
            throw new RuntimeException();
        }
    }

    public boolean isLeader()
    {
        return isLeader;
    }

}
