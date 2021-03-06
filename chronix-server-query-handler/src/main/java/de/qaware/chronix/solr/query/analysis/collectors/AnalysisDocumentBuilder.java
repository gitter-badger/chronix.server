/*
 * Copyright (C) 2015 QAware GmbH
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.qaware.chronix.solr.query.analysis.collectors;

import de.qaware.chronix.Schema;
import de.qaware.chronix.converter.BinaryTimeSeries;
import de.qaware.chronix.converter.KassiopeiaSimpleConverter;
import de.qaware.chronix.timeseries.MetricTimeSeries;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author f.lautenschlager
 */
public class AnalysisDocumentBuilder {

    private AnalysisDocumentBuilder() {
        //avoid instances
    }

    /**
     * Collects the given document and groups them using the join function result
     *
     * @param docs         - the found documents that should be grouped by the join function
     * @param joinFunction - the join function
     * @return the grouped documents
     */
    public static Map<String, List<SolrDocument>> collect(SolrDocumentList docs, Function<SolrDocument, String> joinFunction) {
        Map<String, List<SolrDocument>> collectedDocs = new HashMap<>();

        docs.stream().forEach(doc -> {
            String key = joinFunction.apply(doc);

            if (!collectedDocs.containsKey(key)) {
                collectedDocs.put(key, new ArrayList<>());
            }

            collectedDocs.get(key).add(doc);

        });


        return collectedDocs;
    }

    /**
     * @param aggregation - the isAggregation including its parameter
     * @param queryStart  - the user query start
     * @param queryEnd    - the user query end
     * @param docs        - the lucene documents that belong to the requested time series
     * @return the aggregated solr document
     */
    public static SolrDocument analyze(Map.Entry<AnalysisType, String[]> aggregation, long queryStart, long queryEnd, Map.Entry<String, List<SolrDocument>> docs) {
        MetricTimeSeries timeSeries = collectDocumentToTimeSeries(queryStart, queryEnd, docs);
        double value = analyzeTimeSeries(timeSeries, aggregation);
        return buildDocument(timeSeries, value, aggregation, docs.getKey());
    }


    /**
     * Collects the lucene documents into a single time series
     *
     * @param queryStart - the user query start
     * @param queryEnd   - the user query end
     * @param documents  - the lucene documents
     * @return a metric time series that holds all the points
     */
    private static MetricTimeSeries collectDocumentToTimeSeries(long queryStart, long queryEnd, Map.Entry<String, List<SolrDocument>> documents) {
        //Collect all document of a time series
        return documents.getValue().stream().map(tsDoc -> convert(tsDoc, queryStart, queryEnd))
                .collect(Collectors.reducing((t1, t2) -> {
                    t1.addAll(t2.getPoints());
                    return t1;
                })).get();
    }

    /**
     * Aggregates the metric time series using the given isAggregation
     *
     * @param timeSeries  - the time series
     * @param aggregation - the isAggregation
     * @return the aggregated value
     */
    private static double analyzeTimeSeries(MetricTimeSeries timeSeries, Map.Entry<AnalysisType, String[]> aggregation) {
        return AnalysisEvaluator.evaluate(timeSeries.getPoints(), aggregation.getKey(), aggregation.getValue());
    }

    /**
     * Builds a solr document that is needed for the response from the aggregated time series
     *
     * @param timeSeries  - the time series
     * @param value       - the isAggregation value
     * @param aggregation - the isAggregation
     * @param key         - the join key
     * @return a solr document holding the attributes and the aggregated value
     */
    private static SolrDocument buildDocument(MetricTimeSeries timeSeries, double value, Map.Entry<AnalysisType, String[]> aggregation, String key) {

        boolean highLevelAnalysis = AnalysisType.isHighLevel(aggregation.getKey());

        //-1 on high level analyses marks that the time series is ok and should not returned
        if (highLevelAnalysis && value < 0) {
            return null;
        }

        SolrDocument doc;

        if (highLevelAnalysis) {
            doc = convert(timeSeries, true);
        } else {
            doc = convert(timeSeries, false);
            doc.put("value", value);
        }

        //Add some information about the analysis
        doc.put("analysis", aggregation.getKey().name());
        doc.put("analysisParam", String.join("-", aggregation.getValue()));

        //add the join key
        doc.put("joinKey", key);

        return doc;
    }


    /**
     * Converts the given Lucene document in a metric time series
     *
     * @param doc        - the lucene document
     * @param queryStart - the query start
     * @param queryEnd   - the query end
     * @return a metric time series
     */
    private static MetricTimeSeries convert(SolrDocument doc, long queryStart, long queryEnd) {
        BinaryTimeSeries.Builder binaryDocument = new BinaryTimeSeries.Builder();
        doc.forEach((field, value) -> {
            if (value instanceof ByteBuffer) {
                binaryDocument.field(field, ((ByteBuffer) value).array());
            } else {
                binaryDocument.field(field, value);
            }
        });

        KassiopeiaSimpleConverter converter = new KassiopeiaSimpleConverter();
        return converter.from(binaryDocument.build(), queryStart, queryEnd);
    }

    private static SolrDocument convert(MetricTimeSeries timeSeries, boolean withData) {
        KassiopeiaSimpleConverter converter = new KassiopeiaSimpleConverter();

        SolrDocument doc = new SolrDocument();
        converter.to(timeSeries).getFields().forEach((field, value) -> {

            if (field.equals(Schema.DATA)) {
                if (withData) {
                    doc.addField(field, value);
                }
            } else {
                doc.addField(field, value);
            }

        });
        return doc;
    }
}
