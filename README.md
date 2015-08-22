# cluster-console

Sample project illustrating combining D3 with [scalajs-react](https://github.com/japgolly/scalajs-react)  .

This project provides simple views for akka cluster topography.

This code is heavily influenced by and owes its existence to: https://github.com/ochrons/scalajs-spa-tutorial.



![members](/members.png?raw=true "Members")

![roles](/roles.png?raw=true "Roles")

![nodes](/nodes.png?raw=true "Nodes")


###Getting started

To get started, we'll first need to boot the Spray HTTP Server (for running the console) and setup Scala.JS to recompile any changes.

1. Open 2 terminals.

2. In the first terminal, we'll need to start Spray (using [sbt-revolver](https://github.com/spray/sbt-revolver), so that it automatically restarts when we make code changes:

```bash
sbt 're-start'
```

3. In the second terminal, we want Scala.JS to recompile our JavaScript when local changes are made:

```bash
sbt '~fastOptJS'
```
    
### Running the Sample Cluster
  A console alone isn't enough: we'll need some Akka nodes to visualize. To do this, we need a running Akka Cluster. 

  To boot up the sample Akka cluster, and test the behavior of the console, you have two options:
  - [Start It Locally with Multiple JVMs](#booting-the-sample-cluster-locally-with-multiple-jvms)
  - [Start It With Multiple VMs, Using Vagrant](#booting-the-sample-cluster-in-multiple-vms-with-vagrant)
    
#### Booting The Sample Cluster Locally with Multiple JVMs 
    
This approach will run multiple instances of the JVM, each with an Akka node in it, to facilitate testing. First, we'll need to create a zip file of the compiled project that we can run with.

```bash
sbt 'project sampleCluster' 'dist'

cd sampleCluster/target/universal
unzip samplecluster-1.0.0.zip
sudo chmod +x samplecluster-1.0.0/bin/samplecluster
```

Once this is done, we'll have a fully functional Sample Akka Cluster in `samplecluster-1.0.0/bin/samplecluster`. Now we can start our multiple JVMs to give us some Akka nodes; in this example we're going to start two separate clusters – `FooCluster`, and `BazCluster`

##### FooCluster

To get started with the `FooCluster`, we will need a stable seed node. We can boot this as follows:
   
```bash
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2551 FooCluster 127.0.0.1:2551 Stable-Seed &
```
Next, we'll boot up a bunch of sample actors:

```bash
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2552 FooCluster 127.0.0.1:2551 Baz-Security &
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2553 FooCluster 127.0.0.1:2551 Baz-Security &
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2554 FooCluster 127.0.0.1:2551 Foo-Worker &
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2555 FooCluster 127.0.0.1:2551 Foo-Worker &
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2556 FooCluster 127.0.0.1:2551 Bar-Worker &
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2557 FooCluster 127.0.0.1:2551 Bar-Worker &
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2558 FooCluster 127.0.0.1:2551 Foo-Http &
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2559 FooCluster 127.0.0.1:2551 Bar-Http &
```    

##### BazCluster

To get started with the `BazCluster`, we will need a stable seed node as well. We can boot this as follows:
    
```bash
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2661 BazCluster 127.0.0.1:2661 Stable-Seed &
```

Finally, we'll boot up a bunch of sample actors:

```bash
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2662 BazCluster 127.0.0.1:2661 Baz-Security &
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2663 BazCluster 127.0.0.1:2661 Foo-Worker &
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2664 BazCluster 127.0.0.1:2661 Bar-Worker &
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2665 BazCluster 127.0.0.1:2661 Foo-Http &
samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2666 BazCluster 127.0.0.1:2661 Bar-Http &
```

##### Maintenance and Shutdown

To stop a particular actor by port...

On Mac OS X:    

```bash   
kill -9 `lsof -i tcp:2551 | grep -i LISTEN`
kill -9 `lsof -i tcp:2552 | grep -i LISTEN`
kill -9 `lsof -i tcp:2553 | grep -i LISTEN`
kill -9 `lsof -i tcp:2554 | grep -i LISTEN`
kill -9 `lsof -i tcp:2555 | grep -i LISTEN`
kill -9 `lsof -i tcp:2556 | grep -i LISTEN`
kill -9 `lsof -i tcp:2557 | grep -i LISTEN`
kill -9 `lsof -i tcp:2558 | grep -i LISTEN`
kill -9 `lsof -i tcp:2559 | grep -i LISTEN`
     
kill -9 `lsof -i tcp:2661 | grep -i LISTEN`
kill -9 `lsof -i tcp:2662 | grep -i LISTEN`
kill -9 `lsof -i tcp:2663 | grep -i LISTEN`
kill -9 `lsof -i tcp:2664 | grep -i LISTEN`
kill -9 `lsof -i tcp:2665 | grep -i LISTEN`
kill -9 `lsof -i tcp:2666 | grep -i LISTEN`
```

\*Nix:

```bash
fuser -k -n tcp 2551
```

etc.


#### Booting the Sample Cluster In Multiple VMs with Vagrant 

you'll need this box: https://github.com/dsugden/vagrant-ansible-ubuntu-oracle-java8, install (NOTE: the project Vagrantfile assumes the box is named `ubuntu/trusty64_oraclejava8`) it, then:

```bash
vagrant up
```


Log in to each VM:

```bash
vagrant ssh seed
vagrant ssh member_2
vagrant ssh member_3
vagrant ssh member_4
```

then, on each

```bash
sudo apt-get install unzip
cp /vagrant/sampleCluster/target/universal/samplecluster-1.0.0.zip .
unzip samplecluster-1.0.0.zip
sudo chmod +x samplecluster-1.0.0/bin/samplecluster
```

seed

```bash
samplecluster-1.0.0/bin/samplecluster 192.168.11.20 2551 FooCluster 192.168.11.20:2551 Stable-Seed &
```
    
member_2
   
```bash
samplecluster-1.0.0/bin/samplecluster 192.168.11.22 2552 FooCluster 192.168.11.20:2551 Baz-Security &
samplecluster-1.0.0/bin/samplecluster 192.168.11.22 2553 FooCluster 192.168.11.20:2551 Baz-Security &
samplecluster-1.0.0/bin/samplecluster 192.168.11.22 2554 FooCluster 192.168.11.20:2551 Foo-Worker &
```
    
member_3
   
```bash
samplecluster-1.0.0/bin/samplecluster 192.168.11.23 2555 FooCluster 192.168.11.20:2551 Foo-Worker &
samplecluster-1.0.0/bin/samplecluster 192.168.11.23 2556 FooCluster 192.168.11.20:2551 Bar-Worker &
samplecluster-1.0.0/bin/samplecluster 192.168.11.23 2557 FooCluster 192.168.11.20:2551 Bar-Worker &
```

member_4    

```bash
samplecluster-1.0.0/bin/samplecluster 192.168.11.24 2558 FooCluster 192.168.11.20:2551 Foo-Http &
    samplecluster-1.0.0/bin/samplecluster 192.168.11.24 2559 FooCluster 192.168.11.20:2551 Bar-Http &
````
