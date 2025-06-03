#!/bin/sh

# Start the RMI registry and the VotingServer
java -cp target/classes -Djava.security.policy=security.policy -Djava.rmi.server.hostname=0.0.0.0 com.votingsystem.server.VotingServer
