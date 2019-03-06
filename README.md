# MCFirewall

Adds a firewall to your server in case you can't use a system firewall. Using a system firewall is still recommended.

Supports:

 - Forge
 - Sponge Forge
 - Sponge Vanilla

On loading, a `firewall.txt` file is created in the root directory (where `server.properties` is).

The format is a list of rules. Rules are processed in the order listed, first match wins.

Rules are given as an IP address range in [CIDR](https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing) notation. If a rule matches, the action is either to accept or reject.

Example firewall.txt:

```
# Allow loopback IP range
127.0.0.0/8 ALLOW

# Block everything else
0.0.0.0/0 BLOCK
```

ACCEPT and REJECT are also valid words.

Use `/firewall reload` from server console or an opped player to reload the rules from the file.

Every connection attempt is logged in the server console (and log file).  
In the event of a flood, the logger is rate limited:  
If more than 5 messages are logged each less than 2 seconds apart, then no messages are logged for the next 10 seconds.
