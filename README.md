# jvm_mechanic

##Goal
Provide real-time function flow diagrams (and others) for real-time server work requests and processing on the JVM.

##Method
Using Byteman, inject event generating code in pre-defined locations related to the desired work stream. During normal execution Byteman-injected code generates events which are feed into a data stream. These evente note the time of their occurance and several other useful data points. This data stream is then used to generate real-time function flow diagrams (and others) to assist in monitoring application performance degredation.

##Todo
 - Basic events in Byteman
 - Determine all data points to collect
 - Determine method of collection for method invocations related to work stream
 - Process to turn collection of method invocations into rules which will inject the proper events
 - Find fast stream to file library
 - Find function flow diagram library
 - Process to tail data stream and make necessary calculations to create insights/charts
 - Process to turn file/stream into DB table(s)
