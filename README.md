# astrolabe: An Akka Cluster Console

## A more current version of this project is [here](https://github.com/boldradius/cluster-console)

##-


A burgeoning web-based console<sup>1</sup> for realtime visualization of Akka Clusters.

<small>1</small><sub>This code is heavily influenced by–and owes its existence to–[ochron's ScalaJS Single Page Application Tutorial](https://github.com/ochrons/scalajs-spa-tutorial)</sub>.

**Creator**:

  + [Dave Sugden](http://boldradius.com/team/Uv51hAEAAIFyKXWR/dave-sugden), [<img src="http://bytes.codes/images/br-circle.png" width="20"> BoldRadius Solutions](http://boldradius.com) [<img src="http://bytes.codes/images/black_circle-twitter_icon.png" width=20">](http://twitter.com/fritzsss) [<img src="http://bytes.codes/images/black_circle-github_icon.png" width=20">](http://github.com/dsugden) 

**Contributors**:

  + [Brendan McAdams](http://boldradius.com/team/VSvjIykAACkAyy8C/brendan-mcadams), [<img src="http://bytes.codes/images/br-circle.png" width="20"> BoldRadius Solutions](http://boldradius.com) [<img src="http://bytes.codes/images/black_circle-twitter_icon.png" width=20">](http://twitter.com/rit) [<img src="http://bytes.codes/images/black_circle-github_icon.png" width=20">](http://github.com/bwmcadams) 

## Features

'Astrolabe' is a realtime, reactive, browser-based console intended to provide visualization of any Akka Cluster, with live updating of changes in the cluster state, including member adds & removes. It currently visualizes several properties in diagrams ([outlined below](#visualization-sample-output)), utilizing [D3](http://d3js.org/) & [React.JS](http://facebook.github.io/react/).

It can also serve as an effective Demo/Sample project illustrating combining [Scala.JS](http://www.scala-js.org/) with [D3](http://d3js.org/) & [scalajs-react](https://github.com/japgolly/scalajs-react), for simple Akka Cluster topography overview.

### Current Features:

Currently the following features are supported:

  + Join any Akka Cluster (that isn't encrypted with SSL, as it would require you specially configure for the keys, ssl certs, etc) on the fly – configured via browser.
  + Visualize the topography of each Cluster
    - [Members View](#members-view): Shows each individual ActorSystem which is joined to the cluster, with information on their hostname/IP address, port, and configured roles.
    - [Roles View](#roles-view): Similar to Members View, shows each individual ActorSystem which is joined to the cluster, with information on their hostname/IP address, port, and configured roles. Additionally shows information on configured routers.
    - [Nodes View](#nodes-view): Shows each individual host (hostname/IP address), with each ActorSystem hanging off of that host by Port & Roles.

A number of future features are planned:
  
  + Cluster Metrics awareness: This will collect information about cluster metrics for each node in the system, and display an overview.
  + Actor Hierarchy overview: This will let you visualize the actor tree on each node in the system, displaying a diagram to understand the node.

## Visualization Sample Output

### Members View 
![members](/members.png?raw=true "Members")

### Roles View
![roles](/roles.png?raw=true "Roles")

### Nodes View
![nodes](/nodes.png?raw=true "Nodes")


##Getting started

To get started, we'll first need to boot the Spray HTTP Server (for running the console) and setup Scala.JS to recompile any changes.

1. Open 2 terminals, each running `sbt`.

2. In the first terminal, we'll need to start Spray (using [sbt-revolver](https://github.com/spray/sbt-revolver), so that it automatically restarts when we make code changes:

```sbt
> re-start
```

3. In the second terminal, we want Scala.JS to recompile our JavaScript when local changes are made:

```sbt
> ~fastOptJS
```
    
## Running the Sample Cluster
  A console alone isn't enough: we'll need some Akka nodes to visualize. To do this, we need a running Akka Cluster. 

  To boot up the sample Akka cluster, and test the behavior of the console, you have two options:
  - [Start It Locally with Multiple JVMs](#booting-the-sample-cluster-locally-with-multiple-jvms)
  - [Start It With Multiple VMs, Using Vagrant](#booting-the-sample-cluster-in-multiple-vms-with-vagrant)
    
#### Booting The Sample Cluster Locally with Multiple JVMs 
    
This approach will run multiple instances of the JVM, each with an Akka node in it, to facilitate testing. First, we'll need to create a zip file of the compiled project that we can run with.

```bash
sbt sampleCluster/dist

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

You'll need this Vagrant box: https://github.com/dsugden/vagrant-ansible-ubuntu-oracle-java8, packaged & installed (NOTE: the project Vagrantfile assumes the box is named `ubuntu/trusty64_oraclejava8`.

You'll then need to create a runnable distribution of the `SampleCluster` code to deploy on our sample VMs. There are two options:

  1. [Create and use a standard zip file](#building-a-zip-file)
  2. [Create and use a debian package file](#building-a-debian-package)

###### Building a Zip File

You'll need to generate a distribution zip file, and then unzip it so we can access it from our Vagrant VMs:

```bash
sbt sampleCluster/dist'

cd sampleCluster/target/universal
unzip samplecluster-1.0.0.zip
sudo chmod +x samplecluster-1.0.0/bin/samplecluster
```

Then, [continue on to start up your test nodes](#booting-the-vm-test-nodes)

###### Building a Debian Package

Since we're working with an Ubuntu VM for this style of testing, we have the option of using a Debian package.

Just tell SBT to create the Debian package, and we'll install it when we need it in a bit:

```
sbt sampleCluster/debian:packageBin
```

Then, [continue on to start up your test nodes](#booting-the-vm-test-nodes)

##### Booting the VM Test Nodes

Start up the Vagrant environment, which will boot 4 VMs for us, each capable of running nodes of the Akka `SampleCluster`: 

```bash
vagrant up
```


Then, we'll want 4 separate terminal windows or tabes, and to log in to each VM:

```bash
vagrant ssh seed
vagrant ssh member_2
vagrant ssh member_3
vagrant ssh member_4
```

On each of these nodes we'll need to make the Akka `SampleCluster` available to boot in several roles. We can either use the zip-based Universal package, or install our Debian package.

To use the zip-based Universal package (which we already unzipped), run the following on each VM:

```bash
export PATH=/vagrant/sampleCluster/target/universal/samplecluster-1.0.0/bin:$PATH
```

This will make the `samplecluster`–used to boot each Akka cluster node–available in your standard shell path.

Alternately, installing the Debian package on each of the 4 VMs, will also make it available on your path:

```bash
sudo dpkg -i /vagrant/sampleCluster/target/samplecluster_1.0.0_all.deb
```

We will then need to boot up a Seed node, which will act as the Primary cluster member (with a stable, known address) for other nodes to contact:

```bash
samplecluster 192.168.11.20 2551 FooCluster 192.168.11.20:2551 Stable-Seed &
```
    
Next, we'll boot up 3 sample nodes on `member_2`:
   
```bash
samplecluster 192.168.11.22 2552 FooCluster 192.168.11.20:2551 Baz-Security &
samplecluster 192.168.11.22 2553 FooCluster 192.168.11.20:2551 Baz-Security &
samplecluster 192.168.11.22 2554 FooCluster 192.168.11.20:2551 Foo-Worker &
```
    
And the same on `member_3`:
   
```bash
samplecluster 192.168.11.23 2555 FooCluster 192.168.11.20:2551 Foo-Worker &
samplecluster 192.168.11.23 2556 FooCluster 192.168.11.20:2551 Bar-Worker &
samplecluster 192.168.11.23 2557 FooCluster 192.168.11.20:2551 Bar-Worker &
```

Finally, we'll boot 2 Akka nodes on `member_4`:

```bash
samplecluster 192.168.11.24 2558 FooCluster 192.168.11.20:2551 Foo-Http &
samplecluster 192.168.11.24 2559 FooCluster 192.168.11.20:2551 Bar-Http &
```

![Have fun storming the castle!](http://bytes.codes/images/have-fun-storming-the-castle.gif)
