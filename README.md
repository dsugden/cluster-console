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
    
    sbt "project sampleCluster" "runMain samplecluster.SampleClusterApp 127.0.0.1 2551"
    
