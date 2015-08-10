# cluster-console

front end to view akka cluster topography

This code is heavily influenced and owes it's existence to: https://github.com/ochrons/scalajs-spa-tutorial

d3 facade from https://github.com/spaced/scala-js-d3





to run in dev:


1) open 2 terminals.
2) in first terminal:

     sbt
     re-start
     
3) in second terminal:

    sbt
    ~fastOptJS
    
    
    
#### Sample Cluster
    
    
To start the sample cluster:
    

    sbt 'project sampleCluster' 'dist'
    
    cd sampleCluster/target/universal
    unzip samplecluster-1.0.0.zip 
    sudo chmod +x samplecluster-1.0.0/bin/samplecluster
    
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2551 FooCluster 127.0.0.1:2551 Stable-Seed &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2552 FooCluster 127.0.0.1:2551 Baz-Security &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2553 FooCluster 127.0.0.1:2551 Baz-Security &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2554 FooCluster 127.0.0.1:2551 Foo-Worker &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2555 FooCluster 127.0.0.1:2551 Foo-Worker &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2556 FooCluster 127.0.0.1:2551 Bar-Worker &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2557 FooCluster 127.0.0.1:2551 Bar-Worker &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2558 FooCluster 127.0.0.1:2551 Foo-Http &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2559 FooCluster 127.0.0.1:2551 Bar-Http &
    

    
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2661 BazCluster 127.0.0.1:2661 Stable-Seed &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2662 BazCluster 127.0.0.1:2661 Baz-Security &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2663 BazCluster 127.0.0.1:2661 Foo-Worker &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2664 BazCluster 127.0.0.1:2661 Bar-Worker &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2665 BazCluster 127.0.0.1:2661 Foo-Http &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2666 BazCluster 127.0.0.1:2661 Bar-Http &
    
    
    

To stop a particular actor by port:    

OSX:    
    
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
     

Nix:

    fuser -k -n tcp 2551

etc.