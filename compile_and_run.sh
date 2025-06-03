#!/bin/bash

# Script to compile and run the Online Voting System

# Compile the application
echo "Compiling the application..."
mvn clean package

if [ $? -ne 0 ]; then
    echo "Compilation failed. Please fix the errors and try again."
    exit 1
fi

echo "Compilation successful!"

# Function to run the server
run_server() {
    echo "Starting the server..."
    java -cp target/online-voting-system-1.0-SNAPSHOT.jar -Djava.security.policy=security.policy -Djava.rmi.server.hostname=localhost com.votingsystem.server.VotingServer
}

# Function to run the client
run_client() {
    echo "Starting the client..."
    java -cp target/online-voting-system-1.0-SNAPSHOT.jar -Djava.security.policy=security.policy com.votingsystem.client.LoginFrame localhost
}

# Check command line arguments
if [ "$1" == "server" ]; then
    run_server
elif [ "$1" == "client" ]; then
    run_client
else
    echo "Usage: $0 [server|client]"
    echo "  server - Run the server"
    echo "  client - Run the client"
    exit 1
fi
