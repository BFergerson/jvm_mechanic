jvm_mechanic - Lightweight JVM Performance Diagnostics
==================================
[![Build Status](https://travis-ci.org/BFergerson/jvm_mechanic.svg?branch=master)](https://travis-ci.org/BFergerson/jvm_mechanic)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/BFergerson/jvm_mechanic/master/LICENSE)

# Goal
Provide detailed real-time statistics & diagrams for thread-based request/response work streams processing on the JVM. Insights provided can then be used to diagnose difficult to reproduce runtime performance degradation issues.

# Method
Using Byteman, inject event generating code in pre-defined locations related to the desired work stream. During normal execution Byteman-injected code generates events which are fed into a data stream. These events mark the time of their occurrence and several other useful data points. This data stream is then used to generate real-time session & method level diagrams to assist in monitoring and diagnosing application performance degradation.
 
# Dashboard
<img src="http://i.imgur.com/vlGAlkX.jpg" />

# Technical Design

### WorkRequest
Request by client to process and respond to (request/response model)

### WorkProcessor
Component that processes work requests (request/response model)

### WorkSession
Complete life-cycle of a single request/response

### WorkStream
Aggregate collection of work sessions

### MechanicEvent
Event triggered during the processing of a work stream.

Each event contains the following base metrics (as well as metrics relevant to that event):
 - Current time
 - Context (App/Server/Method/Work session/Etc)

## Collected Data Points
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
 - Active/total work sessions
 - Method duration/frequency predictability

## Available Insights
 - What is the current real-time work request processing duration?
 - To what degree is the volatility of work request processing durations?
 - What is the current real-time work stream method(s) processing duration?
 - To what degree is the volatility of work stream method(s) processing durations?
 - Is work stream processing degradation due to application or server code?
 - Which work stream method(s) are most susceptible to performance degradation?
 - To what degree do work stream method(s) experiencing degradation cause others method(s) to degrade?
