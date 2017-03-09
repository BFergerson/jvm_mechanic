# jvm_mechanic

##Goal
Provide real-time function flow diagrams (and others) for real-time server work requests and processing on the JVM.

##Method
Using Byteman, inject event generating code in pre-defined locations related to the desired work stream. During normal execution Byteman-injected code generates events which are feed into a data stream. These events mark the time of their occurrence and several other useful data points. This data stream is then used to generate real-time function flow diagrams (and others) to assist in monitoring application performance degradation.

##Collected Data Points
 - Method invocation count/frequency
 - Method runtime (absolute/relative/total)
 - Work stream processing rate/speed (min/max/mean/avg)
 - Measured/Unmeasured work stream runtime/ratio
 - App/server runtime/ratio
 - Garbage collection time/ratio
 - Garbage collection contribution (max/min/mean/avg/ratio) by method (relative/absolute by method/stream)
 - Concrete & apparent performance correlations between methods
 - Monitored execution paths / Utilized execution paths
 - Various growth rates

##Available Insights
 - What is the current real-time work request processing duration?
 - To what degree is the volatility of work request processing durations?
 - What is the current real-time work stream method(s) processing duration?
 - To what degree is the volatility of work stream method(s) processing durations?
 - Is work processing degradation due to application or server code?
 - Which method(s) are most susceptible to performance degradation?
 - Could method(s) experiencing degradation cause others method(s) to experience degradation and to what degree?

##Todo
 - Basic events in Byteman
 - Determine all data points to collect
 - Determine method of collection for method invocations related to work stream
 - Process to turn collection of method invocations into rules which will inject the proper events
 - Event processor/log
 - Find fast stream to file library
 - Find function flow diagram library
 - Process to tail data stream and make necessary calculations to create insights/charts
 - Process to turn file/stream into DB table(s)
