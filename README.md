# jvm_mechanic

##Goal
Provide detailed real-time statistics & diagrams for thread-based request/response work streams processing on the JVM. Insights provided can then be used to diagnose difficult to reproduce runtime performance degradation issues.

##Method
Using Byteman, inject event generating code in pre-defined locations related to the desired work stream. During normal execution Byteman-injected code generates events which are feed into a data stream. These events mark the time of their occurrence and several other useful data points. This data stream is then used to generate real-time function flow diagrams (and others) to assist in monitoring application performance degradation.

##Technical Design

###WorkRequest
Request by client to process and respond to (request/response model)

###WorkProcessor
Component that processes work requests (request/response model)

###WorkSession
Complete life-cycle of a single request/response

###WorkStream
Aggregate collection of work sessions

###MechanicEvent
Event triggered during the processing of a work stream.

Each event contains the following base metrics (as well as metrics relevant to that event):
 - Current time
 - Context (App/Server/Method/Work session/Etc)

##Collected Data Points
 - Method invocation count/frequency
 - Method runtime (absolute/relative/total)
 - Work stream processing rate/speed (min/max/mean/avg)
 - Measured/unmeasured work stream runtime/ratio
 - App/server runtime/ratio
 - Garbage collection time/ratio
 - Garbage collection contribution (max/min/mean/avg/ratio) by method (relative/absolute by method/stream)
 - Concrete & apparent performance correlations between methods
 - Monitored execution paths / Utilized execution paths
 - Various growth rates
 - Method exception rate/count/growth
 - Work session exception rate/count/growth
 - Work session success/fail/exception
 - Active/total work sesions
 - Method duration/frequency predictability

##Available Insights
 - What is the current real-time work request processing duration?
 - To what degree is the volatility of work request processing durations?
 - What is the current real-time work stream method(s) processing duration?
 - To what degree is the volatility of work stream method(s) processing durations?
 - Is work stream processing degradation due to application or server code?
 - Which work stream method(s) are most susceptible to performance degradation?
 - To what degree do work stream method(s) experiencing degradation cause others method(s) to degrade?

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
