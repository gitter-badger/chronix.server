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
package de.qaware.chronix.solr.query.analysis.collectors

import de.qaware.chronix.dts.MetricDataPoint
import de.qaware.chronix.timeseries.MetricTimeSeries
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for the isAggregation Evaluator
 * @author f.lautenschlager
 */
class AnalysisEvaluatorTest extends Specification {
    @Unroll
    def "test evaluate aggregation '#analysis'. Expected value is #expected"() {
        given:
        MetricTimeSeries.Builder timeSeries = new MetricTimeSeries.Builder("Aggregation");
        10.times {
            timeSeries.point(new MetricDataPoint(it, it * 10))
        }
        timeSeries.point(new MetricDataPoint(11, 9999))
        MetricTimeSeries ts = timeSeries.build()


        when:
        double analysisValue = AnalysisEvaluator.evaluate(ts.getPoints(), analysis, analysisArgs)

        then:
        analysisValue == expected

        where:
        analysis << [AnalysisType.MIN, AnalysisType.MAX,
                     AnalysisType.AVG, AnalysisType.DEV,
                     AnalysisType.P, AnalysisType.TREND,
                     AnalysisType.OUTLIER, AnalysisType.FREQUENCY]
        analysisArgs << [[] as String, [] as String,
                         [] as String, [] as String,
                         ["0.25d"] as String[], [] as String[],
                         [] as String, ["10", "6"] as String[]]
        expected << [0d, 9999d, 949.9090909090909d, 3001.381363790528, 25.0,
                     1, 1, -1]
    }

    def "test private constructor"() {
        when:
        AnalysisEvaluator.newInstance()

        then:
        noExceptionThrown()
    }
}
