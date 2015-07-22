package clusterconsole.client.domain

import clusterconsole.http.{ DiscoveredCluster, ClusterForm }

case class GuiState(clusterForm: ClusterForm, discoveredClusters: Map[String, DiscoveredCluster])
