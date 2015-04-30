# Maekawa-algorithm

Maekawa-algorith-implementation


Maekawa's algorithm is used for achieving mutual exclusion in distributed systems. All the features of the original algorithm were implemented. The algorithm is implemented in Java.  Major design decisions:  A process/node in the distributed system is treated as a thread. Then node can enter critical section asynchronously. But the times at which a process enters CS is implemented as defined in the problem statement. Processes use sockets to communicate with each other. Stream Sockets are used. Each process listens for messages on an unique address and port. Each process knows the addresses of all other processes in its Quorum. These addresses are hard coded in the program. No Name server was implemented to do this. The concept of CS is implemented in the program. No Synchronization primitives were used to define CS. But the logic in the program guarantees that processes access CS  mutually exclusively.           

int clock;          //Logical clock value exchanged(lamport)  
Messages: REQUEST=1,REPLY=2,RELEASE=3,GRANT=4,INQUIRE=5,FAILED=6,YEILD=7  

Compile Process:  
javac client.java  
javac Project2.java 
javac checker.java  

Run:  
java Project2 n    n being the server number in testconfig file 
java client n     n being the server number in testconfig file 

and this is the application where the routine can be changed only the cs_enter cs_exit and connection1 with a small file read script will remain the same.  there is also launcher scripts which can be used launcher_server.sh and launcher_clients.sh which can be used to automate the run process  

Error detection:  
Error:001 with quorum formations - Quorum property of intersection violated 
Error:002 with quorum formations - Quorum property of subset violated 
Error:003 with quorum formations - The number of quorums in which each process is should be the same 
Error 004:Error writing to the file 
Error 005:Error writing to the file, position not correct 
Error 006:Error reading file 
Error 007:James Bond error, Multiple access to Critical Section 
Error 008:Error reading config file
