# cluster-console

front end to view akka cluster topography

This code is heavily influenced and owed it's existence to: https://github.com/ochrons/scalajs-spa-tutorial


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
    

    sbt
    project sampleCluster
    dist
    exit
    
    cd sampleCluster/target/universal/
    unzip samplecluster-1.0.0.zip 
    sudo chmod +x samplecluster-1.0.0/bin/samplecluster
    
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2551 BackEnd &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2552 BackEnd &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2553 BackEnd &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2554 BackEnd &    
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2555 FrontEnd &
    samplecluster-1.0.0/bin/samplecluster 127.0.0.1 2556 FrontEnd &
    
    

To stop a particular actor by port:    

    lsof -i tcp:2553 | grep -i LISTEN

OSX:    
    
    kill -9 `lsof -i tcp:2551 | grep -i LISTEN`
    kill -9 `lsof -i tcp:2552 | grep -i LISTEN`
    kill -9 `lsof -i tcp:2553 | grep -i LISTEN`
    kill -9 `lsof -i tcp:2554 | grep -i LISTEN`
    kill -9 `lsof -i tcp:2555 | grep -i LISTEN`
    kill -9 `lsof -i tcp:2556 | grep -i LISTEN`
     

Nix:

    fuser -k -n tcp 2551
    fuser -k -n tcp 2552
    fuser -k -n tcp 2553
    fuser -k -n tcp 2554
    fuser -k -n tcp 2555
    fuser -k -n tcp 2556