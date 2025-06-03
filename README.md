# Online Voting System using Java RMI

This is a simple online voting system implemented using Java RMI (Remote Method Invocation). The system allows users to authenticate, view available polls, cast votes, and view real-time results.

## Features

- User authentication with username/password
- Display of available polls
- Voting on polls with multiple options
- Real-time display of voting results
- Prevention of multiple votes from the same user
- Session management with timeouts
- Basic security implementation

## Architecture

The application follows a client-server architecture using Java RMI:

- **Server**: Implements the RMI service and manages the voting system
- **Client**: Provides a Swing-based GUI for users to interact with the system
- **Common**: Contains shared interfaces and classes used by both client and server

## Prerequisites

- Java Development Kit (JDK) 11 or higher
- Maven (for dependency management)

## Building the Application

To build the application, run the following command in the project root directory:

```bash
mvn clean package
```

## Running the Server

To run the server, use the following command:

```bash
java -cp target/online-voting-system-1.0-SNAPSHOT.jar -Djava.security.policy=security.policy -Djava.rmi.server.hostname=localhost com.votingsystem.server.VotingServer
```

## Running the Client

To run the client, use the following command:

```bash
java -cp target/online-voting-system-1.0-SNAPSHOT.jar -Djava.security.policy=security.policy com.votingsystem.client.LoginFrame [server-host]
```

Where `[server-host]` is the hostname or IP address of the server (defaults to "localhost" if not specified).

## Sample Users

The following sample users are available for testing:

- Username: `admin`, Password: `admin123`

## Security Considerations

- The application uses a basic security manager and policy file
- User authentication is implemented with simple username/password verification
- Session management with timeouts to prevent unauthorized access
- Input validation to prevent malicious data
- Prevention of multiple votes from the same user

## Educational Objectives

This project demonstrates:

- Understanding of Java RMI architecture
- Implementation of remote interfaces and methods
- Java serialization for object transmission
- Basic security implementation in distributed systems
- Client-server communication
- Exception handling in distributed applications
# RMI_VOTNG_SYSTEM
