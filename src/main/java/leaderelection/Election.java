package leaderelection;

import io.atomix.catalyst.transport.Address;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class Election
{
    TreeSet<Address> sa;
    Address own;
    boolean isLeader;

    public Election(Address own)
    {
        sa = new TreeSet<>(Comparator.comparing(Address::host).thenComparing(Address::port));
        this.own=own;
        isLeader = false;
        sa.add(own);
    }

    public void join(Election m)
    {
        sa.addAll(m.publicize());
    }

    protected Set<Address> publicize()
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
