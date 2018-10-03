# epsilon-dashboard
A springboot application with UI templates for monitoring and other applications on an epsilon cluster

Changes :

Some changes according to your RM ips since its not fully configurable yet.

search: 172.29.20.48 and replace by your RM ip.


Bulilding:
mvn clean install

Running:
Move the jar to the gateway docker.
There are some prerequisite though.

java -jar eplsilon-dashboard(version).jar
