#!/bin/sh

# Start the client application
# The server hostname is passed as an argument
java -cp target/classes -Djava.security.policy=security.policy com.votingsystem.client.LoginFrame $1
