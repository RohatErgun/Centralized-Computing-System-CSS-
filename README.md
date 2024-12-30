## Implementation Of CSS Class

### Overview And Protocols
This project involves the implementation of a **Centralized Computing System (CSS)** in Java. The application provides the following key functionalities:

1. **UDP Service Discovery**:
    - Listens for broadcast messages starting with `CSS DISCOVER`.
    - Responds with `CSS FOUND` to help clients discover the server.

2. **TCP Client Communication**:
    - Supports multiple concurrent clients using threads.
    - Processes requests in the format `<OPER> <ARG1> <ARG2>`, where:
        - `OPER`: One of `ADD`, `SUB`, `MUL`, `DIV`.
        - `ARG1` and `ARG2`: Integers representing operands.
    - Computes the requested operation and returns the result or an error message if the input is invalid.

3. **Statistics Reporting**:
    - Periodically (every 10 seconds), prints statistics:
        - Total clients connected.
        - Total requests processed.
        - Operation counts for `ADD`, `SUB`, `MUL`, and `DIV`.
        - Number of invalid requests.
        - Sum of computed results.

-----

### Execution
#### Running the Server:
* java CSS.java <port_number>

#### Client Communication:
Since there is no strict requirement for client implementation, I have chose to
test the server communication using `telnet` command.
Which is allows to send text based requests.

And it is a way to test the UTP communication.

1.**Open new terminal and use telnet command**
<br>Telnet opens a TCP connection to given server and allows the user to type manually.

In different terminal : `telnet <loopback address> <port number>`
<br>If server runs from your own computer:
* 127.0.0.1 (loopback address). 
* Your computer's actual IP address (e.g., 192.168.1.40 for local network testing).

<br>If another user download CSS class and test the TCP connection they should use their own ip address.

2. **Sending Requests**
<br>After connecting type request using `<OPER> <ARG1> <ARG2>`.Example `SUB 55 10`.
Server should respond with 45. 
3. **Disconnection**
<br>Can be done with  `Ctrl + ]` then quit the terminal.

----

### Code Implementation And Explanation 
1. **CSS CLASS**
- Handles service discovery and client communication.
- Uses `ExecutorService` to manage threads for UDP and TCP services.
- There are variables for storing the various counting operation.
- `ConcurrentHashMap` and `CopyOnWriteArrayList` is a Thread-Safe versions of the Hashmap and Arraylist.
- Constructor only initialize the port number that given by the user.

2. **`start` method**:
- Responsible for launching 3 core service UDP service , TCP service and periodic statistics.
- Uses `ExecuterService` which is creating a Thread pool for managing multiple threads.
- And submits the methods in to the Thread poll.

3. **`startUdpService` method**:
- `DatagramSocket` : Opens a UDP socket with given port number use for sending UDP packets.
- `buffer`: A byte of array stores incoming messages.
- Starts to listen for UDP packets.
- Creates a packet for holding incoming data.
- Receives the UDP packet and converts the raw-byte to String.
- Then, Validating Discovery Request with constant DISCOVER_MESSAGE.
- Converts the response string to "CSS FOUND" and send back to client.
- Uses `DatagramPacket` to send with `udpSocket.send()`.

4. **`handleClient` method:**
- `BufferedReader in` reads incoming data requests from the client with TCP connection.
- `PrintWrite out` sends response back to client.
- Starts processing requests in a loop and reads a line of input(request).
- Logs the received request `clientRequests` for statistics.
- Using `request.split()` check if the format is valid and check `arg[0](OPER)` is a String. and `<ARG1> <ARG2>` is number.
- After that,performs corresponding operation based on given operation by client.
- Lastly, updates statistics and send back the response.

5. **`displayStats` method:**
- While server is running, it sends the statistic of data every 10 second(10000 millis).
- Sends `[number of totatclients,Total requests,Operation Counts,Incorrect requests,Sum of results]`

----
### Difficulties And Errors

#### Challenges Faced:

Concurrent client management : Ensuring proper synchronization and using Thread-Safe data structures.

Loopback Address , might be confusing while sending tcp requests I tested using `telnet` command.

Possible to use different methods which I have not test on it.

#### Errors Encountered:

1. Infinite loop error Fixed by updating the request inside the loop `(`while ((request = in.readLine()) != null)`)`.
2. Incorrect Thread Usage : Resolved by passing a lambda to `new Thread` and calling `.start()` correctly.
3. Division by Zero : Added explicit checks and error messages for `DIV` operations with zero as the second argument.

#### Lessons Learned
- Importance of validating input and handling edge cases.
- Using thread-safe data structures for shared resources.
- Effective debugging techniques for networked applications.
