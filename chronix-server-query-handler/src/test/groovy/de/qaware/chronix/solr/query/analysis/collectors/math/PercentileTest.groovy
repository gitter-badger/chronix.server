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
package de.qaware.chronix.solr.query.analysis.collectors.math

import spock.lang.Specification

import java.util.stream.DoubleStream
/**
 * Unit test for the percentile class
 * @author f.lautenschlager
 */
class PercentileTest extends Specification {

    def "test private constructor"() {
        when:
        Percentile.newInstance()

        then:
        noExceptionThrown()
    }

    def "test evaluate 0.5 percentile"() {
        given:
        def percentile = 0.5

        when:
        def value = Percentile.evaluate(stream, percentile)

        then:
        value == expected

        where:
        stream << [DoubleStream.builder().add(10).add(11).build(), DoubleStream.builder().add(10).build()]
        expected << [10.5, 10]
    }
}
