package com.crimeLink.anayzer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class CallAnalysisResultDTO {

    @JsonProperty("analysis_id")
    private String analysisId;

    private String status;
    private String timestamp;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("total_calls")
    private int totalCalls;

    @JsonProperty("unique_numbers")
    private List<String> uniqueNumbers;

    @JsonProperty("call_frequency")
    private Map<String, Integer> callFrequency;

    @JsonProperty("time_pattern")
    private Map<String, Integer> timePattern;

    @JsonProperty("common_contacts")
    private List<CommonContact> commonContacts;

    @JsonProperty("network_graph")
    private NetworkGraph networkGraph;

    @JsonProperty("criminal_matches")
    private List<CriminalMatch> criminalMatches;

    @JsonProperty("risk_score")
    private int riskScore;

    // Inner classes for nested structures

    public static class CommonContact {
        private String phone;
        private int count;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class NetworkGraph {
        private List<Node> nodes;
        private List<Edge> edges;

        @JsonProperty("total_nodes")
        private int totalNodes;

        @JsonProperty("total_edges")
        private int totalEdges;

        private double density;

        public static class Node {
            private String id;
            private String label;
            private String type;
            private int size;
            private double centrality;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public int getSize() {
                return size;
            }

            public void setSize(int size) {
                this.size = size;
            }

            public double getCentrality() {
                return centrality;
            }

            public void setCentrality(double centrality) {
                this.centrality = centrality;
            }
        }

        public static class Edge {
            private String source;
            private String target;
            private int weight;
            private String label;

            public String getSource() {
                return source;
            }

            public void setSource(String source) {
                this.source = source;
            }

            public String getTarget() {
                return target;
            }

            public void setTarget(String target) {
                this.target = target;
            }

            public int getWeight() {
                return weight;
            }

            public void setWeight(int weight) {
                this.weight = weight;
            }

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public void setNodes(List<Node> nodes) {
            this.nodes = nodes;
        }

        public List<Edge> getEdges() {
            return edges;
        }

        public void setEdges(List<Edge> edges) {
            this.edges = edges;
        }

        public int getTotalNodes() {
            return totalNodes;
        }

        public void setTotalNodes(int totalNodes) {
            this.totalNodes = totalNodes;
        }

        public int getTotalEdges() {
            return totalEdges;
        }

        public void setTotalEdges(int totalEdges) {
            this.totalEdges = totalEdges;
        }

        public double getDensity() {
            return density;
        }

        public void setDensity(double density) {
            this.density = density;
        }
    }

    public static class CriminalMatch {
        private String phone;

        @JsonProperty("criminal_id")
        private String criminalId;

        private String name;
        private String nic;

        @JsonProperty("crime_history")
        private List<Crime> crimeHistory;

        public static class Crime {
            @JsonProperty("crime_type")
            private String crimeType;

            private String date;
            private String status;

            public String getCrimeType() {
                return crimeType;
            }

            public void setCrimeType(String crimeType) {
                this.crimeType = crimeType;
            }

            public String getDate() {
                return date;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getCriminalId() {
            return criminalId;
        }

        public void setCriminalId(String criminalId) {
            this.criminalId = criminalId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNic() {
            return nic;
        }

        public void setNic(String nic) {
            this.nic = nic;
        }

        public List<Crime> getCrimeHistory() {
            return crimeHistory;
        }

        public void setCrimeHistory(List<Crime> crimeHistory) {
            this.crimeHistory = crimeHistory;
        }
    }

    // Getters and Setters

    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTotalCalls() {
        return totalCalls;
    }

    public void setTotalCalls(int totalCalls) {
        this.totalCalls = totalCalls;
    }

    public List<String> getUniqueNumbers() {
        return uniqueNumbers;
    }

    public void setUniqueNumbers(List<String> uniqueNumbers) {
        this.uniqueNumbers = uniqueNumbers;
    }

    public Map<String, Integer> getCallFrequency() {
        return callFrequency;
    }

    public void setCallFrequency(Map<String, Integer> callFrequency) {
        this.callFrequency = callFrequency;
    }

    public Map<String, Integer> getTimePattern() {
        return timePattern;
    }

    public void setTimePattern(Map<String, Integer> timePattern) {
        this.timePattern = timePattern;
    }

    public List<CommonContact> getCommonContacts() {
        return commonContacts;
    }

    public void setCommonContacts(List<CommonContact> commonContacts) {
        this.commonContacts = commonContacts;
    }

    public NetworkGraph getNetworkGraph() {
        return networkGraph;
    }

    public void setNetworkGraph(NetworkGraph networkGraph) {
        this.networkGraph = networkGraph;
    }

    public List<CriminalMatch> getCriminalMatches() {
        return criminalMatches;
    }

    public void setCriminalMatches(List<CriminalMatch> criminalMatches) {
        this.criminalMatches = criminalMatches;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }
}
