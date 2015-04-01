# Network Delegate
The Network Delegate API used for splitting a finite amount of Ports between an infinite amount of Sockets  

Master | Development  
[![Build status](https://travis-ci.org/Open-RIO/NetworkDelegate.svg?branch=master)](https://travis-ci.org/Open-RIO/NetworkDelegate/)
[![Build status](https://travis-ci.org/Open-RIO/NetworkDelegate.svg?branch=development)](https://travis-ci.org/Open-RIO/NetworkDelegate/)  

##An Introduction  
Network Delegate is an API that allows for an infinite amount of Sockets (or channels), to be shared on a finite amount of Ports. This is commonly seen in the [Toast API](http://github.com/Open-RIO/ToastAPI), where each module may require it's own Socket, but the Field Management System only allows for 10 ports to be opened and accessible per robot.

This is overcome by use of a 'Master' Distributer Socket that will assign each client a UUID and a port to connect to. When the client connects to that port, the Client sends over its UUID. Upon this connection, the server checks the UUID and will make the Client connect to the specified Delegate (Module). This allows for an infinite amount of connections to be established over a finite amount of sockets, while still allowing the Developer and Clients to treat the sockets as if they were standard connections.

##How to Use  
The Network Delegate library is extremely simple to setup. To setup a new Delegate, follow the instructions below:  
###Server
- Create a new DelegateServer: ```DelegateServer server = new DelegateServer(masterPort, slavePort1, slavePort2...)``` OR ```DelegateServer server = DelegateServer.createRange(masterPort, startPort, endPort)```
- Assign your Delegates: ```BoundDelegate delegate = server.requestDelegate(delegateID)```
- Launch the DelegateServer: ```server.launchDelegate()```

In order to handle the data going to and from your BoundDelegate, call ```BoundDelegate.getConnectedSockets()```  
This will return a Vector of all the Clients connected to your delegate. You can handle these sockets just like you would regular sockets.

###Client  
- Create a new DelegateClient: ```DelegateClient client = new DelegateClient(serverHost, serverMasterPort, delegateID)```
- Launch the DelegateClient using: ```client.connect()```

In order to handle the data going to and from your DelegateClient, call ```DelegateClient.getSocket()``` to gain access to the socket itself. This allows you to use the Client just as if it were a regular socket.  

###Custom Implementation
If you wish to not use DelegateClient, below is a list of instructions on how to use the Network Delegate lifecycle in your own applications.

**NOTE: Make sure all lines sent end with a new line (\n)**  
1) Connect to the master socket  
2) Send "REQUEST [delegate_ID]" where delegate_id is the delegate you wish to connect to.  
3) The server will send back "[port_id] [uuid]", where:  
&nbsp;&nbsp;&nbsp;&nbsp;- port_id = The port number assigned to your connection, or -1 if an error occurred  
&nbsp;&nbsp;&nbsp;&nbsp;- uuid = Your client UUID. This is used later. This will be replaced by an Error Message if a port_id of -1 is provided.  
4) Disconnect from the Master Socket.  
5) Connect to the server on the port specified in step 3  
6) Send "TUNNEL [uuid]" to the server, where uuid is the uuid received in step 3  
7) If all is well, the server will send back "SUCCESS". You can now use the socket normally. If not, the server will send back an error message.
