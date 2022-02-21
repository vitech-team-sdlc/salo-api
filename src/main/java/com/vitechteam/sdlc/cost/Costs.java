package com.vitechteam.sdlc.cost;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitechteam.sdlc.cost.model.CostView;
import com.vitechteam.sdlc.cost.model.Product;
import com.vitechteam.sdlc.cost.model.ProductCost;
import com.vitechteam.sdlc.env.model.Environment;
import com.vitechteam.sdlc.template.model.SaloTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.pricing.PricingClient;
import software.amazon.awssdk.services.pricing.model.Filter;
import software.amazon.awssdk.services.pricing.model.FilterType;
import software.amazon.awssdk.services.pricing.model.GetProductsResponse;
import software.amazon.awssdk.services.pricing.paginators.GetProductsIterable;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Log4j2
public class Costs {
    // QUERY API:    https://docs.aws.amazon.com/awsaccountbilling/latest/aboutv2/using-pelong.html
    // BULK API:     https://docs.aws.amazon.com/awsaccountbilling/latest/aboutv2/using-ppslong.html

    public static final Set<String> UNITS = Set.of("Hrs", "Hours");

    private final ObjectMapper mapper;

    private final PricingClient pricingClient = PricingClient.builder()
// TODO: authenticate with our creds .credentialsProvider(AnonymousCredentialsProvider.create())
            .region(Region.US_EAST_1)
            .build();

    public CostView estimateForTemplate(SaloTemplate saloTemplate) {
        final BigDecimal eksPrice = findPricing(Product.AmazonEKS, Map.of(
                "regionCode", "us-east-2",
                "tiertype", "HAStandard"
        )).orElseThrow();
        final BigDecimal ec2Price = findPricing(Product.AmazonEC2, Map.of(
                "regionCode", "us-east-2",
                "instanceType", "m5.xlarge",
                "operation", "RunInstances",
                "tenancy", "Shared",
                "capacitystatus", "UnusedCapacityReservation"
        )).orElseThrow();

        final long numberOfEksClusters = saloTemplate.environments().stream()
                .filter(et -> Environment.DEV_ENV_KEY.equals(et.config().key()) || et.config().remoteCluster())
                .count();

        final long numberOfEc2Instances = saloTemplate.environments().stream()
                .filter(et -> Environment.DEV_ENV_KEY.equals(et.config().key()) || et.config().remoteCluster())
                .flatMap(et -> et.cluster().nodeGroups().stream())
                .map(SaloTemplate.EnvironmentTemplate.ClusterTemplate.NodeGroupTemplate::maxSize)
                .count();

        return CostView.of(Map.of(
                   Product.AmazonEKS, new ProductCost(numberOfEksClusters, eksPrice),
                   Product.AmazonEC2, new ProductCost(numberOfEc2Instances, ec2Price)
                ));
    }

    private Optional<BigDecimal> findPricing(Product product, Map<String, String> filters) {
        final GetProductsIterable productPrices = pricingClient.getProductsPaginator(builder -> builder
                .serviceCode(product.name())
                .filters(filters.entrySet().stream()
                        .map(entry -> Filter.builder()
                                .type(FilterType.TERM_MATCH)
                                .field(entry.getKey())
                                .value(entry.getValue())
                                .build())
                        .toList())
        );

        return findMaxHourlyPrice(productPrices);
    }

    private Optional<BigDecimal> findMaxHourlyPrice(GetProductsIterable products) {
        products.stream().flatMap(r -> r.priceList().stream()).forEach(log::info);

        return products.stream()
                .filter(GetProductsResponse::hasPriceList)
                .map(GetProductsResponse::priceList)
                .flatMap(Collection::stream)
                .map(this::parse)
                .flatMap(price -> streamFields(price.at("/terms/OnDemand"))
                        .flatMap(entry -> {
                            final String key = entry.getKey();
                            final JsonNode onDemand = entry.getValue();
                            return streamFields(onDemand.at("/priceDimensions"))
                                    .filter(priceEntry -> priceEntry.getKey().startsWith(key))
                                    .map(Map.Entry::getValue)
                                    .filter(priceDim -> UNITS.contains(priceDim.get("unit").asText()))
                                    .map(priceDim -> priceDim.at("/pricePerUnit/USD").asText())
                                    .map(BigDecimal::new);
                        }))
                .reduce(BigDecimal::max);
    }

    private JsonNode parse(String price) {
        try {
            return mapper.readTree(price);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Stream<Map.Entry<String, JsonNode>> streamFields(JsonNode onDemandPricing) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        onDemandPricing.fields(),
                        Spliterator.ORDERED
                ),
                false
        );
    }
}
