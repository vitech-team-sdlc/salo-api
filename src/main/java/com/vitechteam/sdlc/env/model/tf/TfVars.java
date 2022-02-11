package com.vitechteam.sdlc.env.model.tf;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.vitechteam.sdlc.env.model.Environment;
import com.vitechteam.sdlc.env.model.cluster.Cluster;
import com.vitechteam.sdlc.env.model.cluster.Label;
import com.vitechteam.sdlc.env.model.cluster.Tag;
import com.vitechteam.sdlc.env.model.cluster.Taint;
import com.vitechteam.sdlc.env.model.config.IngressConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder(alphabetic = true)
public class TfVars {

    private Map<String, Object> unknownFields = new HashMap<>();

    @JsonProperty("cluster_name")
    String clusterName;

    @JsonProperty("apex_domain")
    String apexDomain;
    @JsonProperty("subdomain")
    String subdomain;
    @JsonProperty("domain_registered_in_same_aws_account")
    boolean domainRegisteredInSameAwsAccount;

    @JsonProperty("production_letsencrypt")
    boolean productionLetsencrypt;
    @JsonProperty("enable_tls")
    boolean enableTls;
    @JsonProperty("tls_email")
    String tlsEmail;
    @JsonProperty("jx_bot_username")
    String jxBotUsername;
    @JsonProperty("jx_git_url")
    String jxGitUrl;
    String region;
    Map<String, Worker> workers;


    @JsonAnyGetter
    public Map<String, Object> getUnknownFields() {
        return unknownFields;
    }

    @JsonAnySetter
    public void setUnknownField(String name, Object value) {
        unknownFields.put(name, value);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonPropertyOrder(alphabetic = true)
    public static class Worker {

        private Map<String, Object> unknownFields = new HashMap<>();

        @JsonProperty("asg_desired_capacity")
        int asgDesiredCapacity;
        @JsonProperty("asg_max_size")
        int asgMaxSize;
        @JsonProperty("asg_min_size")
        int asgMinSize;
        @JsonProperty("k8s_labels")
        Collection<Label> k8SLabels;
        @JsonProperty("k8s_taints")
        Collection<Taint> k8STaints;
        @JsonProperty("on_demand_base_capacity")
        int onDemandBaseCapacity;
        @JsonProperty("override_instance_types")
        Collection<String> overrideInstanceTypes;
        @JsonProperty("spot_price")
        String spotPrice;
        @JsonProperty("root_volume_size")
        int rootVolumeSize;
        @JsonProperty("root_encrypted")
        boolean rootEncrypted;
        @JsonProperty("tags")
        Collection<Tag> tags;

        @JsonAnyGetter
        public Map<String, Object> getUnknownFields() {
            return unknownFields;
        }

        @JsonAnySetter
        public void setUnknownField(String name, Object value) {
            unknownFields.put(name, value);
        }
    }

    public void mergeWith(Environment environment, IngressConfig ingressConfig) {
        final Cluster cluster = environment.cluster();
        this.clusterName = String.format("%s-%s", cluster.getName(), environment.config().key());
        this.apexDomain = ingressConfig.getDomain();
        this.subdomain = cluster.getName();
        this.domainRegisteredInSameAwsAccount = cluster.isDomainOwner();

        this.region = cluster.getRegion();
        this.jxGitUrl = environment.envRepository().url();
        this.tlsEmail = ingressConfig.getTls().getEmail();
        this.enableTls = true;
        this.jxBotUsername = cluster.getJxBotUsername();

        this.workers.clear();

        cluster.getNodeGroups().forEach(node -> this.workers.put(node.name(), new Worker(
                new HashMap<>(),
                node.minSize(),
                node.maxSize(),
                node.minSize(),
                node.labels(),
                node.taints(),
                node.maxSize() - node.spotSize(),
                node.vmTypes(),
                "0.04",
                node.volumeSize(),
                true,
                node.tags()
        )));
    }

}
